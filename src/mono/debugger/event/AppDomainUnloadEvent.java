package mono.debugger.event;

import mono.debugger.EventSetImpl;
import mono.debugger.JDWP;
import mono.debugger.VirtualMachine;

/**
 * @author VISTALL
 * @since 14.04.14
 */
public class AppDomainUnloadEvent extends EventSetImpl.ThreadedEventImpl implements Event
{
	public AppDomainUnloadEvent(VirtualMachine virtualMachine, JDWP.Event.Composite.Events.AppDomainUnload evt)
	{
		super(virtualMachine, evt, evt.requestID, evt.thread);
	}

	@Override
	public String eventName()
	{
		return "AppDomainUnloadEvent";
	}
}
