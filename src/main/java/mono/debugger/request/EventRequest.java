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

package mono.debugger.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import mono.debugger.EventKind;
import mono.debugger.EventRequestManagerImpl;
import mono.debugger.JDWP;
import mono.debugger.JDWPException;
import mono.debugger.MirrorImpl;
import mono.debugger.SuspendPolicy;
import mono.debugger.VirtualMachine;

public abstract class EventRequest extends MirrorImpl
{

	private final EventRequestManagerImpl myRequestManager;
	private int id;

	/*
	 * This list is not protected by a synchronized wrapper. All
	 * access/modification should be protected by synchronizing on
	 * the enclosing instance of EventRequestImpl.
	 */
	public List<JDWP.EventRequest.Set.Modifier> filters = new ArrayList<JDWP.EventRequest.Set.Modifier>();

	boolean isEnabled = false;
	protected boolean deleted = false;
	protected SuspendPolicy suspendPolicy = SuspendPolicy.ALL;
	private Map<Object, Object> clientProperties = null;

	public EventRequest(VirtualMachine virtualMachine, EventRequestManagerImpl requestManager)
	{
		super(virtualMachine);
		myRequestManager = requestManager;
	}

	public int id()
	{
		return id;
	}

	/*
	 * Override superclass back to default equality
	 */
	@Override
	public boolean equals(Object obj)
	{
		return this == obj;
	}

	@Override
	public int hashCode()
	{
		return System.identityHashCode(this);
	}

	public abstract EventKind eventCmd();

	protected InvalidRequestStateException invalidState()
	{
		return new InvalidRequestStateException(toString());
	}

	public String state()
	{
		return deleted ? " (deleted)" : (isEnabled() ? " (enabled)" : " (disabled)");
	}

	/**
	 * @return all the event request of this kind
	 */
	public List requestList()
	{
		return myRequestManager.requestList(eventCmd());
	}

	/**
	 * delete the event request
	 */
	public void delete()
	{
		if(!deleted)
		{
			requestList().remove(this);
			disable(); /* must do BEFORE delete */
			deleted = true;
		}
	}

	public boolean isEnabled()
	{
		return isEnabled;
	}

	public void enable()
	{
		setEnabled(true);
	}

	public void disable()
	{
		setEnabled(false);
	}

	public synchronized void setEnabled(boolean val)
	{
		if(deleted)
		{
			throw invalidState();
		}
		else
		{
			if(val != isEnabled)
			{
				if(isEnabled)
				{
					clear();
				}
				else
				{
					set();
				}
			}
		}
	}

	public synchronized void addCountFilter(int count)
	{
		if(isEnabled() || deleted)
		{
			throw invalidState();
		}
		if(count < 1)
		{
			throw new IllegalArgumentException("count is less than one");
		}
		filters.add(JDWP.EventRequest.Set.Modifier.Count.create(count));
	}

	public void setSuspendPolicy(SuspendPolicy policy)
	{
		if(isEnabled() || deleted)
		{
			throw invalidState();
		}
		suspendPolicy = policy;
	}

	@Nonnull
	public SuspendPolicy suspendPolicy()
	{
		return suspendPolicy;
	}

	/**
	 * set (enable) the event request
	 */
	synchronized void set()
	{
		JDWP.EventRequest.Set.Modifier[] mods = filters.toArray(new JDWP.EventRequest.Set.Modifier[filters.size()]);
		try
		{
			id = JDWP.EventRequest.Set.process(vm, (byte) eventCmd().ordinal(), suspendPolicy.ordinal(), mods).requestID;
		}
		catch(JDWPException exc)
		{
			throw exc.asUncheckedException();
		}
		isEnabled = true;
	}

	synchronized void clear()
	{
		try
		{
			JDWP.EventRequest.Clear.process(vm, (byte) eventCmd().ordinal(), id);
		}
		catch(JDWPException exc)
		{
			throw exc.asUncheckedException();
		}
		isEnabled = false;
	}

	/**
	 * @return a small Map
	 * @see #putProperty
	 * @see #getProperty
	 */
	private Map<Object, Object> getProperties()
	{
		if(clientProperties == null)
		{
			clientProperties = new HashMap<Object, Object>(2);
		}
		return clientProperties;
	}

	/**
	 * Returns the value of the property with the specified key.  Only
	 * properties added with <code>putProperty</code> will return
	 * a non-null value.
	 *
	 * @return the value of this property or null
	 * @see #putProperty
	 */
	public final Object getProperty(Object key)
	{
		if(clientProperties == null)
		{
			return null;
		}
		else
		{
			return getProperties().get(key);
		}
	}

	/**
	 * Add an arbitrary key/value "property" to this component.
	 *
	 * @see #getProperty
	 */
	public final void putProperty(Object key, Object value)
	{
		if(value != null)
		{
			getProperties().put(key, value);
		}
		else
		{
			getProperties().remove(key);
		}
	}
}
