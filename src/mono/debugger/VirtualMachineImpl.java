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

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import mono.debugger.connect.spi.Connection;
import mono.debugger.event.EventQueue;
import mono.debugger.protocol.AppDomain_GetRootDomain;
import mono.debugger.protocol.VirtualMachine_GetVersion;
import mono.debugger.protocol.VirtualMachine_SetProtocolVersion;
import mono.debugger.request.EventRequestManager;

public class VirtualMachineImpl extends MirrorImpl implements VirtualMachine, ThreadListener
{
	final int sequenceNumber;

	private final TargetVM target;
	private final EventQueueImpl eventQueue;
	private final EventRequestManagerImpl eventRequestManager;
	final VirtualMachineManagerImpl vmManager;
	private final ThreadGroup threadGroupForJDI;

	// Allow direct access to this field so that that tracing code slows down
	// JDI as little as possible when not enabled.
	public int traceFlags = TRACE_ALL;

	static int TRACE_RAW_SENDS = 0x01000000;
	static int TRACE_RAW_RECEIVES = 0x02000000;

	public boolean traceReceives = false;   // pre-compute because of frequency

	// ReferenceType access - updated with class prepare and unload events
	// Protected by "synchronized(this)". "retrievedAllTypes" may be
	// tested unsynchronized (since once true, it stays true), but must
	// be set synchronously
	private Map<Long, ReferenceType> typesByID;


	// ObjectReference cache
	// "objectsByID" protected by "synchronized(this)".
	private final Map<Long, SoftObjectReference> objectsByID = new HashMap<Long, SoftObjectReference>();
	private final ReferenceQueue<ObjectReferenceImpl> referenceQueue = new ReferenceQueue<ObjectReferenceImpl>();
	static private final int DISPOSE_THRESHOLD = 50;
	private final List<SoftObjectReference> batchedDisposeRequests = Collections.synchronizedList(new ArrayList<SoftObjectReference>
			(DISPOSE_THRESHOLD + 10));

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
         * Set up a thread to handle events processed internally
         * the JDI implementation.
         */
		EventQueueImpl internalEventQueue = new EventQueueImpl(this, target);
		new InternalEventHandler(this, internalEventQueue);
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

	/*
	 * ThreadListener implementation
	 */
	@Override
	public boolean threadResumable(ThreadAction action)
	{
		/*
		 * If any thread is resumed, the VM is considered not suspended.
         * Just one thread is being resumed so pass it to thaw.
         */
		state.thaw(action.thread());
		return true;
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
	public List<ReferenceType> findTypes(String className, boolean ignoreCase)
	{
		validateVM();
		//String signature = JNITypeParser.typeNameToSignature(className);
		List<ReferenceType> list = findTypesImpl(className, ignoreCase);

		return Collections.unmodifiableList(list);
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

	private synchronized ReferenceTypeImpl addReferenceType(long id, String signature)
	{
		if(typesByID == null)
		{
			initReferenceTypes();
		}
		ReferenceTypeImpl type = new ClassTypeImpl(vm, id);
        /*
         * If a signature was specified, make sure to set it ASAP, to
         * prevent any needless JDWP command to retrieve it. (for example,
         * typesBySignature.add needs the signature, to maintain proper
         * ordering.
         */
		if(signature != null)
		{
			type.setSignature(signature);
		}

		typesByID.put(new Long(id), type);

		if((vm.traceFlags & VirtualMachine.TRACE_REFTYPES) != 0)
		{
			vm.printTrace("Caching new ReferenceType, sig=" + signature +
					", id=" + id);
		}

		return type;
	}

	synchronized void removeReferenceType(ReferenceType signature)
	{
		if(typesByID == null)
		{
			return;
		}

		if(typesByID.remove(((ReferenceTypeImpl) signature).ref()) != null)
		{
			findTypesImpl(signature.name(), true);
		}
	}

	private void initReferenceTypes()
	{
		typesByID = new HashMap<Long, ReferenceType>(300);
	}

	ReferenceTypeImpl referenceType(long ref)
	{
		return referenceType(ref, null);
	}

	ClassTypeImpl classType(long ref)
	{
		return (ClassTypeImpl) referenceType(ref, null);
	}

	ArrayTypeImpl arrayType(long ref)
	{
		return (ArrayTypeImpl) referenceType(ref, null);
	}

	ReferenceTypeImpl referenceType(long id, String signature)
	{
		if((vm.traceFlags & VirtualMachine.TRACE_REFTYPES) != 0)
		{
			StringBuilder sb = new StringBuilder();
			sb.append("Looking up ");
			sb.append("Class");

			if(signature != null)
			{
				sb.append(", signature='").append(signature).append("'");
			}
			sb.append(", id=").append(id);
			vm.printTrace(sb.toString());
		}
		if(id == 0)
		{
			return null;
		}
		else
		{
			ReferenceTypeImpl retType = null;
			synchronized(this)
			{
				if(typesByID != null)
				{
					retType = (ReferenceTypeImpl) typesByID.get(new Long(id));
				}
				if(retType == null)
				{
					retType = addReferenceType(id, signature);
				}
			}
			return retType;
		}
	}

	private List<ReferenceType> findTypesImpl(String name, boolean ignoreCase)
	{
		if((vm.traceFlags & VirtualMachine.TRACE_REFTYPES) != 0)
		{
			vm.printTrace("Retrieving matching ReferenceTypes, name=" + name);
		}
		JDWP.VirtualMachine.GetTypes.ClassInfo[] cinfos;
		try
		{
			cinfos = JDWP.VirtualMachine.GetTypes.process(vm, name, ignoreCase).classes;
		}
		catch(JDWPException exc)
		{
			throw exc.toJDIException();
		}

		int count = cinfos.length;
		List<ReferenceType> list = new ArrayList<ReferenceType>(count);

		// Hold lock during processing to improve performance
		synchronized(this)
		{
			for(int i = 0; i < count; i++)
			{
				JDWP.VirtualMachine.GetTypes.ClassInfo ci = cinfos[i];
				ReferenceTypeImpl type = referenceType(ci.typeID, name);
				list.add(type);
			}
		}
		return list;
	}

	void sendToTarget(Packet packet)
	{
		target.send(packet);
	}

	void waitForTargetReply(Packet packet)
	{
		target.waitForReply(packet);
        /*
         * If any object disposes have been batched up, send them now.
         */
		processBatchedDisposes();
	}

	Type findBootType(String signature) throws ClassNotLoadedException
	{
		throw new IllegalArgumentException(signature);
	}

	private void processBatchedDisposes()
	{
		if(shutdown)
		{
			return;
		}

	}

	private void batchForDispose(SoftObjectReference ref)
	{
		if((traceFlags & TRACE_OBJREFS) != 0)
		{
			printTrace("Batching object " + ref.key().longValue() +
					" for dispose (ref count = " + ref.count() + ")");
		}
		batchedDisposeRequests.add(ref);
	}

	private void processQueue()
	{
		Reference<?> ref;
		//if ((traceFlags & TRACE_OBJREFS) != 0) {
		//    printTrace("Checking for softly reachable objects");
		//}
		while((ref = referenceQueue.poll()) != null)
		{
			SoftObjectReference softRef = (SoftObjectReference) ref;
			removeObjectMirror(softRef);
			batchForDispose(softRef);
		}
	}

	synchronized ObjectReferenceImpl objectMirror(long id, int tag)
	{

		// Handle any queue elements that are not strongly reachable
		processQueue();

		if(id == 0)
		{
			return null;
		}

		ObjectReferenceImpl object = null;
		Long key = new Long(id);

        /*
         * Attempt to retrieve an existing object object reference
         */
		Map<Long, SoftObjectReference> map = tagToMap(tag);

		SoftObjectReference ref = map.get(key);
		if(ref != null)
		{
			object = ref.object();
		}

        /*
         * If the object wasn't in the table, or it's soft reference was
         * cleared, create a new instance.
         */
		if(object == null)
		{
			switch(tag)
			{
				case JDWP.Tag.OBJECT:
					object = new ObjectReferenceImpl(vm, id);
					break;
				case JDWP.Tag.STRING:
					object = new StringReferenceImpl(vm, id);
					break;
				case JDWP.Tag.ARRAY:
					object = new ArrayReferenceImpl(vm, id);
					break;
				case JDWP.Tag.THREAD:
					ThreadMirror thread = new ThreadMirror(vm, id);
					thread.addListener(this);
					object = thread;
					break;
				case JDWP.Tag.CLASS_OBJECT:
					object = new ClassObjectReferenceImpl(vm, id);
					break;
				default:
					throw new IllegalArgumentException("Invalid object tag: " + tag);
			}
			ref = new SoftObjectReference(key, object, referenceQueue);

            /*
             * If there was no previous entry in the table, we add one here
             * If the previous entry was cleared, we replace it here.
             */
			map.put(key, ref);
			if((traceFlags & TRACE_OBJREFS) != 0)
			{
				printTrace("Creating new " + object.getClass().getName() + " (id = " + id + ")");
			}
		}
		else
		{
			ref.incrementCount();
		}

		return object;
	}

	synchronized void removeObjectMirror(ObjectReferenceImpl object)
	{

		// Handle any queue elements that are not strongly reachable
		processQueue();

		Map<Long, SoftObjectReference> map = tagToMap(object.typeValueKey());

		SoftObjectReference ref = map.remove(new Long(object.ref()));
		if(ref != null)
		{
			batchForDispose(ref);
		}
		else
		{
            /*
             * If there's a live ObjectReference about, it better be part
             * of the cache.
             */
			throw new InternalException("ObjectReference " + object.ref() + " not found in object cache");
		}
	}

	Map<Long, SoftObjectReference> tagToMap(int tag)
	{
		Map<Long, SoftObjectReference> map;
		switch(tag)
		{
			default:
				map = objectsByID;
				break;
		}
		return map;
	}

	synchronized void removeObjectMirror(SoftObjectReference ref)
	{
        /*
         * This will remove the soft reference if it has not been
         * replaced in the cache.
         */
		objectsByID.remove(ref.key());
	}

	ObjectReferenceImpl objectMirror(long id)
	{
		return objectMirror(id, JDWP.Tag.OBJECT);
	}

	StringReferenceImpl stringMirror(long id)
	{
		return (StringReferenceImpl) objectMirror(id, JDWP.Tag.STRING);
	}

	ArrayReferenceImpl arrayMirror(long id)
	{
		return (ArrayReferenceImpl) objectMirror(id, JDWP.Tag.ARRAY);
	}

	ThreadMirror threadMirror(long id)
	{
		return (ThreadMirror) objectMirror(id, JDWP.Tag.THREAD);
	}

	ClassObjectReferenceImpl classObjectMirror(long id)
	{
		return (ClassObjectReferenceImpl) objectMirror(id, JDWP.Tag.CLASS_OBJECT);
	}

	ThreadGroup threadGroupForJDI()
	{
		return threadGroupForJDI;
	}

	static private class SoftObjectReference extends SoftReference<ObjectReferenceImpl>
	{
		int count;
		Long key;

		SoftObjectReference(
				Long key, ObjectReferenceImpl mirror, ReferenceQueue<ObjectReferenceImpl> queue)
		{
			super(mirror, queue);
			this.count = 1;
			this.key = key;
		}

		int count()
		{
			return count;
		}

		void incrementCount()
		{
			count++;
		}

		Long key()
		{
			return key;
		}

		ObjectReferenceImpl object()
		{
			return get();
		}
	}
}
