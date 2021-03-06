/*
 * Copyright (c) 2001, Oracle and/or its affiliates. All rights reserved.
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

import mono.debugger.EventKind;
import mono.debugger.EventRequestManagerImpl;
import mono.debugger.VirtualMachine;

/**
 * Request for notification when the target VM terminates.
 * When an enabled VMDeathRequest is satisfied, an
 * {@link mono.debugger.event.EventSet event set} containing a
 * {@link mono.debugger.event.VMDeathEvent VMDeathEvent}
 * will be placed on the
 * {@link mono.debugger.event.EventQueue EventQueue}.
 * The collection of existing VMDeathRequests is
 * managed by the {@link EventRequestManager}
 * <P>
 * Even without creating a VMDeathRequest, a single
 * unsolicited VMDeathEvent will be sent with a
 * {@link EventRequest#suspendPolicy() suspend policy}
 * of {@link EventRequest#SUSPEND_NONE SUSPEND_NONE}.
 * This request would typically be created so that a
 * VMDeathEvent with a suspend policy of
 * {@link EventRequest#SUSPEND_ALL SUSPEND_ALL}
 * will be sent.  This event can be used to assure
 * completion of any processing which requires the VM
 * to be alive (e.g. event processing).  Note: the
 * unsolicited VMDeathEvent will still be sent.
 *
 * @see mono.debugger.event.VMDeathEvent
 * @see mono.debugger.event.EventQueue
 * @see EventRequestManager
 *
 * @author Robert Field
 * @since  1.4
 */
public class VMDeathRequest extends EventRequest
{
	public VMDeathRequest(VirtualMachine virtualMachine, EventRequestManagerImpl requestManager)
	{
		super(virtualMachine, requestManager);
	}

	@Override
	public EventKind eventCmd()
	{
		return EventKind.VM_DEATH;
	}

	@Override
	public String toString()
	{
		return "VM death request " + state();
	}
}