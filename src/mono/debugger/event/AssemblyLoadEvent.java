package mono.debugger.event;

import mono.debugger.AssemblyReference;
import mono.debugger.EventSetImpl;
import mono.debugger.JDWP;
import mono.debugger.VirtualMachine;

/**
 * @author VISTALL
 * @since 08.04.14
 */
public class AssemblyLoadEvent  extends EventSetImpl.ThreadedEventImpl implements Event
{
	private final AssemblyReference myAssembly;

	public AssemblyLoadEvent(VirtualMachine virtualMachine, JDWP.Event.Composite.Events.AssemblyLoad evt)
	{
		super(virtualMachine, evt, evt.requestID, evt.thread);

		myAssembly = evt.assembly;
	}

	@Override
	public String eventName()
	{
		return "AssemblyLoadEvent";
	}

	public AssemblyReference getAssembly()
	{
		return myAssembly;
	}
}
