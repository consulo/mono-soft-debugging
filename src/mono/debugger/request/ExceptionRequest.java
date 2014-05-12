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
import mono.debugger.TypeMirror;
import mono.debugger.VirtualMachineImpl;

/**
 * Request for notification when an exception occurs in the target VM.
 * When an enabled ExceptionRequest is satisfied, an
 * {@link mono.debugger.event.EventSet event set} containing an
 * {@link mono.debugger.event.ExceptionEvent ExceptionEvent} will be placed
 * on the {@link mono.debugger.event.EventQueue EventQueue}.
 * The collection of existing ExceptionRequests is
 * managed by the {@link EventRequestManager}
 *
 * @author Robert Field
 * @see mono.debugger.event.ExceptionEvent
 * @see mono.debugger.event.EventQueue
 * @see EventRequestManager
 * @since 1.3
 */
public class ExceptionRequest extends ClassVisibleEventRequest
{
	TypeMirror exception = null;
	boolean caught = true;
	boolean uncaught = true;

	public ExceptionRequest(
			TypeMirror refType, boolean notifyCaught, boolean notifyUncaught, VirtualMachineImpl vm, EventRequestManagerImpl requestManager)
	{
		super(vm, requestManager);
		exception = refType;
		caught = notifyCaught;
		uncaught = notifyUncaught;
		  /*  {
				ReferenceTypeImpl exc;
                if (exception == null) {
                    exc = new ClassTypeImpl(vm, 0);
                } else {
                    exc = (ReferenceTypeImpl)exception;
                }
                filters.add(JDWP.EventRequest.Set.Modifier.ExceptionOnly.
                            create(exc, caught, uncaught));
            }  */
	}

	public TypeMirror exception()
	{
		return exception;
	}

	public boolean notifyCaught()
	{
		return caught;
	}

	public boolean notifyUncaught()
	{
		return uncaught;
	}

	@Override
	public EventKind eventCmd()
	{
		return EventKind.EXCEPTION;
	}

	@Override
	public String toString()
	{
		return "exception request " + exception() + state();
	}

	@Override
	public <A, R> R visit(@NotNull EventRequestVisitor<A, R> visitor, A a)
	{
		return visitor.visitException(this, a);
	}
}