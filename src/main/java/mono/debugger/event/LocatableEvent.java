package mono.debugger.event;

import mono.debugger.JDWP;
import mono.debugger.Locatable;
import mono.debugger.Location;
import mono.debugger.MethodMirror;
import mono.debugger.ThreadMirror;
import mono.debugger.VirtualMachine;

public abstract class LocatableEvent extends ThreadedEvent implements Locatable
{
	private Location location;

	public LocatableEvent(VirtualMachine virtualMachine, JDWP.Event.Composite.Events.EventsCommon evt, int requestID, ThreadMirror thread, Location location)
	{
		super(virtualMachine, evt, requestID, thread);
		this.location = location;
	}

	@Override
	public Location location()
	{
		return location;
	}

	/**
	 * For MethodEntry and MethodExit
	 */
	public MethodMirror method()
	{
		return location.method();
	}

	@Override
	public String toString()
	{
		return eventName() + "@" +
				((location() == null) ? " null" : location().toString()) +
				" in thread " + thread().name();
	}
}
