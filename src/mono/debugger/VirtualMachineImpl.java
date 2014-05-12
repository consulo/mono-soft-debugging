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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import mono.debugger.connect.spi.Connection;
import mono.debugger.event.EventQueue;
import mono.debugger.protocol.AppDomain_GetRootDomain;
import mono.debugger.protocol.VirtualMachine_GetTypes;
import mono.debugger.protocol.VirtualMachine_GetTypesForSourceFile;
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
	public int traceFlags = 0;

	static int TRACE_RAW_SENDS = 0x01000000;
	static int TRACE_RAW_RECEIVES = 0x02000000;

	public boolean traceReceives = false;   // pre-compute because of frequency

	private final AppDomainMirror myRootAppDomain;

	// These are cached once for the life of the VM
	private final VirtualMachine_GetVersion myVersionInfo;

	// Launched debuggee process
	private Process process;

	// coordinates state changes and corresponding listener notifications
	private VMState state = new VMState(this);

	private final Object initMonitor = new Object();
	private boolean initComplete = false;

	private Map<Integer, TypeMirror> myTypeMirrorCache = new HashMap<Integer, TypeMirror>();
	private Map<Integer, MethodMirror> myMethodMirrorCache = new HashMap<Integer, MethodMirror>();
	private Map<Integer, AssemblyMirror> myAssemblyMirrorCache = new HashMap<Integer, AssemblyMirror>();


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
			myVersionInfo = VirtualMachine_GetVersion.process(vm);

			if(myVersionInfo.jdwpMajor != vmManager.majorInterfaceVersion())
			{
				throw new IllegalArgumentException("Virtual Machine major version is not equal client: " + myVersionInfo.jdwpMajor);
			}

			VirtualMachine_SetProtocolVersion.process(vm, vmManager.majorInterfaceVersion(), vmManager.minorInterfaceVersion());

			myRootAppDomain = AppDomain_GetRootDomain.process(vm).myAppDomainMirror;
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

	@NotNull
	@Override
	public AppDomainMirror rootAppDomain()
	{
		return myRootAppDomain;
	}

	@NotNull
	@Override
	public TypeMirror[] findTypesByQualifiedName(String typeName, boolean ignoreCase)
	{
		checkVersion(2, 9);
		try
		{
			return VirtualMachine_GetTypes.process(vm, typeName, ignoreCase).types;
		}
		catch(JDWPException exc)
		{
			throw exc.toJDIException();
		}
	}

	@NotNull
	@Override
	public TypeMirror[] findTypesBySourcePath(String sourcePath, boolean ignoreCase)
	{
		checkVersion(2, 7);
		try
		{
			return VirtualMachine_GetTypesForSourceFile.process(vm, sourcePath, ignoreCase).types;
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
		return eventRequestManager;
	}

	EventRequestManagerImpl eventRequestManagerImpl()
	{
		return eventRequestManager;
	}

	@Override
	public void dispose()
	{
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
		return process;
	}

	@Override
	public boolean isAtLeastVersion(int major, int minor)
	{
		return (myVersionInfo.jdwpMajor > major) || ((myVersionInfo.jdwpMajor == major && myVersionInfo.jdwpMinor >= minor));
	}

	@Override
	public void enableEvents(@NotNull EventKind... eventKinds)
	{
		for(EventKind eventKind : eventKinds)
		{
			try
			{
				JDWP.EventRequest.Set.process(vm, eventKind.ordinal(), SuspendPolicy.NONE.ordinal(), new JDWP.EventRequest.Set.Modifier[0]);
			}
			catch(JDWPException e)
			{
				throw e.toJDIException();
			}
		}
	}

	@NotNull
	@Override
	public String version()
	{
		return myVersionInfo.jdwpMajor + "." + myVersionInfo.jdwpMinor;
	}

	@NotNull
	@Override
	public String name()
	{
		return myVersionInfo.description;
	}

	public void checkVersion(int major, int minor)
	{
		if(!isAtLeastVersion(major, minor))
		{
			throw new VersionMismatchException();
		}
	}

	@NotNull
	public TypeMirror getOrCreateTypeMirror(int id)
	{
		TypeMirror typeMirror = myTypeMirrorCache.get(id);
		if(typeMirror == null)
		{
			myTypeMirrorCache.put(id, typeMirror = new TypeMirror(vm, id));
		}
		return typeMirror;
	}

	@NotNull
	public MethodMirror getOrCreateMethodMirror(int id)
	{
		MethodMirror methodMirror = myMethodMirrorCache.get(id);
		if(methodMirror == null)
		{
			myMethodMirrorCache.put(id, methodMirror = new MethodMirror(vm, id));
		}
		return methodMirror;
	}

	@NotNull
	public AssemblyMirror getOrCreateAssemblyMirror(int id)
	{
		AssemblyMirror assemblyMirror = myAssemblyMirrorCache.get(id);
		if(assemblyMirror == null)
		{
			myAssemblyMirrorCache.put(id, assemblyMirror = new AssemblyMirror(vm, id));
		}
		return assemblyMirror;
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
