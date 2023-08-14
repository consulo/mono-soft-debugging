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

import mono.debugger.protocol.*;
import mono.debugger.util.BitUtil;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

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

	ThreadMirror(VirtualMachine aVm, int aRef)
	{
		super(aVm, aRef);
	}

	@Nonnull
	@Override
	public String nameImpl() throws JDWPException
	{
		String threadName = Thread_GetName.process(vm, this).threadName;
		if(threadName.length() == 0)
		{
			return "";
		}
		return threadName;
	}

	/**
	 * Return a unique identifier for this thread, multiple ThreadMirror objects
	 * may have the same ThreadId because of appdomains.
	 */
	public long threadId()
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
	public long systemThreadId()
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

	private Thread_GetState status()
	{
		try
		{
			return Thread_GetState.process(vm, this);
		}
		catch(JDWPException e)
		{
			throw e.asUncheckedException();
		}
	}

	public int state()
	{
		return status().state;
	}

	public boolean isSuspended()
	{
		return (status().state & ThreadState.Suspended) != 0;
	}

	@Nonnull
	public List<StackFrameMirror> frames()
	{
		return frames(0, -1);
	}

	@Nonnull
	public List<StackFrameMirror> frames(int startIndex, int length)
	{
		try
		{
			Thread_GetFrameInfo.Frame[] frames = Thread_GetFrameInfo.process(vm, this, startIndex, length).frames;
			List<StackFrameMirror> frameMirrors = new ArrayList<>(frames.length);
			for(Thread_GetFrameInfo.Frame frame : frames)
			{
				if(frame.location == null)
				{
					throw new InternalException("Invalid frame location");
				}

				List<StackFrameMirror.StackFrameFlags> result = new ArrayList<>();
				byte flags = frame.flags;
				for(StackFrameMirror.StackFrameFlags f : StackFrameMirror.StackFrameFlags.values())
				{
					if(BitUtil.isSet(flags, f.mask))
					{
						result.add(f);
					}
				}

				frameMirrors.add(new StackFrameMirror(vm, this, frame.frameID, frame.location, EnumSet.copyOf(result)));
			}
			return frameMirrors;

		}
		catch(JDWPException e)
		{
			throw e.asUncheckedException();
		}
	}
}
