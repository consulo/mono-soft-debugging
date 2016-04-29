/*
 * Copyright (c) 1998, 2011, Oracle and/or its affiliates. All rights reserved.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.jetbrains.annotations.NotNull;
import mono.debugger.event.*;
import mono.debugger.request.EventRequest;

/*
 * An EventSet is normally created by the transport reader thread when
 * it reads a JDWP Composite command.  The constructor doesn't unpack
 * the events contained in the Composite command and create EventImpls
 * for them because that process might involve calling back into the back-end
 * which should not be done by the transport reader thread.  Instead,
 * the raw bytes of the packet are read and stored in the EventSet.
 * The EventSet is then added to each EventQueue. When an EventSet is
 * removed from an EventQueue, the EventSetImpl.build() method is called.
 * This method reads the packet bytes and creates the actual EventImpl objects.
 * build() also filters out events for our internal handler and puts them in
 * their own EventSet.  This means that the EventImpls that are in the EventSet
 * that is on the queues are all for client requests.
 */
public class EventSetImpl extends ArrayList<Event> implements EventSet
{
	private static final long serialVersionUID = -4857338819787924570L;
	private VirtualMachineImpl vm; // we implement Mirror
	private Packet pkt;
	private SuspendPolicy suspendPolicy;

	@Override
	public String toString()
	{
		String string = "event set, policy:" + suspendPolicy +
				", count:" + this.size() + " = {";
		boolean first = true;
		for(Event event : this)
		{
			if(!first)
			{
				string += ", ";
			}
			string += event.toString();
			first = false;
		}
		string += "}";
		return string;
	}

	public abstract static class EventImpl extends MirrorImpl implements Event
	{

		private final EventKind eventCmd;
		private final int requestID;
		// This is set only for client requests, not internal requests.
		private final EventRequest request;

		/**
		 * Constructor for events.
		 */
		public EventImpl(VirtualMachine virtualMachine, JDWP.Event.Composite.Events.EventsCommon evt, int requestID)
		{
			super(virtualMachine);
			this.eventCmd = evt.eventKind();
			this.requestID = requestID;
			EventRequestManagerImpl ermi = vm.eventRequestManagerImpl();
			request = ermi.request(eventCmd, requestID);
		}

		/**
		 * Constructor for VM disconnected events.
		 */
		protected EventImpl(VirtualMachine virtualMachine, EventKind eventCmd)
		{
			super(virtualMachine);
			this.eventCmd = eventCmd;
			this.requestID = 0;
			this.request = null;
		}

		/*
		 * Override superclass back to default equality
		 */
		@Override
		public boolean equals(Object obj)
		{
			return this == obj;
		}

		@Override
		public EventRequest request()
		{
			return request;
		}

		int requestID()
		{
			return requestID;
		}

		public abstract String eventName();

		@Override
		public String toString()
		{
			return eventName();
		}
	}

	public abstract static class ThreadedEventImpl extends EventImpl
	{
		private ThreadMirror thread;

		public ThreadedEventImpl(VirtualMachine virtualMachine, JDWP.Event.Composite.Events.EventsCommon evt, int requestID, ThreadMirror thread)
		{
			super(virtualMachine, evt, requestID);
			this.thread = thread;
		}

		public ThreadMirror thread()
		{
			return thread;
		}

		@Override
		public String toString()
		{
			return eventName() + " in thread " + thread.name();
		}
	}

	public static abstract class LocatableEventImpl extends ThreadedEventImpl implements Locatable
	{
		private Location location;

		public LocatableEventImpl(VirtualMachine virtualMachine, JDWP.Event.Composite.Events.EventsCommon evt, int requestID, ThreadMirror thread, Location location)
		{
			super(virtualMachine, evt, requestID, thread);
			this.location = location;
		}

		@Override
		public Location location()
		{
			return location;
		}

		/**
		 * For MethodEntry and MethodExit
		 */
		public MethodMirror method()
		{
			return location.method();
		}

		@Override
		public String toString()
		{
			return eventName() + "@" +
					((location() == null) ? " null" : location().toString()) +
					" in thread " + thread().name();
		}
	}

	class StepEventImpl extends LocatableEventImpl implements StepEvent
	{
		StepEventImpl(VirtualMachine virtualMachine, JDWP.Event.Composite.Events.SingleStep evt)
		{
			super(virtualMachine, evt, evt.requestID, evt.thread, evt.location);
		}

		@Override
		public String eventName()
		{
			return "StepEvent";
		}
	}

	class MethodEntryEventImpl extends LocatableEventImpl implements MethodEntryEvent
	{
		MethodEntryEventImpl(VirtualMachine virtualMachine, JDWP.Event.Composite.Events.MethodEntry evt)
		{
			super(virtualMachine, evt, evt.requestID, evt.thread, evt.location);
		}

		@Override
		public String eventName()
		{
			return "MethodEntryEvent";
		}
	}

	class MethodExitEventImpl extends LocatableEventImpl implements MethodExitEvent
	{
		private Value returnVal = null;

		MethodExitEventImpl(VirtualMachine virtualMachine, JDWP.Event.Composite.Events.MethodExit evt)
		{
			super(virtualMachine, evt, evt.requestID, evt.thread, evt.location);
		}

		@Override
		public String eventName()
		{
			return "MethodExitEvent";
		}

		@Override
		public Value returnValue()
		{
			return returnVal;
		}

	}

	class ThreadDeathEventImpl extends ThreadedEventImpl implements ThreadDeathEvent
	{
		ThreadDeathEventImpl(VirtualMachine virtualMachine, JDWP.Event.Composite.Events.ThreadDeath evt)
		{
			super(virtualMachine, evt, evt.requestID, evt.thread);
		}

		@Override
		public String eventName()
		{
			return "ThreadDeathEvent";
		}
	}

	class ThreadStartEventImpl extends ThreadedEventImpl implements ThreadStartEvent
	{
		ThreadStartEventImpl(VirtualMachine virtualMachine, JDWP.Event.Composite.Events.ThreadStart evt)
		{
			super(virtualMachine, evt, evt.requestID, evt.thread);
		}

		@Override
		public String eventName()
		{
			return "ThreadStartEvent";
		}
	}

	/**
	 * Events are constructed on the thread which reads all data from the
	 * transport. This means that the packet cannot be converted to real
	 * JDI objects as that may involve further communications with the
	 * back end which would deadlock.
	 * <p/>
	 * Hence the {@link #build()} method below called by EventQueue.
	 */
	EventSetImpl(VirtualMachine aVm, Packet pkt)
	{
		super();

		// From "MirrorImpl":
		// Yes, its a bit of a hack. But by doing it this
		// way, this is the only place we have to change
		// typing to substitute a new impl.
		vm = (VirtualMachineImpl) aVm;

		this.pkt = pkt;
	}

	/**
	 * Constructor for special events like VM disconnected
	 */
	EventSetImpl(VirtualMachine aVm, byte eventCmd)
	{
		this(aVm, null);
		suspendPolicy = SuspendPolicy.NONE;
		switch(eventCmd)
		{
			default:
				throw new InternalException("Bad singleton event code");
		}
	}

	private void addEvent(EventImpl evt)
	{
		// Note that this class has a public add method that throws
		// an exception so that clients can't modify the EventSet
		super.add(evt);
	}

	/*
	 * Complete the construction of an EventSet.  This is called from
	 * an event handler thread.  It upacks the JDWP events inside
	 * the packet and creates EventImpls for them.  The EventSet is already
	 * on EventQueues when this is called, so it has to be synch.
	 */
	synchronized void build()
	{
		if(pkt == null)
		{
			return;
		}
		PacketStream ps = new PacketStream(vm, pkt);
		JDWP.Event.Composite compEvt = new JDWP.Event.Composite(vm, ps);
		suspendPolicy = SuspendPolicy.values()[compEvt.suspendPolicy];

		for(int i = 0; i < compEvt.events.length; i++)
		{
			EventImpl evt = createEvent(compEvt.events[i]);

			addEvent(evt);
		}
		pkt = null; // No longer needed - free it up

		// Avoid hangs described in 6296125, 6293795
		if(super.size() == 0)
		{
			// This set has no client events.  If we don't do
			// needed resumes, no one else is going to.
			if(suspendPolicy == SuspendPolicy.ALL)
			{
				vm.resume();
			}
			suspendPolicy = SuspendPolicy.NONE;

		}

	}

	/**
	 * Filter out internal events
	 */
	EventSet userFilter()
	{
		return this;
	}

	public EventImpl createEvent(JDWP.Event.Composite.Events evt)
	{
		JDWP.Event.Composite.Events.EventsCommon comm = evt.aEventsCommon;
		switch(evt.eventKind)
		{
			case APPDOMAIN_CREATE:
				return new AppDomainCreateEvent(vm, (JDWP.Event.Composite.Events.AppDomainCreate) comm);
			case APPDOMAIN_UNLOAD:
				return new AppDomainUnloadEvent(vm, (JDWP.Event.Composite.Events.AppDomainUnload) comm);
			case THREAD_START:
				return new ThreadStartEventImpl(vm, (JDWP.Event.Composite.Events.ThreadStart) comm);
			case THREAD_DEATH:
				return new ThreadDeathEventImpl(vm, (JDWP.Event.Composite.Events.ThreadDeath) comm);
			case EXCEPTION:
				return new ExceptionEvent(vm, (JDWP.Event.Composite.Events.Exception) comm);
			case BREAKPOINT:
				return new BreakpointEvent(vm, (JDWP.Event.Composite.Events.Breakpoint) comm);
			case METHOD_ENTRY:
				return new MethodEntryEventImpl(vm, (JDWP.Event.Composite.Events.MethodEntry) comm);
			case METHOD_EXIT:
				return new MethodExitEventImpl(vm, (JDWP.Event.Composite.Events.MethodExit) comm);
			case TYPE_LOAD:
				return new TypeLoadEvent(vm, (JDWP.Event.Composite.Events.TypeLoad) comm);
			case STEP:
				return new StepEventImpl(vm, (JDWP.Event.Composite.Events.SingleStep) comm);
			case ASSEMBLY_LOAD:
				return new AssemblyLoadEvent(vm, (JDWP.Event.Composite.Events.AssemblyLoad) comm);
			case ASSEMBLY_UNLOAD:
				return new AssemblyUnloadEvent(vm, (JDWP.Event.Composite.Events.AssemblyUnLoad) comm);
			case VM_START:
				return new VMStartEvent(vm, (JDWP.Event.Composite.Events.VMStart) comm);
			case VM_DEATH:
				return new VMDeathEvent(vm, (JDWP.Event.Composite.Events.VMDeath) comm);
			case USER_BREAK:
				return new UserBreakEvent(vm, (JDWP.Event.Composite.Events.UserBreak) comm);
			case USER_LOG:
				return new UserLogEvent(vm, (JDWP.Event.Composite.Events.UserLog) comm);
			default:
				// Ignore unknown event types
				System.err.println("Ignoring event cmd " + evt.eventKind + " from the VM");
				return null;
		}
	}

	@Override
	public VirtualMachine virtualMachine()
	{
		return vm;
	}

	@NotNull
	@Override
	public SuspendPolicy suspendPolicy()
	{
		return suspendPolicy;
	}

	@Override
	public ThreadMirror eventThread()
	{
		for(Event event : this)
		{
			if(event instanceof ThreadedEventImpl)
			{
				return ((ThreadedEventImpl) event).thread();
			}
		}
		return null;
	}

	@NotNull
	@Override
	public Iterator<Event> iterator()
	{
		return new Itr();
	}

	@Override
	public Iterator<Event> eventIterator()
	{
		return new Itr();
	}

	public class Itr implements Iterator<Event>
	{
		/**
		 * Index of element to be returned by subsequent call to next.
		 */
		int cursor = 0;

		@Override
		public boolean hasNext()
		{
			return cursor != size();
		}

		@Override
		public Event next()
		{
			try
			{
				Event nxt = get(cursor);
				++cursor;
				return nxt;
			}
			catch(IndexOutOfBoundsException e)
			{
				throw new NoSuchElementException();
			}
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}

   /* below make this unmodifiable */

	@Override
	public boolean add(Event o)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends Event> coll)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> coll)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> coll)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear()
	{
		throw new UnsupportedOperationException();
	}
}
