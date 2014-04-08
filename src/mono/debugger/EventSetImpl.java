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
	private byte suspendPolicy;
	private EventSetImpl internalEventSet;

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

		private final byte eventCmd;
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
		protected EventImpl(VirtualMachine virtualMachine, byte eventCmd)
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
		public int hashCode()
		{
			return System.identityHashCode(this);
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

		EventDestination destination()
		{
			/*
			 * We need to decide if this event is for
             * 1. an internal request
             * 2. a client request that is no longer available, ie
             *    it has been deleted, or disabled and re-enabled
             *    which gives it a new ID.
             * 3. a current client request that is disabled
             * 4. a current enabled client request.
             *
             * We will filter this set into a set
             * that contains only 1s for our internal queue
             * and a set that contains only 4s for our client queue.
             * If we get an EventSet that contains only 2 and 3
             * then we have to resume it if it is not SUSPEND_NONE
             * because no one else will.
             */
			if(requestID == 0)
			{
				/* An unsolicited event.  These have traditionally
                 * been treated as client events.
                 */
				return EventDestination.CLIENT_EVENT;
			}

			// Is this an event for a current client request?
			if(request == null)
			{
				// Nope.  Is it an event for an internal request?
				EventRequestManagerImpl ermi = this.vm.getInternalEventRequestManager();
				if(ermi.request(eventCmd, requestID) != null)
				{
					// Yep
					return EventDestination.INTERNAL_EVENT;
				}
				return EventDestination.UNKNOWN_EVENT;
			}

			// We found a client request
			if(request.isEnabled())
			{
				return EventDestination.CLIENT_EVENT;
			}
			return EventDestination.UNKNOWN_EVENT;
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
		private ThreadReference thread;

		public ThreadedEventImpl(VirtualMachine virtualMachine, JDWP.Event.Composite.Events.EventsCommon evt, int requestID, ThreadReference thread)
		{
			super(virtualMachine, evt, requestID);
			this.thread = thread;
		}

		public ThreadReference thread()
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

		LocatableEventImpl(
				VirtualMachine virtualMachine, JDWP.Event.Composite.Events.EventsCommon evt, int requestID, ThreadReference thread,
				Location location)
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
		public Method method()
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

	class BreakpointEventImpl extends LocatableEventImpl implements BreakpointEvent
	{
		BreakpointEventImpl(VirtualMachine virtualMachine, JDWP.Event.Composite.Events.Breakpoint evt)
		{
			super(virtualMachine, evt, evt.requestID, evt.thread, evt.location);
		}

		@Override
		public String eventName()
		{
			return "BreakpointEvent";
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

		MethodExitEventImpl(VirtualMachine virtualMachine, JDWP.Event.Composite.Events.MethodExitWithReturnValue evt)
		{
			super(virtualMachine, evt, evt.requestID, evt.thread, evt.location);
			returnVal = evt.value;
		}

		@Override
		public String eventName()
		{
			return "MethodExitEvent";
		}

		@Override
		public Value returnValue()
		{
			if(!this.vm.canGetMethodReturnValues())
			{
				throw new UnsupportedOperationException("target does not support return values in MethodExit events");
			}
			return returnVal;
		}

	}

	class MonitorContendedEnterEventImpl extends LocatableEventImpl implements MonitorContendedEnterEvent
	{
		private ObjectReference monitor = null;

		MonitorContendedEnterEventImpl(VirtualMachine virtualMachine, JDWP.Event.Composite.Events.MonitorContendedEnter evt)
		{
			super(virtualMachine, evt, evt.requestID, evt.thread, evt.location);
			this.monitor = evt.object;
		}

		@Override
		public String eventName()
		{
			return "MonitorContendedEnter";
		}

		@Override
		public ObjectReference monitor()
		{
			return monitor;
		}
	}

	class MonitorContendedEnteredEventImpl extends LocatableEventImpl implements MonitorContendedEnteredEvent
	{
		private ObjectReference monitor = null;

		MonitorContendedEnteredEventImpl(VirtualMachine virtualMachine, JDWP.Event.Composite.Events.MonitorContendedEntered evt)
		{
			super(virtualMachine, evt, evt.requestID, evt.thread, evt.location);
			this.monitor = evt.object;
		}

		@Override
		public String eventName()
		{
			return "MonitorContendedEntered";
		}

		@Override
		public ObjectReference monitor()
		{
			return monitor;
		}

	}

	class MonitorWaitEventImpl extends LocatableEventImpl implements MonitorWaitEvent
	{
		private ObjectReference monitor = null;
		private long timeout;

		MonitorWaitEventImpl(VirtualMachine virtualMachine, JDWP.Event.Composite.Events.MonitorWait evt)
		{
			super(virtualMachine, evt, evt.requestID, evt.thread, evt.location);
			this.monitor = evt.object;
			this.timeout = evt.timeout;
		}

		@Override
		public String eventName()
		{
			return "MonitorWait";
		}

		@Override
		public ObjectReference monitor()
		{
			return monitor;
		}

		@Override
		public long timeout()
		{
			return timeout;
		}
	}

	class MonitorWaitedEventImpl extends LocatableEventImpl implements MonitorWaitedEvent
	{
		private ObjectReference monitor = null;
		private boolean timed_out;

		MonitorWaitedEventImpl(VirtualMachine virtualMachine, JDWP.Event.Composite.Events.MonitorWaited evt)
		{
			super(virtualMachine, evt, evt.requestID, evt.thread, evt.location);
			this.monitor = evt.object;
			this.timed_out = evt.timed_out;
		}

		@Override
		public String eventName()
		{
			return "MonitorWaited";
		}

		@Override
		public ObjectReference monitor()
		{
			return monitor;
		}

		@Override
		public boolean timedout()
		{
			return timed_out;
		}
	}

	class ClassPrepareEventImpl extends ThreadedEventImpl implements ClassPrepareEvent
	{
		private ReferenceType referenceType;

		ClassPrepareEventImpl(VirtualMachine virtualMachine, JDWP.Event.Composite.Events.ClassPrepare evt)
		{
			super(virtualMachine, evt, evt.requestID, evt.thread);
			referenceType = this.vm.referenceType(evt.typeID);
		}

		@Override
		public ReferenceType referenceType()
		{
			return referenceType;
		}

		@Override
		public String eventName()
		{
			return "ClassPrepareEvent";
		}
	}

	class ClassUnloadEventImpl extends ThreadedEventImpl implements ClassUnloadEvent
	{
		private ReferenceType referenceType;

		ClassUnloadEventImpl(VirtualMachine virtualMachine, JDWP.Event.Composite.Events.ClassUnload evt)
		{
			super(virtualMachine, evt, evt.requestID, evt.thread);
			referenceType = this.vm.referenceType(evt.typeID);
		}

		@Override
		public ReferenceType referenceType()
		{
			return referenceType;
		}

		@Override
		public String eventName()
		{
			return "ClassUnloadEvent";
		}
	}

	class ExceptionEventImpl extends LocatableEventImpl implements ExceptionEvent
	{
		private ObjectReference exception;
		private Location catchLocation;

		ExceptionEventImpl(VirtualMachine virtualMachine, JDWP.Event.Composite.Events.Exception evt)
		{
			super(virtualMachine, evt, evt.requestID, evt.thread, evt.location);
			this.exception = evt.exception;
			this.catchLocation = evt.catchLocation;
		}

		@Override
		public ObjectReference exception()
		{
			return exception;
		}

		@Override
		public Location catchLocation()
		{
			return catchLocation;
		}

		@Override
		public String eventName()
		{
			return "ExceptionEvent";
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

	class VMStartEventImpl extends ThreadedEventImpl implements VMStartEvent
	{
		VMStartEventImpl(VirtualMachine virtualMachine, JDWP.Event.Composite.Events.VMStart evt)
		{
			super(virtualMachine, evt, evt.requestID, evt.thread);
		}

		@Override
		public String eventName()
		{
			return "VMStartEvent";
		}
	}

	public static class VMDeathEventImpl extends EventImpl implements VMDeathEvent
	{

		public VMDeathEventImpl(VirtualMachine virtualMachine, JDWP.Event.Composite.Events.VMDeath evt)
		{
			super(virtualMachine, evt, evt.requestID);
		}

		@Override
		public String eventName()
		{
			return "VMDeathEvent";
		}
	}

	public static class VMDisconnectEventImpl extends EventImpl implements VMDisconnectEvent
	{
		public VMDisconnectEventImpl(VirtualMachine virtualMachine)
		{
			super(virtualMachine, (byte) JDWP.EventKind.VM_DISCONNECTED);
		}

		@Override
		public String eventName()
		{
			return "VMDisconnectEvent";
		}
	}

	abstract class WatchpointEventImpl extends LocatableEventImpl implements WatchpointEvent
	{
		private final ReferenceTypeImpl refType;
		private final long fieldID;
		private final ObjectReference object;
		private Field field = null;

		WatchpointEventImpl(
				VirtualMachine virtualMachine,
				JDWP.Event.Composite.Events.EventsCommon evt,
				int requestID,
				ThreadReference thread,
				Location location,
				byte refTypeTag,
				long typeID,
				long fieldID,
				ObjectReference object)
		{
			super(virtualMachine, evt, requestID, thread, location);
			this.refType = this.vm.referenceType(typeID);
			this.fieldID = fieldID;
			this.object = object;
		}

		@Override
		public Field field()
		{
			if(field == null)
			{
				field = refType.getFieldMirror(fieldID);
			}
			return field;
		}

		@Override
		public ObjectReference object()
		{
			return object;
		}

		@Override
		public Value valueCurrent()
		{
			if(object == null)
			{
				return refType.getValue(field());
			}
			else
			{
				return object.getValue(field());
			}
		}
	}

	class AccessWatchpointEventImpl extends WatchpointEventImpl implements AccessWatchpointEvent
	{

		AccessWatchpointEventImpl(VirtualMachine virtualMachine, JDWP.Event.Composite.Events.FieldAccess evt)
		{
			super(virtualMachine, evt, evt.requestID, evt.thread, evt.location, evt.refTypeTag, evt.typeID, evt.fieldID, evt.object);
		}

		@Override
		public String eventName()
		{
			return "AccessWatchpoint";
		}
	}

	class ModificationWatchpointEventImpl extends WatchpointEventImpl implements ModificationWatchpointEvent
	{
		Value newValue;

		ModificationWatchpointEventImpl(VirtualMachine virtualMachine, JDWP.Event.Composite.Events.FieldModification evt)
		{
			super(virtualMachine, evt, evt.requestID, evt.thread, evt.location, evt.refTypeTag, evt.typeID, evt.fieldID, evt.object);
			this.newValue = evt.valueToBe;
		}

		@Override
		public Value valueToBe()
		{
			return newValue;
		}

		@Override
		public String eventName()
		{
			return "ModificationWatchpoint";
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
		suspendPolicy = JDWP.SuspendPolicy.NONE;
		switch(eventCmd)
		{
			case JDWP.EventKind.VM_DISCONNECTED:
				addEvent(new VMDisconnectEventImpl(aVm));
				break;

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
		suspendPolicy = compEvt.suspendPolicy;
		if((vm.traceFlags & VirtualMachine.TRACE_EVENTS) != 0)
		{
			switch(suspendPolicy)
			{
				case JDWP.SuspendPolicy.ALL:
					vm.printTrace("EventSet: SUSPEND_ALL");
					break;

				case JDWP.SuspendPolicy.EVENT_THREAD:
					vm.printTrace("EventSet: SUSPEND_EVENT_THREAD");
					break;

				case JDWP.SuspendPolicy.NONE:
					vm.printTrace("EventSet: SUSPEND_NONE");
					break;
			}
		}

		ThreadReference fix6485605 = null;
		for(int i = 0; i < compEvt.events.length; i++)
		{
			EventImpl evt = createEvent(compEvt.events[i]);

			if((vm.traceFlags & VirtualMachine.TRACE_EVENTS) != 0)
			{
				try
				{
					vm.printTrace("Event: " + evt);
				}
				catch(VMDisconnectedException ee)
				{
					// ignore - see bug 6502716
				}
			}

			switch(evt.destination())
			{
				case UNKNOWN_EVENT:
					// Ignore disabled, deleted, unknown events, but
					// save the thread if there is one since we might
					// have to resume it.  Note that events for different
					// threads can't be in the same event set.
					if(evt instanceof ThreadedEventImpl && suspendPolicy == JDWP.SuspendPolicy.EVENT_THREAD)
					{
						fix6485605 = ((ThreadedEventImpl) evt).thread();
					}
					continue;
				case CLIENT_EVENT:
					addEvent(evt);
					break;
				case INTERNAL_EVENT:
					if(internalEventSet == null)
					{
						internalEventSet = new EventSetImpl(this.vm, null);
					}
					internalEventSet.addEvent(evt);
					break;
				default:
					throw new InternalException("Invalid event destination");
			}
		}
		pkt = null; // No longer needed - free it up

		// Avoid hangs described in 6296125, 6293795
		if(super.size() == 0)
		{
			// This set has no client events.  If we don't do
			// needed resumes, no one else is going to.
			if(suspendPolicy == JDWP.SuspendPolicy.ALL)
			{
				vm.resume();
			}
			else if(suspendPolicy == JDWP.SuspendPolicy.EVENT_THREAD)
			{
				// See bug 6485605.
				if(fix6485605 != null)
				{
					fix6485605.resume();
				}
				else
				{
					// apparently, there is nothing to resume.
				}
			}
			suspendPolicy = JDWP.SuspendPolicy.NONE;

		}

	}

	/**
	 * Filter out internal events
	 */
	EventSet userFilter()
	{
		return this;
	}

	/**
	 * Filter out user events.
	 */
	EventSet internalFilter()
	{
		return this.internalEventSet;
	}

	public EventImpl createEvent(JDWP.Event.Composite.Events evt)
	{
		JDWP.Event.Composite.Events.EventsCommon comm = evt.aEventsCommon;
		switch(evt.eventKind)
		{
			case JDWP.EventKind.THREAD_START:
				return new ThreadStartEventImpl(vm, (JDWP.Event.Composite.Events.ThreadStart) comm);

			case JDWP.EventKind.THREAD_END:
				return new ThreadDeathEventImpl(vm, (JDWP.Event.Composite.Events.ThreadDeath) comm);

			case JDWP.EventKind.EXCEPTION:
				return new ExceptionEventImpl(vm, (JDWP.Event.Composite.Events.Exception) comm);

			case JDWP.EventKind.BREAKPOINT:
				return new BreakpointEventImpl(vm, (JDWP.Event.Composite.Events.Breakpoint) comm);

			case JDWP.EventKind.METHOD_ENTRY:
				return new MethodEntryEventImpl(vm, (JDWP.Event.Composite.Events.MethodEntry) comm);

			case JDWP.EventKind.METHOD_EXIT:
				return new MethodExitEventImpl(vm, (JDWP.Event.Composite.Events.MethodExit) comm);

			case JDWP.EventKind.METHOD_EXIT_WITH_RETURN_VALUE:
				return new MethodExitEventImpl(vm, (JDWP.Event.Composite.Events.MethodExitWithReturnValue) comm);

			case JDWP.EventKind.FIELD_ACCESS:
				return new AccessWatchpointEventImpl(vm, (JDWP.Event.Composite.Events.FieldAccess) comm);

			case JDWP.EventKind.FIELD_MODIFICATION:
				return new ModificationWatchpointEventImpl(vm, (JDWP.Event.Composite.Events.FieldModification) comm);

			case JDWP.EventKind.SINGLE_STEP:
				return new StepEventImpl(vm, (JDWP.Event.Composite.Events.SingleStep) comm);

			case JDWP.EventKind.ASSEMBLY_LOAD:
				return new AssemblyLoadEvent(vm, (JDWP.Event.Composite.Events.AssemblyLoad) comm);

			case JDWP.EventKind.ASSEMBLY_UNLOAD:
				return new AssemblyUnloadEvent(vm, (JDWP.Event.Composite.Events.AssemblyUnLoad) comm);

			case JDWP.EventKind.MONITOR_CONTENDED_ENTER:
				return new MonitorContendedEnterEventImpl(vm, (JDWP.Event.Composite.Events.MonitorContendedEnter) comm);

			case JDWP.EventKind.MONITOR_CONTENDED_ENTERED:
				return new MonitorContendedEnteredEventImpl(vm, (JDWP.Event.Composite.Events.MonitorContendedEntered) comm);

			case JDWP.EventKind.MONITOR_WAIT:
				return new MonitorWaitEventImpl(vm, (JDWP.Event.Composite.Events.MonitorWait) comm);

			case JDWP.EventKind.MONITOR_WAITED:
				return new MonitorWaitedEventImpl(vm, (JDWP.Event.Composite.Events.MonitorWaited) comm);

			case JDWP.EventKind.VM_START:
				return new VMStartEventImpl(vm, (JDWP.Event.Composite.Events.VMStart) comm);

			case JDWP.EventKind.VM_DEATH:
				return new VMDeathEventImpl(vm, (JDWP.Event.Composite.Events.VMDeath) comm);

			default:
				// Ignore unknown event types
				System.err.println("Ignoring event cmd " +
						evt.eventKind + " from the VM");
				return null;
		}
	}

	@Override
	public VirtualMachine virtualMachine()
	{
		return vm;
	}

	@Override
	public int suspendPolicy()
	{
		return EventRequestManagerImpl.JDWPtoJDISuspendPolicy(suspendPolicy);
	}

	private ThreadReference eventThread()
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

	@Override
	public void resume()
	{
		switch(suspendPolicy())
		{
			case EventRequest.SUSPEND_ALL:
				vm.resume();
				break;
			case EventRequest.SUSPEND_EVENT_THREAD:
				ThreadReference thread = eventThread();
				if(thread == null)
				{
					throw new InternalException("Inconsistent suspend policy");
				}
				thread.resume();
				break;
			case EventRequest.SUSPEND_NONE:
				// Do nothing
				break;
			default:
				throw new InternalException("Invalid suspend policy");
		}
	}

	@Override
	public Iterator<Event> iterator()
	{
		return new Itr();
	}

	@Override
	public EventIterator eventIterator()
	{
		return new Itr();
	}

	public class Itr implements EventIterator
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
		public Event nextEvent()
		{
			return next();
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
