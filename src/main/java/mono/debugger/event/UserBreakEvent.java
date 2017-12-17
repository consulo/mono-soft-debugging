package mono.debugger.event;

import mono.debugger.JDWP;
import mono.debugger.VirtualMachine;

/**
 * @author VISTALL
 * @since 23.04.2015
 */
public class UserBreakEvent extends ThreadedEvent
{
	public UserBreakEvent(VirtualMachine virtualMachine, JDWP.Event.Composite.Events.UserBreak evt)
	{
		super(virtualMachine, evt, evt.requestID, evt.thread);
	}

	@Override
	public String eventName()
	{
		return "user break";
	}
}
