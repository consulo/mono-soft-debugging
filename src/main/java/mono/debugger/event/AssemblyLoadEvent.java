package mono.debugger.event;

import jakarta.annotation.Nonnull;
import mono.debugger.AssemblyMirror;
import mono.debugger.JDWP;
import mono.debugger.VirtualMachine;

/**
 * @author VISTALL
 * @since 08.04.14
 */
public class AssemblyLoadEvent extends ThreadedEvent implements Event
{
	private final AssemblyMirror myAssembly;

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

	@Nonnull
	public AssemblyMirror getAssembly()
	{
		return myAssembly;
	}
}
