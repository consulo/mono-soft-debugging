package mono.debugger.event;

import org.jetbrains.annotations.NotNull;
import mono.debugger.AssemblyMirror;
import mono.debugger.JDWP;
import mono.debugger.VirtualMachine;

/**
 * @author VISTALL
 * @since 08.04.14
 */
public class AssemblyUnloadEvent extends ThreadedEvent implements Event
{
	private final AssemblyMirror myAssembly;

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

	@NotNull
	public AssemblyMirror getAssembly()
	{
		return myAssembly;
	}
}
