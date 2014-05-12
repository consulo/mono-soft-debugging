package mono.debugger.request;

import org.jetbrains.annotations.NotNull;
import mono.debugger.EventKind;
import mono.debugger.EventRequestManagerImpl;
import mono.debugger.VirtualMachine;

/**
 * @author VISTALL
 * @since 11.05.14
 */
public class AppDomainUnloadRequest extends EventRequestImpl
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

	@Override
	public <A, R> R visit(@NotNull EventRequestVisitor<A, R> visitor, A a)
	{
		return visitor.visitAppDomainCreateUnload(this, a);
	}
}
