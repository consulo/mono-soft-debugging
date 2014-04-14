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

import java.util.List;

import org.jetbrains.annotations.NotNull;
import mono.debugger.event.EventQueue;
import mono.debugger.request.EventRequestManager;

/**
 * A virtual machine targeted for debugging.
 * More precisely, a {@link Mirror mirror} representing the
 * composite state of the target VM.
 * All other mirrors are associated with an instance of this
 * interface.  Access to all other mirrors is achieved
 * directly or indirectly through an instance of this
 * interface.
 * Access to global VM properties and control of VM execution
 * are supported directly by this interface.
 * <p/>
 * Instances of this interface are created by instances of
 * {@link mono.debugger.connect.Connector}. For example,
 * an {@link mono.debugger.connect.AttachingConnector AttachingConnector}
 * attaches to a target VM and returns its virtual machine mirror.
 * A Connector will typically create a VirtualMachine by invoking
 * the VirtualMachineManager's {@link
 * mono.debugger.VirtualMachineManager#createVirtualMachine(Connection)}
 * createVirtualMachine(Connection) method.
 * <p/>
 * Note that a target VM launched by a launching connector is not
 * guaranteed to be stable until after the {@link mono.debugger.event.VMStartEvent} has been
 * received.
 * <p/>
 * Any method on <code>VirtualMachine</code> which
 * takes <code>VirtualMachine</code> as an parameter may throw
 * {@link mono.debugger.VMDisconnectedException} if the target VM is
 * disconnected and the {@link mono.debugger.event.VMDisconnectEvent} has been or is
 * available to be read from the {@link mono.debugger.event.EventQueue}.
 * <p/>
 * Any method on <code>VirtualMachine</code> which
 * takes <code>VirtualMachine</code> as an parameter may throw
 * {@link mono.debugger.VMOutOfMemoryException} if the target VM has run out of memory.
 *
 * @author Robert Field
 * @author Gordon Hirsch
 * @author James McIlree
 * @since 1.3
 */
public interface VirtualMachine extends Mirror
{
	/**
	 * All tracing is disabled.
	 */
	int TRACE_NONE = 0x00000000;
	/**
	 * Tracing enabled for JDWP packets sent to target VM.
	 */
	int TRACE_SENDS = 0x00000001;
	/**
	 * Tracing enabled for JDWP packets received from target VM.
	 */
	int TRACE_RECEIVES = 0x00000002;
	/**
	 * Tracing enabled for internal event handling.
	 */
	int TRACE_EVENTS = 0x00000004;
	/**
	 * Tracing enabled for internal managment of reference types.
	 */
	int TRACE_REFTYPES = 0x00000008;
	/**
	 * Tracing enabled for internal management of object references.
	 */
	int TRACE_OBJREFS = 0x00000010;
	/**
	 * All tracing is enabled.
	 */
	int TRACE_ALL = 0x00ffffff;

	int MAJOR_VERSION = 2;
	int MINOR_VERSION = 29;

	/**
	 * Traces the activities performed by the mono.debugger implementation.
	 * All trace information is output to System.err. The given trace
	 * flags are used to limit the output to only the information
	 * desired. The given flags are in effect and the corresponding
	 * trace will continue until the next call to
	 * this method.
	 * <p/>
	 * Output is implementation dependent and trace mode may be ignored.
	 *
	 * @param traceFlags identifies which kinds of tracing to enable.
	 */
	void setDebugTraceMode(int traceFlags);

	@NotNull
	AppDomainMirror rootAppDomain();

	/**
	 * Returns the loaded reference types that
	 * match a given name. The name must be fully qualified
	 * (for example, java.lang.String). The returned list
	 * will contain a {@link ReferenceType} for each class
	 * or interface found with the given name. The search
	 * is confined to loaded classes only; no attempt is made
	 * to load a class of the given name.
	 * <p/>
	 * The returned list will include reference types
	 * loaded at least to the point of preparation and
	 * types (like array) for which preparation is
	 * not defined.
	 *
	 * @param className  the class/interface name to search for
	 * @param ignoreCase
	 * @return a list of {@link ReferenceType} objects, each
	 *         mirroring a type in the target VM with the given name.
	 */
	@NotNull
	TypeMirror[] findTypesByQualifiedName(String typeName, boolean ignoreCase);

	@NotNull
	TypeMirror[] findTypesBySourcePath(String sourcePath, boolean ignoreCase);

	/**
	 * Returns a list of the currently running threads. For each
	 * running thread in the target VM, a {@link ThreadMirror}
	 * that mirrors it is placed in the list.
	 * The returned list contains threads created through
	 * java.lang.Thread, all native threads attached to
	 * the target VM through JNI, and system threads created
	 * by the target VM. Thread objects that have
	 * not yet been started
	 * (see {@link java.lang.Thread#start Thread.start()})
	 * and thread objects that have
	 * completed their execution are not included in the returned list.
	 *
	 * @return a list of {@link ThreadMirror} objects, one for each
	 *         running thread in the mirrored VM.
	 */
	@NotNull
	List<ThreadMirror> allThreads();

	/**
	 * Suspends the execution of the application running in this
	 * virtual machine. All threads currently running will be suspended.
	 * <p/>
	 * Unlike {@link java.lang.Thread#suspend Thread.suspend()},
	 * suspends of both the virtual machine and individual threads are
	 * counted. Before a thread will run again, it must be resumed
	 * (through {@link #resume} or {@link ThreadMirror#resume})
	 * the same number of times it has been suspended.
	 *
	 * @throws VMCannotBeModifiedException if the VirtualMachine is read-only - see {@link VirtualMachine#canBeModified()}.
	 */
	void suspend();

	/**
	 * Continues the execution of the application running in this
	 * virtual machine. All threads are resumed as documented in
	 * {@link ThreadMirror#resume}.
	 *
	 * @throws VMCannotBeModifiedException if the VirtualMachine is read-only - see {@link VirtualMachine#canBeModified()}.
	 * @see #suspend
	 */
	void resume();

	/**
	 * Returns the event queue for this virtual machine.
	 * A virtual machine has only one {@link EventQueue} object, this
	 * method will return the same instance each time it
	 * is invoked.
	 *
	 * @return the {@link EventQueue} for this virtual machine.
	 * @throws VMCannotBeModifiedException if the VirtualMachine is read-only - see {@link VirtualMachine#canBeModified()}.
	 */
	@NotNull
	EventQueue eventQueue();

	/**
	 * Returns the event request manager for this virtual machine.
	 * The {@link EventRequestManager} controls user settable events
	 * such as breakpoints.
	 * A virtual machine has only one {@link EventRequestManager} object,
	 * this method will return the same instance each time it
	 * is invoked.
	 *
	 * @return the {@link EventRequestManager} for this virtual machine.
	 * @throws VMCannotBeModifiedException if the VirtualMachine is read-only - see {@link VirtualMachine#canBeModified()}.
	 */
	@NotNull
	EventRequestManager eventRequestManager();

	/**
	 * Returns the {@link java.lang.Process} object for this
	 * virtual machine if launched
	 * by a {@link mono.debugger.connect.LaunchingConnector}
	 *
	 * @return the {@link java.lang.Process} object for this virtual
	 *         machine, or null if it was not launched by a
	 *         {@link mono.debugger.connect.LaunchingConnector}.
	 * @throws VMCannotBeModifiedException if the VirtualMachine is read-only
	 *                                     -see {@link VirtualMachine#canBeModified()}.
	 */
	@NotNull
	Process process();

	/**
	 * Invalidates this virtual machine mirror.
	 * The communication channel to the target VM is closed, and
	 * the target VM prepares to accept another subsequent connection
	 * from this debugger or another debugger, including the
	 * following tasks:
	 * <ul>
	 * <li>All event requests are cancelled.
	 * <li>All threads suspended by {@link #suspend} or by
	 * {@link ThreadMirror#suspend} are resumed as many
	 * times as necessary for them to run.
	 * <li>Garbage collection is re-enabled in all cases where it was
	 * disabled through {@link ObjectReference#disableCollection}.
	 * </ul>
	 * Any current method invocations executing in the target VM
	 * are continued after the disconnection. Upon completion of any such
	 * method invocation, the invoking thread continues from the
	 * location where it was originally stopped.
	 * <p/>
	 * Resources originating in
	 * this VirtualMachine (ObjectReferences, ReferenceTypes, etc.)
	 * will become invalid.
	 */
	void dispose();

	/**
	 * Causes the mirrored VM to terminate with the given error code.
	 * All resources associated with this VirtualMachine are freed.
	 * If the mirrored VM is remote, the communication channel
	 * to it will be closed. Resources originating in
	 * this VirtualMachine (ObjectReferences, ReferenceTypes, etc.)
	 * will become invalid.
	 * <p/>
	 * Threads running in the mirrored VM are abruptly terminated.
	 * A thread death exception is not thrown and
	 * finally blocks are not run.
	 *
	 * @param exitCode the exit code for the target VM.  On some platforms,
	 *                 the exit code might be truncated, for example, to the lower order 8 bits.
	 * @throws VMCannotBeModifiedException if the VirtualMachine is read-only - see {@link VirtualMachine#canBeModified()}.
	 */
	void exit(int exitCode);

	/**
	 * Returns the version of the Java Runtime Environment in the target
	 * VM as reported by the property <code>java.version</code>.
	 * For obtaining the JDI interface version, use
	 * {@link VirtualMachineManager#majorInterfaceVersion}
	 * and {@link VirtualMachineManager#minorInterfaceVersion}
	 *
	 * @return the target VM version.
	 */
	@NotNull
	String version();

	/**
	 * Returns the name of the target VM as reported by the
	 * property <code>java.vm.name</code>.
	 *
	 * @return the target VM name.
	 */
	@NotNull
	String name();
}
