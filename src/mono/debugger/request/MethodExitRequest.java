/*
 * Copyright (c) 1998, 2001, Oracle and/or its affiliates. All rights reserved.
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

package mono.debugger.request;

import org.jetbrains.annotations.NotNull;
import mono.debugger.EventKind;
import mono.debugger.EventRequestManagerImpl;
import mono.debugger.VirtualMachine;

/**
 * Request for notification when a method returns in the target VM.
 * When an enabled MethodExitRequest is hit, an
 * {@link mono.debugger.event.EventSet event set} containing a
 * {@link mono.debugger.event.MethodExitEvent MethodExitEvent}
 * will be placed on the
 * {@link mono.debugger.event.EventQueue EventQueue}.
 * The collection of existing MethodExitRequests is
 * managed by the {@link EventRequestManager}
 *
 * @see mono.debugger.event.MethodExitEvent
 * @see mono.debugger.event.EventQueue
 * @see EventRequestManager
 *
 * @author Robert Field
 * @since  1.3
 */
public class MethodExitRequest extends ClassVisibleEventRequest
{
	public MethodExitRequest(VirtualMachine virtualMachine, EventRequestManagerImpl requestManager)
	{
		super(virtualMachine, requestManager);
	}

	@Override
	public EventKind eventCmd()
	{
		return EventKind.METHOD_EXIT;
	}

	@Override
	public String toString()
	{
		return "method exit request " + state();
	}

	@Override
	public <A, R> R visit(@NotNull EventRequestVisitor<A, R> visitor, A a)
	{
		return visitor.visitMethodExit(this, a);
	}
}