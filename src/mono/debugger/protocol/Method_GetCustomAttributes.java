package mono.debugger.protocol;

import mono.debugger.CustomAttributeMirror;
import mono.debugger.JDWPException;
import mono.debugger.MethodMirror;
import mono.debugger.PacketStream;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 23.07.2015
 */
public class Method_GetCustomAttributes implements Method
{
	static final int COMMAND = 9;

	public static Method_GetCustomAttributes process(VirtualMachineImpl vm, MethodMirror methodMirror) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm, methodMirror);
		return waitForReply(vm, ps);
	}

	static PacketStream enqueueCommand(VirtualMachineImpl vm, MethodMirror methodMirror)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		ps.writeId(methodMirror);
		ps.writeInt(0); // attribute id
		ps.send();
		return ps;
	}

	static Method_GetCustomAttributes waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
	{
		ps.waitForReply();
		return new Method_GetCustomAttributes(vm, ps);
	}

	public final CustomAttributeMirror[] customAttributeMirrors;

	private Method_GetCustomAttributes(VirtualMachineImpl vm, PacketStream ps)
	{
		customAttributeMirrors = ps.readCustomAttributes();
	}
}
