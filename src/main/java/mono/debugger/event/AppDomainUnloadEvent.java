package mono.debugger.event;

import jakarta.annotation.Nonnull;

import mono.debugger.AppDomainMirror;
import mono.debugger.JDWP;
import mono.debugger.VirtualMachine;

/**
 * @author VISTALL
 * @since 14.04.14
 */
public class AppDomainUnloadEvent extends ThreadedEvent implements Event
{
	private AppDomainMirror myAppDomainMirror;

	public AppDomainUnloadEvent(VirtualMachine virtualMachine, JDWP.Event.Composite.Events.AppDomainUnload evt)
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
		return "AppDomainUnloadEvent";
	}
}
