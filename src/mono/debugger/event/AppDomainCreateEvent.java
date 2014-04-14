package mono.debugger.event;

import mono.debugger.EventSetImpl;
import mono.debugger.JDWP;
import mono.debugger.VirtualMachine;

/**
 * @author VISTALL
 * @since 14.04.14
 */
public class AppDomainCreateEvent extends EventSetImpl.ThreadedEventImpl implements Event
{
	public AppDomainCreateEvent(VirtualMachine virtualMachine, JDWP.Event.Composite.Events.AppDomainCreate evt)
	{
		super(virtualMachine, evt, evt.requestID, evt.thread);
	}

	@Override
	public String eventName()
	{
		return "AppDomainCreateEvent";
	}
}
