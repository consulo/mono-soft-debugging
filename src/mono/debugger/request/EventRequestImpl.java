package mono.debugger.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import mono.debugger.EventKind;
import mono.debugger.EventRequestManagerImpl;
import mono.debugger.JDWP;
import mono.debugger.JDWPException;
import mono.debugger.MirrorImpl;
import mono.debugger.SuspendPolicy;
import mono.debugger.VirtualMachine;

/**
* @author VISTALL
* @since 11.05.14
*/
public abstract class EventRequestImpl extends MirrorImpl implements EventRequest
{
	private final EventRequestManagerImpl myRequestManager;
	private int id;

	/*
	 * This list is not protected by a synchronized wrapper. All
	 * access/modification should be protected by synchronizing on
	 * the enclosing instance of EventRequestImpl.
	 */
	public List<Object> filters = new ArrayList<Object>();

	boolean isEnabled = false;
	protected boolean deleted = false;
	protected SuspendPolicy suspendPolicy = SuspendPolicy.ALL;
	private Map<Object, Object> clientProperties = null;

	public EventRequestImpl(VirtualMachine virtualMachine, EventRequestManagerImpl requestManager)
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

	@Override
	public boolean isEnabled()
	{
		return isEnabled;
	}

	@Override
	public void enable()
	{
		setEnabled(true);
	}

	@Override
	public void disable()
	{
		setEnabled(false);
	}

	@Override
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

	@Override
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

	@Override
	public void setSuspendPolicy(SuspendPolicy policy)
	{
		if(isEnabled() || deleted)
		{
			throw invalidState();
		}
		suspendPolicy = policy;
	}

	@NotNull
	@Override
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
	@Override
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
	@Override
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
