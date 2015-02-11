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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import mono.debugger.protocol.Thread_GetFrameInfo;
import mono.debugger.protocol.Thread_GetId;
import mono.debugger.protocol.Thread_GetName;
import mono.debugger.protocol.Thread_GetState;
import mono.debugger.protocol.Thread_GetTId;
import mono.debugger.request.BreakpointRequest;

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
public class ThreadMirror extends MirrorWithIdAndName
{
	public static interface ThreadState
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

	// This is cached only while this one thread is suspended.  Each time
	// the thread is resumed, we abandon the current cache object and
	// create a new intialized one.
	private static class LocalCache
	{
		Thread_GetState myState = null;
		List<StackFrameMirror> frames = null;
		int framesStart = -1;
		int framesLength = 0;

		boolean triedCurrentContended = false;
	}

	/*
	 * The localCache instance var is set by resetLocalCache to an initialized
	 * object as shown above.  This occurs when the ThreadReference
	 * object is created, and when the mirrored thread is resumed.
	 * The fields are then filled in by the relevant methods as they
	 * are called.  A problem can occur if resetLocalCache is called
	 * (ie, a resume() is executed) at certain points in the execution
	 * of some of these methods - see 6751643.  To avoid this, each
	 * method that wants to use this cache must make a local copy of
	 * this variable and use that.  This means that each invocation of
	 * these methods will use a copy of the cache object that was in
	 * effect at the point that the copy was made; if a racy resume
	 * occurs, it won't affect the method's local copy.  This means that
	 * the values returned by these calls may not match the state of
	 * the debuggee at the time the caller gets the values.  EG,
	 * frameCount() is called and comes up with 5 frames.  But before
	 * it returns this, a resume of the debuggee thread is executed in a
	 * different debugger thread.  The thread is resumed and running at
	 * the time that the value 5 is returned.  Or even worse, the thread
	 * could be suspended again and have a different number of frames, eg, 24,
	 * but this call will still return 5.
	 */
	private LocalCache localCache;

	private void resetLocalCache()
	{
		localCache = new LocalCache();
	}

	ThreadMirror(VirtualMachine aVm, int aRef)
	{
		super(aVm, aRef);
		resetLocalCache();
	}

	@NotNull
	@Override
	public String nameImpl() throws JDWPException
	{
		String threadName = Thread_GetName.process(vm, this).threadName;
		if(threadName.length() == 0)
		{
			return "<empty>";
		}
		return threadName;
	}

	/**
	 * Return a unique identifier for this thread, multiple ThreadMirror objects
	 * may have the same ThreadId because of appdomains.
	 */
	public int threadId()
	{
		try
		{
			return Thread_GetId.process(vm, this).id;
		}
		catch(JDWPException exc)
		{
			throw exc.asUncheckedException();
		}
	}

	/**
	 * Return the system thread id (TID) for this thread, this id is not unique since
	 * a newly started thread might reuse a dead thread's id.
	 */
	public int systemThreadId()
	{
		try
		{
			return Thread_GetTId.process(vm, this).id;
		}
		catch(JDWPException exc)
		{
			throw exc.asUncheckedException();
		}
	}

	private Thread_GetState jdwpStatus()
	{
		LocalCache snapshot = localCache;
		Thread_GetState myState = snapshot.myState;
		try
		{
			if(myState == null)
			{
				myState = Thread_GetState.process(vm, this);
			}
		}
		catch(JDWPException exc)
		{
			throw exc.asUncheckedException();
		}
		return myState;
	}

	public int state()
	{
		return jdwpStatus().state;
	}

	public boolean isSuspended()
	{
		return (jdwpStatus().state & ThreadState.Suspended) != 0;
	}

	public boolean isAtBreakpoint()
	{
		/*
		 * TO DO: This fails to take filters into account.
         */
		try
		{
			StackFrameMirror frame = frame(0);
			Location location = frame.location();
			List<BreakpointRequest> requests = vm.eventRequestManager().breakpointRequests();
			Iterator<BreakpointRequest> iter = requests.iterator();
			while(iter.hasNext())
			{
				BreakpointRequest request = iter.next();
				if(location.equals(request.location()))
				{
					return true;
				}
			}
			return false;
		}
		catch(IndexOutOfBoundsException iobe)
		{
			return false;  // no frames on stack => not at breakpoint
		}
		catch(IncompatibleThreadStateException itse)
		{
			// Per the javadoc, not suspended => return false
			return false;
		}
	}

	public List<StackFrameMirror> frames() throws IncompatibleThreadStateException
	{
		return privateFrames(0, -1);
	}

	public StackFrameMirror frame(int index) throws IncompatibleThreadStateException
	{
		List<StackFrameMirror> list = privateFrames(index, 1);
		return list.get(0);
	}

	/**
	 * Is the requested subrange within what has been retrieved?
	 * local is known to be non-null.  Should only be called from
	 * a sync method.
	 */
	private boolean isSubrange(LocalCache snapshot, int start, int length)
	{
		if(start < snapshot.framesStart)
		{
			return false;
		}
		if(length == -1)
		{
			return (snapshot.framesLength == -1);
		}
		if(snapshot.framesLength == -1)
		{
			if((start + length) > (snapshot.framesStart + snapshot.frames.size()))
			{
				throw new IndexOutOfBoundsException();
			}
			return true;
		}
		return ((start + length) <= (snapshot.framesStart + snapshot.framesLength));
	}

	public List<StackFrameMirror> frames(int start, int length) throws IncompatibleThreadStateException
	{
		if(length < 0)
		{
			throw new IndexOutOfBoundsException("length must be greater than or equal to zero");
		}
		return privateFrames(start, length);
	}

	/**
	 * Private version of frames() allows "-1" to specify all
	 * remaining frames.
	 */
	synchronized private List<StackFrameMirror> privateFrames(int start, int length) throws IncompatibleThreadStateException
	{

		// Lock must be held while creating stack frames so if that two threads
		// do this at the same time, one won't clobber the subset created by the other.
		LocalCache snapshot = localCache;
		try
		{
			if(snapshot.frames == null || !isSubrange(snapshot, start, length))
			{
				Thread_GetFrameInfo.Frame[] jdwpFrames = Thread_GetFrameInfo.process(vm, this, start, length).frames;
				int count = jdwpFrames.length;
				snapshot.frames = new ArrayList<StackFrameMirror>(count);

				for(int i = 0; i < count; i++)
				{
					if(jdwpFrames[i].location == null)
					{
						throw new InternalException("Invalid frame location");
					}
					StackFrameMirror frame = new StackFrameMirror(vm, this, jdwpFrames[i].frameID, jdwpFrames[i].location,
							StackFrameMirror.StackFrameFlags.values()[jdwpFrames[i].flags]);
					// Add to the frame list
					snapshot.frames.add(frame);
				}
				snapshot.framesStart = start;
				snapshot.framesLength = length;
				return Collections.unmodifiableList(snapshot.frames);
			}
			else
			{
				int fromIndex = start - snapshot.framesStart;
				int toIndex;
				if(length == -1)
				{
					toIndex = snapshot.frames.size() - fromIndex;
				}
				else
				{
					toIndex = fromIndex + length;
				}
				return Collections.unmodifiableList(snapshot.frames.subList(fromIndex, toIndex));
			}
		}
		catch(JDWPException exc)
		{
			throw exc.asUncheckedException();
		}
	}
}
