/*
 * Copyright (c) 1998, 1999, Oracle and/or its affiliates. All rights reserved.
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

package mono.debugger.event;

import mono.debugger.EventSetImpl;
import mono.debugger.JDWP;
import mono.debugger.ObjectValueMirror;
import mono.debugger.VirtualMachine;

/**
 * Notification of an exception in the target VM. When an exception
 * is thrown which satisfies a currently enabled
 * {@link mono.debugger.request.ExceptionRequest exception request},
 * an {@link EventSet event set}
 * containing an instance of this class will be added
 * to the VM's event queue.
 * If the exception is thrown from a non-native method,
 * the exception event is generated at the location where the
 * exception is thrown.
 * If the exception is thrown from a native method, the exception event
 * is generated at the first non-native location reached after the exception
 * is thrown.
 *
 * @author Robert Field
 * @since 1.3
 */
public class ExceptionEvent extends EventSetImpl.LocatableEventImpl implements Event
{
	private ObjectValueMirror exception;

	public ExceptionEvent(VirtualMachine virtualMachine, JDWP.Event.Composite.Events.Exception evt)
	{
		super(virtualMachine, evt, evt.requestID, evt.thread, evt.location);
		this.exception = evt.exception;
	}

	public ObjectValueMirror exception()
	{
		return exception;
	}

	@Override
	public String eventName()
	{
		return "ExceptionEvent";
	}
}
