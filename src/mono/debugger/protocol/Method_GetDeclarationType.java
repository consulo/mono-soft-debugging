package mono.debugger.protocol;

import mono.debugger.JDWPException;
import mono.debugger.MethodMirror;
import mono.debugger.PacketStream;
import mono.debugger.TypeMirror;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class Method_GetDeclarationType implements Method
{
	static final int COMMAND = 2;

	public static Method_GetDeclarationType process(VirtualMachineImpl vm, MethodMirror methodMirror) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm, methodMirror);
		return waitForReply(vm, ps);
	}

	static PacketStream enqueueCommand(VirtualMachineImpl vm, MethodMirror methodMirror)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		ps.writeId(methodMirror);
		ps.send();
		return ps;
	}

	static Method_GetDeclarationType waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
	{
		ps.waitForReply();
		return new Method_GetDeclarationType(vm, ps);
	}

	public final TypeMirror declarationType;

	private Method_GetDeclarationType(VirtualMachineImpl vm, PacketStream ps)
	{
		declarationType = ps.readTypeMirror();
	}
}
