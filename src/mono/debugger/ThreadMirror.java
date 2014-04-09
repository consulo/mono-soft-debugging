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
public interface ThreadMirror extends ObjectReference
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
}
