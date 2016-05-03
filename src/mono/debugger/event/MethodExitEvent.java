/*
 * Copyright (c) 1998, 2004, Oracle and/or its affiliates. All rights reserved.
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

import org.jetbrains.annotations.NotNull;
import mono.debugger.EventSetImpl;
import mono.debugger.JDWP;
import mono.debugger.MethodMirror;
import mono.debugger.VirtualMachine;

/**
 * Notification of a method return in the target VM. This event
 * is generated after all code in the method has executed, but the
 * location of this event is the last executed location in the method.
 * Method exit events are generated for both native and non-native
 * methods. Method exit events are not generated if the method terminates
 * with a thrown exception.
 *
 * @see EventQueue
 *
 * @author Robert Field
 * @since  1.3
 */
public class MethodExitEvent extends EventSetImpl.ThreadedEventImpl
{
	private MethodMirror myMethodMirror;

	public MethodExitEvent(VirtualMachine virtualMachine, JDWP.Event.Composite.Events.MethodExit evt)
	{
		super(virtualMachine, evt, evt.requestID, evt.thread);
		myMethodMirror = evt.method;
	}

	@NotNull
	public MethodMirror method()
	{
		return myMethodMirror;
	}
	@Override
	public String eventName()
	{
		return "MethodExitEvent";
	}
}