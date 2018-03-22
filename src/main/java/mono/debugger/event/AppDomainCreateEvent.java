package mono.debugger.event;

import javax.annotation.Nonnull;

import mono.debugger.AppDomainMirror;
import mono.debugger.JDWP;
import mono.debugger.VirtualMachine;

/**
 * @author VISTALL
 * @since 14.04.14
 */
public class AppDomainCreateEvent extends ThreadedEvent implements Event
{
	private AppDomainMirror myAppDomainMirror;

	public AppDomainCreateEvent(VirtualMachine virtualMachine, JDWP.Event.Composite.Events.AppDomainCreate evt)
	{
		super(virtualMachine, evt, evt.requestID, evt.thread);
		myAppDomainMirror = evt.appDomainMirror;
	}

	@Nonnull
	public AppDomainMirror getAppDomainMirror()
	{
		return myAppDomainMirror;
	}

	@Override
	public String eventName()
	{
		return "AppDomainCreateEvent";
	}
}
