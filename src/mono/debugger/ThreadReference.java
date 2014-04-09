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

/**
 * A thread object from the target VM.
 * A ThreadReference is an {@link ObjectReference} with additional
 * access to thread-specific information from the target VM.
 *
 * @author Robert Field
 * @author Gordon Hirsch
 * @author James McIlree
 * @since 1.3
 */
public interface ThreadReference extends ObjectReference
{
	public interface ThreadState
	{
		int Running = 0x00000000;
		int StopRequested = 0x00000001;
		int SuspendRequested = 0x00000002;
		int Background = 0x00000004;
		int Unstarted = 0x00000008;
		int Stopped = 0x00000010;
		int WaitSleepJoin = 0x00000020;
		int Suspended = 0x00000040;
		int AbortRequested = 0x00000080;
		int Aborted = 0x00000100;
	}

	/**
	 * Returns the name of this thread.
	 *
	 * @return the string containing the thread name.
	 */
	String name();


	/**
	 * @see ThreadState
	 */
	int state();

	/**
	 * Determines whether the thread has been suspended by the
	 * the debugger.
	 *
	 * @return <code>true</code> if the thread is currently suspended;
	 *         <code>false</code> otherwise.
	 */
	boolean isSuspended();

	/**
	 * Determines whether the thread is suspended at a breakpoint.
	 *
	 * @return <code>true</code> if the thread is currently stopped at
	 *         a breakpoint; <code>false</code> otherwise.
	 */
	boolean isAtBreakpoint();


	/**
	 * Returns the number of stack frames in the thread's current
	 * call stack.
	 * The thread must be suspended (normally through an interruption
	 * to the VM) to get this information, and
	 * it is only valid until the thread is resumed again.
	 *
	 * @return an integer frame count
	 * @throws IncompatibleThreadStateException
	 *          if the thread is
	 *          not suspended in the target VM
	 */
	int frameCount() throws IncompatibleThreadStateException;

	/**
	 * Returns a List containing each {@link StackFrame} in the
	 * thread's current call stack.
	 * The thread must be suspended (normally through an interruption
	 * to the VM) to get this information, and
	 * it is only valid until the thread is resumed again.
	 *
	 * @return a List of {@link StackFrame} with the current frame first
	 *         followed by each caller's frame.
	 * @throws IncompatibleThreadStateException
	 *          if the thread is
	 *          not suspended in the target VM
	 */
	List<StackFrame> frames() throws IncompatibleThreadStateException;

	/**
	 * Returns the {@link StackFrame} at the given index in the
	 * thread's current call stack. Index 0 retrieves the current
	 * frame; higher indices retrieve caller frames.
	 * The thread must be suspended (normally through an interruption
	 * to the VM) to get this information, and
	 * it is only valid until the thread is resumed again.
	 *
	 * @param index the desired frame
	 * @return the requested {@link StackFrame}
	 * @throws IncompatibleThreadStateException
	 *          if the thread is
	 *          not suspended in the target VM
	 * @throws java.lang.IndexOutOfBoundsException
	 *          if the index is greater than
	 *          or equal to {@link #frameCount} or is negative.
	 */
	StackFrame frame(int index) throws IncompatibleThreadStateException;

	/**
	 * Returns a List containing a range of {@link StackFrame} mirrors
	 * from the thread's current call stack.
	 * The thread must be suspended (normally through an interruption
	 * to the VM) to get this information, and
	 * it is only valid until the thread is resumed again.
	 *
	 * @param start  the index of the first frame to retrieve.
	 *               Index 0 represents the current frame.
	 * @param length the number of frames to retrieve
	 * @return a List of {@link StackFrame} with the current frame first
	 *         followed by each caller's frame.
	 * @throws IncompatibleThreadStateException
	 *                                   if the thread is
	 *                                   not suspended in the target VM
	 * @throws IndexOutOfBoundsException if the specified range is not
	 *                                   within the range of stack frame indicies.
	 *                                   That is, the exception is thrown if any of the following are true:
	 *                                   <pre>    start &lt; 0
	 *                                      start &gt;= {@link #frameCount}
	 *                                      length &lt; 0
	 *                                      (start+length) &gt; {@link #frameCount}</pre>
	 */
	List<StackFrame> frames(int start, int length) throws IncompatibleThreadStateException;


	/**
	 * Pop stack frames.
	 * <p/>
	 * All frames up to and including the <CODE>frame</CODE> are
	 * popped off the stack.
	 * The frame previous to the parameter <CODE>frame</CODE>
	 * will become the current frame.
	 * <p/>
	 * After this operation, this thread will be
	 * suspended at the invoke instruction of the target method
	 * that created <CODE>frame</CODE>.
	 * The <CODE>frame</CODE>'s method can be reentered with a step into
	 * the instruction.
	 * <p/>
	 * The operand stack is restored, however, any changes
	 * to the arguments that occurred in the called method, remain.
	 * For example, if the method <CODE>foo</CODE>:
	 * <PRE>
	 * void foo(int x) {
	 * System.out.println("Foo: " + x);
	 * x = 4;
	 * System.out.println("pop here");
	 * }
	 * </PRE>
	 * was called with <CODE>foo(7)</CODE> and <CODE>foo</CODE>
	 * is popped at the second <CODE>println</CODE> and resumed,
	 * it will print: <CODE>Foo: 4</CODE>.
	 * <p/>
	 * Locks acquired by a popped frame are released when it
	 * is popped. This applies to synchronized methods that
	 * are popped, and to any synchronized blocks within them.
	 * <p/>
	 * Finally blocks are not executed.
	 * <p/>
	 * No aspect of state, other than this thread's execution point and
	 * locks, is affected by this call.  Specifically, the values of
	 * fields are unchanged, as are external resources such as
	 * I/O streams.  Additionally, the target program might be
	 * placed in a state that is impossible with normal program flow;
	 * for example, order of lock acquisition might be perturbed.
	 * Thus the target program may
	 * proceed differently than the user would expect.
	 * <p/>
	 * The specified thread must be suspended.
	 * <p/>
	 * All <code>StackFrame</code> objects for this thread are
	 * invalidated.
	 * <p/>
	 * No events are generated by this method.
	 * <p/>
	 * None of the frames through and including the frame for the caller
	 * of <i>frame</i> may be native.
	 * <p/>
	 * Not all target virtual machines support this operation.
	 * Use {@link VirtualMachine#canPopFrames() VirtualMachine.canPopFrames()}
	 * to determine if the operation is supported.
	 *
	 * @param frame Stack frame to pop.  <CODE>frame</CODE> is on this
	 *              thread's call stack.
	 * @throws java.lang.UnsupportedOperationException
	 *                                     if
	 *                                     the target virtual machine does not support this
	 *                                     operation - see
	 *                                     {@link VirtualMachine#canPopFrames() VirtualMachine.canPopFrames()}.
	 * @throws IncompatibleThreadStateException
	 *                                     if this
	 *                                     thread is not suspended.
	 * @throws java.lang.IllegalArgumentException
	 *                                     if <CODE>frame</CODE>
	 *                                     is not on this thread's call stack.
	 * @throws NativeMethodException       if one of the frames that would be
	 *                                     popped is that of a native method or if the frame previous to
	 *                                     <i>frame</i> is native.
	 * @throws InvalidStackFrameException  if <CODE>frame</CODE> has become
	 *                                     invalid. Once this thread is resumed, the stack frame is
	 *                                     no longer valid.  This exception is also thrown if there are no
	 *                                     more frames.
	 * @throws VMCannotBeModifiedException if the VirtualMachine is read-only - see {@link VirtualMachine#canBeModified()}.
	 * @since 1.4
	 */
	void popFrames(StackFrame frame) throws IncompatibleThreadStateException;
}
