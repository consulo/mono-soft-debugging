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

import java.util.List;

import org.jetbrains.annotations.NotNull;
import mono.debugger.connect.spi.Connection;
import mono.debugger.event.EventQueue;
import mono.debugger.protocol.AppDomain_GetRootDomain;
import mono.debugger.protocol.VirtualMachine_GetVersion;
import mono.debugger.protocol.VirtualMachine_SetProtocolVersion;
import mono.debugger.request.EventRequestManager;

public class VirtualMachineImpl extends MirrorImpl implements VirtualMachine
{
	final int sequenceNumber;

	private final TargetVM target;
	private final EventQueueImpl eventQueue;
	private final EventRequestManagerImpl eventRequestManager;
	final VirtualMachineManagerImpl vmManager;
	private final ThreadGroup threadGroupForJDI;

	// Allow direct access to this field so that that tracing code slows down
	// JDI as little as possible when not enabled.
	public int traceFlags = TRACE_NONE;

	static int TRACE_RAW_SENDS = 0x01000000;
	static int TRACE_RAW_RECEIVES = 0x02000000;

	public boolean traceReceives = false;   // pre-compute because of frequency

	private AppDomainMirror myRootAppDomain;

	// These are cached once for the life of the VM
	private VirtualMachine_GetVersion versionInfo;

	// Launched debuggee process
	private Process process;

	// coordinates state changes and corresponding listener notifications
	private VMState state = new VMState(this);

	private final Object initMonitor = new Object();
	private boolean initComplete = false;
	private boolean shutdown = false;

	VirtualMachineImpl(VirtualMachineManager manager, Connection connection, Process process, int sequenceNumber)
	{
		super(null);  // Can't use super(this)
		vm = this;

		this.vmManager = (VirtualMachineManagerImpl) manager;
		this.process = process;
		this.sequenceNumber = sequenceNumber;

        /* Create ThreadGroup to be used by all threads servicing
         * this VM.
         */
		threadGroupForJDI = new ThreadGroup(vmManager.mainGroupForJDI(), "Mono Soft Debugger [" +this.hashCode() + "]");

        /*
         * Set up a thread to communicate with the target VM over
         * the specified transport.
         */
		target = new TargetVM(this, connection);

        /*
         * Initialize client access to event setting and handling
         */
		eventQueue = new EventQueueImpl(this, target);
		eventRequestManager = new EventRequestManagerImpl(this);

		target.start();

        /*
         * Tell other threads, notably TargetVM, that initialization
         * is complete.
         */
		notifyInitCompletion();

		try
		{
			versionInfo = VirtualMachine_GetVersion.process(vm);

			VirtualMachine_SetProtocolVersion.process(vm, MAJOR_VERSION, MINOR_VERSION);
		}
		catch(JDWPException e)
		{
			throw e.toJDIException();
		}
	}

	private void notifyInitCompletion()
	{
		synchronized(initMonitor)
		{
			initComplete = true;
			initMonitor.notifyAll();
		}
	}

	void waitInitCompletion()
	{
		synchronized(initMonitor)
		{
			while(!initComplete)
			{
				try
				{
					initMonitor.wait();
				}
				catch(InterruptedException e)
				{
					// ignore
				}
			}
		}
	}

	VMState state()
	{
		return state;
	}

	void validateVM()
	{
        /*
         * We no longer need to do this.  The spec now says
         * that a VMDisconnected _may_ be thrown in these
         * cases, not that it _will_ be thrown.
         * So, to simplify things we will just let the
         * caller's of this method proceed with their business.
         * If the debuggee is disconnected, either because it
         * crashed or finished or something, or because the
         * debugger called exit() or dispose(), then if
         * we end up trying to communicate with the debuggee,
         * code in TargetVM will throw a VMDisconnectedException.
         * This means that if we can satisfy a request without
         * talking to the debuggee, (eg, with cached data) then
         * VMDisconnectedException will _not_ be thrown.
         * if (shutdown) {
         *    throw new VMDisconnectedException();
         * }
         */
	}

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

	@NotNull
	@Override
	public AppDomainMirror rootAppDomain()
	{
		if(myRootAppDomain == null)
		{
			try
			{
				myRootAppDomain = AppDomain_GetRootDomain.process(vm).myAppDomainMirror;
			}
			catch(JDWPException e)
			{
				throw e.toJDIException();
			}
		}
		return myRootAppDomain;
	}

	@NotNull
	@Override
	public TypeMirror[] findTypes(String typeName, boolean ignoreCase)
	{
		validateVM();
		try
		{
			return JDWP.VirtualMachine.GetTypes.process(vm, typeName, ignoreCase).classes;
		}
		catch(JDWPException exc)
		{
			throw exc.toJDIException();
		}
	}

	@NotNull
	@Override
	public List<ThreadMirror> allThreads()
	{
		validateVM();
		return state.allThreads();
	}

	/*
	 * Sends a command to the back end which is defined to do an
	 * implicit vm-wide resume. The VM can no longer be considered
	 * suspended, so certain cached data must be invalidated.
	 */
	PacketStream sendResumingCommand(CommandSender sender)
	{
		return state.thawCommand(sender);
	}

	/*
	 * The VM has been suspended. Additional caching can be done
	 * as long as there are no pending resumes.
	 */
	void notifySuspend()
	{
		state.freeze();
	}

	@Override
	public void suspend()
	{
		validateVM();
		try
		{
			JDWP.VirtualMachine.Suspend.process(vm);
		}
		catch(JDWPException exc)
		{
			throw exc.toJDIException();
		}
		notifySuspend();
	}

	@Override
	public void resume()
	{
		validateVM();
		CommandSender sender = new CommandSender()
		{
			@Override
			public PacketStream send()
			{
				return JDWP.VirtualMachine.Resume.enqueueCommand(vm);
			}
		};
		try
		{
			PacketStream stream = state.thawCommand(sender);
			JDWP.VirtualMachine.Resume.waitForReply(vm, stream);
		}
		catch(VMDisconnectedException exc)
		{
            /*
             * If the debugger makes a VMDeathRequest with SUSPEND_ALL,
             * then when it does an EventSet.resume after getting the
             * VMDeathEvent, the normal flow of events is that the
             * BE shuts down, but the waitForReply comes back ok.  In this
             * case, the run loop in TargetVM that is waiting for a packet
             * gets an EOF because the socket closes. It generates a
             * VMDisconnectedEvent and everyone is happy.
             * However, sometimes, the BE gets shutdown before this
             * waitForReply completes.  In this case, TargetVM.waitForReply
             * gets awakened with no reply and so gens a VMDisconnectedException
             * which is not what we want.  It might be possible to fix this
             * in the BE, but it is ok to just ignore the VMDisconnectedException
             * here.  This will allow the VMDisconnectedEvent to be generated
             * correctly.  And, if the debugger should happen to make another
             * request, it will get a VMDisconnectedException at that time.
             */
		}
		catch(JDWPException exc)
		{
			switch(exc.errorCode())
			{
				case JDWP.Error.VM_DEAD:
					return;
				default:
					throw exc.toJDIException();
			}
		}
	}

	@NotNull
	@Override
	public EventQueue eventQueue()
	{
        /*
         * No VM validation here. We allow access to the event queue
         * after disconnection, so that there is access to the terminating
         * events.
         */
		return eventQueue;
	}

	@NotNull
	@Override
	public EventRequestManager eventRequestManager()
	{
		validateVM();
		return eventRequestManager;
	}

	EventRequestManagerImpl eventRequestManagerImpl()
	{
		return eventRequestManager;
	}

	@Override
	public void dispose()
	{
		validateVM();
		shutdown = true;
		try
		{
			JDWP.VirtualMachine.Dispose.process(vm);
		}
		catch(JDWPException exc)
		{
			throw exc.toJDIException();
		}
		target.stopListening();
	}

	@Override
	public void exit(int exitCode)
	{
		validateVM();
		shutdown = true;
		try
		{
			JDWP.VirtualMachine.Exit.process(vm, exitCode);
		}
		catch(JDWPException exc)
		{
			throw exc.toJDIException();
		}
		target.stopListening();
	}

	@NotNull
	@Override
	public Process process()
	{
		validateVM();
		return process;
	}

	public boolean isAtLeastVersion(int major, int minor)
	{
		return (versionInfo.jdwpMajor > major) || ((versionInfo.jdwpMajor == major && versionInfo.jdwpMinor >= minor));
	}

	@NotNull
	@Override
	public String version()
	{
		validateVM();
		return versionInfo.jdwpMajor + "." + versionInfo.jdwpMinor;
	}

	@NotNull
	@Override
	public String name()
	{
		validateVM();
		return versionInfo.description;
	}

	@Override
	public void setDebugTraceMode(int traceFlags)
	{
		validateVM();
		this.traceFlags = traceFlags;
		this.traceReceives = (traceFlags & TRACE_RECEIVES) != 0;
	}

	public void printTrace(String string)
	{
		System.err.println("[MDI: " + string + "]");
	}

	public void printReceiveTrace(int depth, String string)
	{
		StringBuilder sb = new StringBuilder("Receiving:");
		for(int i = depth; i > 0; --i)
		{
			sb.append("    ");
		}
		sb.append(string);
		printTrace(sb.toString());
	}

	void sendToTarget(Packet packet)
	{
		target.send(packet);
	}

	void waitForTargetReply(Packet packet)
	{
		target.waitForReply(packet);
	}

	ThreadGroup threadGroupForJDI()
	{
		return threadGroupForJDI;
	}
}
