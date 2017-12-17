package mono.debugger.request;

import mono.debugger.EventKind;
import mono.debugger.EventRequestManagerImpl;
import mono.debugger.VirtualMachine;

/**
 * @author VISTALL
 * @since 11.05.14
 */
public class AppDomainUnloadRequest extends EventRequest
{
	public AppDomainUnloadRequest(VirtualMachine virtualMachine, EventRequestManagerImpl requestManager)
	{
		super(virtualMachine, requestManager);
	}

	@Override
	public EventKind eventCmd()
	{
		return EventKind.APPDOMAIN_UNLOAD;
	}
}
