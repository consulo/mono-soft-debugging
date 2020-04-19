/*
 * Copyright (c) 1998, 2006, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package mono.debugger;

import java.util.LinkedList;

import mono.debugger.event.EventQueue;
import mono.debugger.event.EventSet;

public class EventQueueImpl extends MirrorImpl implements EventQueue {

    /*
     * Note this is not a synchronized list. Iteration/update should be
     * protected through the 'this' monitor.
     */
    LinkedList<EventSet> eventSets = new LinkedList<EventSet>();

    TargetVM target;
    boolean closed = false;

    EventQueueImpl(VirtualMachine vm, TargetVM target) {
        super(vm);
        this.target = target;
        target.addEventQueue(this);
    }

    /*
     * Override superclass back to default equality
     */
    @Override
	public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
	public int hashCode() {
        return System.identityHashCode(this);
    }

    synchronized void enqueue(EventSet eventSet) {
        eventSets.add(eventSet);
        notifyAll();
    }

    synchronized int size() {
        return eventSets.size();
    }

    synchronized void close() {
        if (!closed) {
            closed = true; // OK for this the be first since synchronized

            // place VMDisconnectEvent into queue
           // enqueue(new EventSetImpl(vm, (byte)JDWP.EventKind.VM_DISCONNECTED));
        }
    }

    @Override
	public EventSet remove() throws InterruptedException {
        return remove(0);
    }

    /**
     * Filter out events not for user's eyes.
     * Then filter out empty sets.
     */
    @Override
	public EventSet remove(long timeout) throws InterruptedException {
        if (timeout < 0) {
            throw new IllegalArgumentException("Timeout cannot be negative");
        }

        EventSet eventSet;
        while (true) {
            EventSetImpl fullEventSet = removeUnfiltered(timeout);
            if (fullEventSet == null) {
                eventSet = null;  // timeout
                break;
            }
            /*
             * Remove events from the event set for which
             * there is no corresponding enabled request (
             * this includes our internally requested events.)
             * This never returns null
             */
            eventSet = fullEventSet.userFilter();
            if (!eventSet.isEmpty()) {
                break;
            }
        }

        if ((eventSet != null) && (eventSet.suspendPolicy() == SuspendPolicy.ALL)) {
            vm.notifySuspend();
        }

        return eventSet;
    }

    private TimerThread startTimerThread(long timeout) {
        TimerThread thread = new TimerThread(timeout);
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    private boolean shouldWait(TimerThread timerThread) {
        return !closed && eventSets.isEmpty() &&
               ((timerThread == null) ? true : !timerThread.timedOut());
    }

    private EventSetImpl removeUnfiltered(long timeout)
                                               throws InterruptedException {
        EventSetImpl eventSet = null;

        /*
         * Make sure the VM has completed initialization before
         * trying to build events.
         */
        vm.waitInitCompletion();

        synchronized(this) {
            if (!eventSets.isEmpty()) {
                /*
                 * If there's already something there, no need
                 * for anything elaborate.
                 */
                eventSet = (EventSetImpl)eventSets.removeFirst();
            } else {
                /*
                 * If a timeout was specified, create a thread to
                 * notify this one when a timeout
                 * occurs. We can't use the timed version of wait()
                 * because it is possible for multiple enqueue() calls
                 * before we see something in the eventSet queue
                 * (this is possible when multiple threads call
                 * remove() concurrently -- not a great idea, but
                 * it should be supported). Even if enqueue() did a
                 * notify() instead of notifyAll() we are not able to
                 * use a timed wait because there's no way to distinguish
                 * a timeout from a notify.  That limitation implies a
                 * possible race condition between a timed out thread
                 * and a notified thread.
                 */
                TimerThread timerThread = null;
                try {
                    if (timeout > 0) {
                        timerThread = startTimerThread(timeout);
                    }

                    while (shouldWait(timerThread)) {
                        this.wait();
                    }
                } finally {
                    if ((timerThread != null) && !timerThread.timedOut()) {
                        timerThread.interrupt();
                    }
                }

                if (eventSets.isEmpty()) {
                    if (closed) {
                        throw new VMDisconnectedException();
                    }
                } else {
                    eventSet = (EventSetImpl)eventSets.removeFirst();
                }
            }
        }

        // The build is synchronized on the event set, don't hold
        // the queue lock.
        if (eventSet != null) {
            target.notifyDequeueEventSet();
            eventSet.build();
        }
        return eventSet;
    }

    private class TimerThread extends Thread {
        private boolean timedOut = false;
        private long timeout;

        TimerThread(long timeout) {
            super(vm.threadGroupForJDI(), "JDI Event Queue Timer");
            this.timeout = timeout;
        }

        boolean timedOut() {
            return timedOut;
        }

        @Override
		public void run() {
            try {
                Thread.sleep(timeout);
                EventQueueImpl queue = EventQueueImpl.this;
                synchronized(queue) {
                    timedOut = true;
                    queue.notifyAll();
                }
            } catch (InterruptedException e) {
                // Exit without notifying
            }
        }
    }
}