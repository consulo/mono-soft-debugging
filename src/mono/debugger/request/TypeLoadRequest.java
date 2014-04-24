package mono.debugger.request;

import mono.debugger.EventKind;
import mono.debugger.EventRequestManagerImpl;
import mono.debugger.JDWP;
import mono.debugger.VirtualMachine;

/**
 * @author VISTALL
 * @since 24.04.14
 */
public class TypeLoadRequest extends EventRequestManagerImpl.ClassVisibleEventRequestImpl
{
	public TypeLoadRequest(VirtualMachine virtualMachine, EventRequestManagerImpl requestManager)
	{
		super(virtualMachine, requestManager);
	}

	public void addSourceFileFilter(String... a)
	{
		filters.add(JDWP.EventRequest.Set.Modifier.SourceFileMatch.create(a));
	}

	public void addTypeNameFilter(String... a)
	{
		filters.add(JDWP.EventRequest.Set.Modifier.TypeNameFilter.create(a));
	}

	@Override
	protected EventKind eventCmd()
	{
		return EventKind.TYPE_LOAD;
	}
}
