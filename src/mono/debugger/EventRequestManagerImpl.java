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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import mono.debugger.request.*;

/**
 * This interface is used to create and remove Breakpoints, Watchpoints,
 * etc.
 * It include implementations of all the request interfaces..
 */
// Warnings from List filters and List[] requestLists is  hard to fix.
// Remove SuppressWarning when we fix the warnings from List filters
// and List[] requestLists. The generic array is not supported.
@SuppressWarnings("unchecked")
class EventRequestManagerImpl extends MirrorImpl implements EventRequestManager
{
	List<? extends EventRequest>[] requestLists;

	static int JDWPtoJDISuspendPolicy(byte jdwpPolicy)
	{
		switch(jdwpPolicy)
		{
			case JDWP.SuspendPolicy.ALL:
				return EventRequest.SUSPEND_ALL;
			case JDWP.SuspendPolicy.EVENT_THREAD:
				return EventRequest.SUSPEND_EVENT_THREAD;
			case JDWP.SuspendPolicy.NONE:
				return EventRequest.SUSPEND_NONE;
			default:
				throw new IllegalArgumentException("Illegal policy constant: " + jdwpPolicy);
		}
	}

	static byte JDItoJDWPSuspendPolicy(int jdiPolicy)
	{
		switch(jdiPolicy)
		{
			case EventRequest.SUSPEND_ALL:
				return JDWP.SuspendPolicy.ALL;
			case EventRequest.SUSPEND_EVENT_THREAD:
				return JDWP.SuspendPolicy.EVENT_THREAD;
			case EventRequest.SUSPEND_NONE:
				return JDWP.SuspendPolicy.NONE;
			default:
				throw new IllegalArgumentException("Illegal policy constant: " + jdiPolicy);
		}
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

	abstract class EventRequestImpl extends MirrorImpl implements EventRequest
	{
		int id;

		/*
		 * This list is not protected by a synchronized wrapper. All
		 * access/modification should be protected by synchronizing on
		 * the enclosing instance of EventRequestImpl.
		 */ List<Object> filters = new ArrayList<Object>();

		boolean isEnabled = false;
		boolean deleted = false;
		byte suspendPolicy = JDWP.SuspendPolicy.ALL;
		private Map<Object, Object> clientProperties = null;

		EventRequestImpl()
		{
			super(EventRequestManagerImpl.this.vm);
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

		abstract int eventCmd();

		InvalidRequestStateException invalidState()
		{
			return new InvalidRequestStateException(toString());
		}

		String state()
		{
			return deleted ? " (deleted)" : (isEnabled() ? " (enabled)" : " (disabled)");
		}

		/**
		 * @return all the event request of this kind
		 */
		List requestList()
		{
			return EventRequestManagerImpl.this.requestList(eventCmd());
		}

		/**
		 * delete the event request
		 */
		void delete()
		{
			if(!deleted)
			{
				requestList().remove(this);
				disable(); /* must do BEFORE delete */
				deleted = true;
			}
		}

		@Override
		public boolean isEnabled()
		{
			return isEnabled;
		}

		@Override
		public void enable()
		{
			setEnabled(true);
		}

		@Override
		public void disable()
		{
			setEnabled(false);
		}

		@Override
		public synchronized void setEnabled(boolean val)
		{
			if(deleted)
			{
				throw invalidState();
			}
			else
			{
				if(val != isEnabled)
				{
					if(isEnabled)
					{
						clear();
					}
					else
					{
						set();
					}
				}
			}
		}

		@Override
		public synchronized void addCountFilter(int count)
		{
			if(isEnabled() || deleted)
			{
				throw invalidState();
			}
			if(count < 1)
			{
				throw new IllegalArgumentException("count is less than one");
			}
			filters.add(JDWP.EventRequest.Set.Modifier.Count.create(count));
		}

		@Override
		public void setSuspendPolicy(int policy)
		{
			if(isEnabled() || deleted)
			{
				throw invalidState();
			}
			suspendPolicy = JDItoJDWPSuspendPolicy(policy);
		}

		@Override
		public int suspendPolicy()
		{
			return JDWPtoJDISuspendPolicy(suspendPolicy);
		}

		/**
		 * set (enable) the event request
		 */
		synchronized void set()
		{
			JDWP.EventRequest.Set.Modifier[] mods = filters.toArray(new JDWP.EventRequest.Set.Modifier[filters.size()]);
			try
			{
				id = JDWP.EventRequest.Set.process(vm, (byte) eventCmd(), suspendPolicy, mods).requestID;
			}
			catch(JDWPException exc)
			{
				throw exc.toJDIException();
			}
			isEnabled = true;
		}

		synchronized void clear()
		{
			try
			{
				JDWP.EventRequest.Clear.process(vm, (byte) eventCmd(), id);
			}
			catch(JDWPException exc)
			{
				throw exc.toJDIException();
			}
			isEnabled = false;
		}

		/**
		 * @return a small Map
		 * @see #putProperty
		 * @see #getProperty
		 */
		private Map<Object, Object> getProperties()
		{
			if(clientProperties == null)
			{
				clientProperties = new HashMap<Object, Object>(2);
			}
			return clientProperties;
		}

		/**
		 * Returns the value of the property with the specified key.  Only
		 * properties added with <code>putProperty</code> will return
		 * a non-null value.
		 *
		 * @return the value of this property or null
		 * @see #putProperty
		 */
		@Override
		public final Object getProperty(Object key)
		{
			if(clientProperties == null)
			{
				return null;
			}
			else
			{
				return getProperties().get(key);
			}
		}

		/**
		 * Add an arbitrary key/value "property" to this component.
		 *
		 * @see #getProperty
		 */
		@Override
		public final void putProperty(Object key, Object value)
		{
			if(value != null)
			{
				getProperties().put(key, value);
			}
			else
			{
				getProperties().remove(key);
			}
		}
	}

	abstract class ThreadVisibleEventRequestImpl extends EventRequestImpl
	{
		public synchronized void addThreadFilter(ThreadMirror thread)
		{
			validateMirror(thread);
			if(isEnabled() || deleted)
			{
				throw invalidState();
			}
			filters.add(JDWP.EventRequest.Set.Modifier.ThreadOnly.create((ThreadMirror) thread));
		}
	}

	abstract class ClassVisibleEventRequestImpl extends ThreadVisibleEventRequestImpl
	{
		public synchronized void addClassFilter(TypeMirror clazz)
		{
			validateMirror(clazz);
			if(isEnabled() || deleted)
			{
				throw invalidState();
			}
			filters.add(JDWP.EventRequest.Set.Modifier.ClassOnly.create((TypeMirror) clazz));
		}


	}

	class BreakpointRequestImpl extends ClassVisibleEventRequestImpl implements BreakpointRequest
	{
		private final Location location;

		BreakpointRequestImpl(Location location)
		{
			this.location = location;
			filters.add(0, JDWP.EventRequest.Set.Modifier.LocationOnly.create(location));
			requestList().add(this);
		}

		@Override
		public Location location()
		{
			return location;
		}

		@Override
		int eventCmd()
		{
			return JDWP.EventKind.BREAKPOINT;
		}

		@Override
		public String toString()
		{
			return "breakpoint request " + location() + state();
		}
	}


	class ExceptionRequestImpl extends ClassVisibleEventRequestImpl implements ExceptionRequest
	{
		TypeMirror exception = null;
		boolean caught = true;
		boolean uncaught = true;

		ExceptionRequestImpl(
				TypeMirror refType, boolean notifyCaught, boolean notifyUncaught)
		{
			exception = refType;
			caught = notifyCaught;
			uncaught = notifyUncaught;
		  /*  {
                ReferenceTypeImpl exc;
                if (exception == null) {
                    exc = new ClassTypeImpl(vm, 0);
                } else {
                    exc = (ReferenceTypeImpl)exception;
                }
                filters.add(JDWP.EventRequest.Set.Modifier.ExceptionOnly.
                            create(exc, caught, uncaught));
            }  */
			requestList().add(this);
		}

		@Override
		public TypeMirror exception()
		{
			return exception;
		}

		@Override
		public boolean notifyCaught()
		{
			return caught;
		}

		@Override
		public boolean notifyUncaught()
		{
			return uncaught;
		}

		@Override
		int eventCmd()
		{
			return JDWP.EventKind.EXCEPTION;
		}

		@Override
		public String toString()
		{
			return "exception request " + exception() + state();
		}
	}

	class MethodEntryRequestImpl extends ClassVisibleEventRequestImpl implements MethodEntryRequest
	{
		MethodEntryRequestImpl()
		{
			requestList().add(this);
		}

		@Override
		int eventCmd()
		{
			return JDWP.EventKind.METHOD_ENTRY;
		}

		@Override
		public String toString()
		{
			return "method entry request " + state();
		}
	}

	class MethodExitRequestImpl extends ClassVisibleEventRequestImpl implements MethodExitRequest
	{
		MethodExitRequestImpl()
		{
			requestList().add(this);
		}

		@Override
		int eventCmd()
		{
			return JDWP.EventKind.METHOD_EXIT;
		}

		@Override
		public String toString()
		{
			return "method exit request " + state();
		}
	}

	class StepRequestImpl extends ClassVisibleEventRequestImpl implements StepRequest
	{
		ThreadMirror thread;
		StepSize size;
		StepDepth depth;

		StepRequestImpl(ThreadMirror thread, StepSize size, StepDepth depth)
		{
			this.thread = thread;
			this.size = size;
			this.depth = depth;

            /*
             * Make sure this isn't a duplicate
             */
			List<StepRequest> requests = stepRequests();
			Iterator<StepRequest> iter = requests.iterator();
			while(iter.hasNext())
			{
				StepRequest request = iter.next();
				if((request != this) &&
						request.isEnabled() &&
						request.thread().equals(thread))
				{
					throw new DuplicateRequestException("Only one step request allowed per thread");
				}
			}

			filters.add(JDWP.EventRequest.Set.Modifier.Step.create(this.thread, size, depth));
			requestList().add(this);

		}

		@Override
		public StepDepth depth()
		{
			return depth;
		}

		@Override
		public StepSize size()
		{
			return size;
		}

		@Override
		public ThreadMirror thread()
		{
			return thread;
		}

		@Override
		int eventCmd()
		{
			return JDWP.EventKind.SINGLE_STEP;
		}

		@Override
		public String toString()
		{
			return "step request " + thread() + state();
		}
	}

	class ThreadDeathRequestImpl extends ThreadVisibleEventRequestImpl implements ThreadDeathRequest
	{
		ThreadDeathRequestImpl()
		{
			requestList().add(this);
		}

		@Override
		int eventCmd()
		{
			return JDWP.EventKind.THREAD_DEATH;
		}

		@Override
		public String toString()
		{
			return "thread death request " + state();
		}
	}

	class ThreadStartRequestImpl extends ThreadVisibleEventRequestImpl implements ThreadStartRequest
	{
		ThreadStartRequestImpl()
		{
			requestList().add(this);
		}

		@Override
		int eventCmd()
		{
			return JDWP.EventKind.THREAD_START;
		}

		@Override
		public String toString()
		{
			return "thread start request " + state();
		}
	}


	class VMDeathRequestImpl extends EventRequestImpl implements VMDeathRequest
	{
		VMDeathRequestImpl()
		{
			requestList().add(this);
		}

		@Override
		int eventCmd()
		{
			return JDWP.EventKind.VM_DEATH;
		}

		@Override
		public String toString()
		{
			return "VM death request " + state();
		}
	}

	/**
	 * Constructor.
	 */
	EventRequestManagerImpl(VirtualMachine vm)
	{
		super(vm);
		java.lang.reflect.Field[] ekinds = JDWP.EventKind.class.getDeclaredFields();
		int highest = 0;
		for(int i = 0; i < ekinds.length; ++i)
		{
			int val;
			try
			{
				val = ekinds[i].getInt(null);
			}
			catch(IllegalAccessException exc)
			{
				throw new RuntimeException("Got: " + exc);
			}
			if(val > highest)
			{
				highest = val;
			}
		}
		requestLists = new List[highest + 1];
		for(int i = 0; i <= highest; i++)
		{
			requestLists[i] = new ArrayList<EventRequest>();
		}
	}

	@Override
	public ExceptionRequest createExceptionRequest(
			TypeMirror refType, boolean notifyCaught, boolean notifyUncaught)
	{
		validateMirrorOrNull(refType);
		return new ExceptionRequestImpl(refType, notifyCaught, notifyUncaught);
	}

	@Override
	public StepRequest createStepRequest(ThreadMirror thread, StepRequest.StepSize size, StepRequest.StepDepth depth)
	{
		validateMirror(thread);
		return new StepRequestImpl(thread, size, depth);
	}

	@Override
	public ThreadDeathRequest createThreadDeathRequest()
	{
		return new ThreadDeathRequestImpl();
	}

	@NotNull
	@Override
	public EventRequest createAppDomainCreate()
	{
		return new EventRequestImpl()
		{
			{
				requestList().add(this);
			}
			@Override
			int eventCmd()
			{
				return JDWP.EventKind.APPDOMAIN_CREATE;
			}
		};
	}

	@NotNull
	@Override
	public EventRequest createAppDomainUnload()
	{
		return new EventRequestImpl()
		{
			{
				requestList().add(this);
			}
			@Override
			int eventCmd()
			{
				return JDWP.EventKind.APPDOMAIN_UNLOAD;
			}
		};
	}

	@Override
	public ThreadStartRequest createThreadStartRequest()
	{
		return new ThreadStartRequestImpl();
	}

	@Override
	public MethodEntryRequest createMethodEntryRequest()
	{
		return new MethodEntryRequestImpl();
	}

	@Override
	public MethodExitRequest createMethodExitRequest()
	{
		return new MethodExitRequestImpl();
	}

	@Override
	public BreakpointRequest createBreakpointRequest(Location location)
	{
		validateMirror(location);
		if(location.codeIndex() == -1)
		{
			throw new NativeMethodException("Cannot set breakpoints on native methods");
		}
		return new BreakpointRequestImpl(location);
	}

	@Override
	public VMDeathRequest createVMDeathRequest()
	{

		return new VMDeathRequestImpl();
	}

	@Override
	public void deleteEventRequest(EventRequest eventRequest)
	{
		validateMirror(eventRequest);
		((EventRequestImpl) eventRequest).delete();
	}

	@Override
	public void deleteEventRequests(List<? extends EventRequest> eventRequests)
	{
		validateMirrors(eventRequests);
		// copy the eventRequests to avoid ConcurrentModificationException
		Iterator<? extends EventRequest> iter = (new ArrayList<EventRequest>(eventRequests)).iterator();
		while(iter.hasNext())
		{
			((EventRequestImpl) iter.next()).delete();
		}
	}

	@Override
	public void deleteAllBreakpoints()
	{
		requestList(JDWP.EventKind.BREAKPOINT).clear();

		try
		{
			JDWP.EventRequest.ClearAllBreakpoints.process(vm);
		}
		catch(JDWPException exc)
		{
			throw exc.toJDIException();
		}
	}

	@Override
	public List<StepRequest> stepRequests()
	{
		return (List<StepRequest>) unmodifiableRequestList(JDWP.EventKind.SINGLE_STEP);
	}

	@Override
	public List<ThreadStartRequest> threadStartRequests()
	{
		return (List<ThreadStartRequest>) unmodifiableRequestList(JDWP.EventKind.THREAD_START);
	}

	@Override
	public List<ThreadDeathRequest> threadDeathRequests()
	{
		return (List<ThreadDeathRequest>) unmodifiableRequestList(JDWP.EventKind.THREAD_DEATH);
	}

	@Override
	public List<ExceptionRequest> exceptionRequests()
	{
		return (List<ExceptionRequest>) unmodifiableRequestList(JDWP.EventKind.EXCEPTION);
	}

	@Override
	public List<BreakpointRequest> breakpointRequests()
	{
		return (List<BreakpointRequest>) unmodifiableRequestList(JDWP.EventKind.BREAKPOINT);
	}

	@Override
	public List<MethodEntryRequest> methodEntryRequests()
	{
		return (List<MethodEntryRequest>) unmodifiableRequestList(JDWP.EventKind.METHOD_ENTRY);
	}

	@Override
	public List<MethodExitRequest> methodExitRequests()
	{
		return (List<MethodExitRequest>) unmodifiableRequestList(JDWP.EventKind.METHOD_EXIT);
	}

	@NotNull
	@Override
	public List<EventRequest> appDomainCreateEventRequests()
	{
		return (List<EventRequest>) unmodifiableRequestList(JDWP.EventKind.APPDOMAIN_CREATE);
	}

	@NotNull
	@Override
	public List<EventRequest> appDomainUnloadEventRequests()
	{
		return (List<EventRequest>) unmodifiableRequestList(JDWP.EventKind.APPDOMAIN_UNLOAD);
	}

	@Override
	public List<VMDeathRequest> vmDeathRequests()
	{
		return (List<VMDeathRequest>) unmodifiableRequestList(JDWP.EventKind.VM_DEATH);
	}

	List<? extends EventRequest> unmodifiableRequestList(int eventCmd)
	{
		return Collections.unmodifiableList(requestList(eventCmd));
	}

	EventRequest request(int eventCmd, int requestId)
	{
		List<? extends EventRequest> rl = requestList(eventCmd);
		for(int i = rl.size() - 1; i >= 0; i--)
		{
			EventRequestImpl er = (EventRequestImpl) rl.get(i);
			if(er.id == requestId)
			{
				return er;
			}
		}
		return null;
	}

	List<? extends EventRequest> requestList(int eventCmd)
	{
		return requestLists[eventCmd];
	}

}
