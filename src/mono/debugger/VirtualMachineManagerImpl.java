/*
 * Copyright (c) 1998, 2008, Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import mono.debugger.connect.spi.Connection;

/* Public for use by mono.debugger.Bootstrap */
public class VirtualMachineManagerImpl implements VirtualMachineManager
{
	private static final int MAJOR_VERSION = 2;
	private static final int MINOR_VERSION = 29;

	private List<VirtualMachine> targets = new ArrayList<VirtualMachine>();
	private final ThreadGroup mainGroupForJDI;
	private ResourceBundle messages = null;
	private int vmSequenceNumber = 0;

	private static final Object lock = new Object();
	private static VirtualMachineManagerImpl vmm;

	public static VirtualMachineManager virtualMachineManager()
	{
		SecurityManager sm = System.getSecurityManager();
		if(sm != null)
		{
			JDIPermission vmmPermission = new JDIPermission("virtualMachineManager");
			sm.checkPermission(vmmPermission);
		}
		synchronized(lock)
		{
			if(vmm == null)
			{
				vmm = new VirtualMachineManagerImpl();
			}
		}
		return vmm;
	}

	protected VirtualMachineManagerImpl()
	{

        /*
		 * Create a top-level thread group
         */
		ThreadGroup top = Thread.currentThread().getThreadGroup();
		ThreadGroup parent = null;
		while((parent = top.getParent()) != null)
		{
			top = parent;
		}
		mainGroupForJDI = new ThreadGroup(top, "Mono Soft Debugger main");
	}

	@Override
	public List<VirtualMachine> connectedVirtualMachines()
	{
		return Collections.unmodifiableList(targets);
	}

	@Override
	public synchronized VirtualMachine createVirtualMachine(
			Connection connection, Process process) throws IOException
	{

		if(!connection.isOpen())
		{
			throw new IllegalStateException("connection is not open");
		}

		VirtualMachine vm;
		try
		{
			vm = new VirtualMachineImpl(this, connection, process, ++vmSequenceNumber);
		}
		catch(VMDisconnectedException e)
		{
			throw new IOException(e.getMessage());
		}
		targets.add(vm);
		return vm;
	}

	@Override
	public VirtualMachine createVirtualMachine(Connection connection) throws IOException
	{
		return createVirtualMachine(connection, null);
	}

	public void addVirtualMachine(VirtualMachine vm)
	{
		targets.add(vm);
	}

	void disposeVirtualMachine(VirtualMachine vm)
	{
		targets.remove(vm);
	}

	@Override
	public int majorInterfaceVersion()
	{
		return MAJOR_VERSION;
	}

	@Override
	public int minorInterfaceVersion()
	{
		return MINOR_VERSION;
	}

	ThreadGroup mainGroupForJDI()
	{
		return mainGroupForJDI;
	}

	String getString(String key)
	{
		if(messages == null)
		{
			messages = ResourceBundle.getBundle("resources.jdi");
		}
		return messages.getString(key);
	}
}
