package mono.debugger.event;

import org.jetbrains.annotations.NotNull;
import mono.debugger.JDWP;
import mono.debugger.TypeMirror;
import mono.debugger.VirtualMachine;

/**
 * @author VISTALL
 * @since 24.04.14
 */
public class TypeLoadEvent extends ThreadedEvent
{
	private final TypeMirror myTypeMirror;

	public TypeLoadEvent(VirtualMachine virtualMachine, JDWP.Event.Composite.Events.TypeLoad evt)
	{
		super(virtualMachine, evt, evt.requestID, evt.thread);
		myTypeMirror = evt.typeMirror;
	}

	@Override
	public String eventName()
	{
		return "type load";
	}

	@NotNull
	public TypeMirror typeMirror()
	{
		return myTypeMirror;
	}
}
