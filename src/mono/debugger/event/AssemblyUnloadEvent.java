package mono.debugger.event;

import mono.debugger.AssemblyReference;
import mono.debugger.EventSetImpl;
import mono.debugger.JDWP;
import mono.debugger.VirtualMachine;

/**
 * @author VISTALL
 * @since 08.04.14
 */
public class AssemblyUnloadEvent extends EventSetImpl.ThreadedEventImpl implements Event
{
	private final AssemblyReference myAssembly;

	public AssemblyUnloadEvent(VirtualMachine virtualMachine, JDWP.Event.Composite.Events.AssemblyUnLoad evt)
	{
		super(virtualMachine, evt, evt.requestID, evt.thread);

		myAssembly = evt.assembly;
	}

	@Override
	public String eventName()
	{
		return "AssemblyUnloadEvent";
	}

	public AssemblyReference getAssembly()
	{
		return myAssembly;
	}
}
