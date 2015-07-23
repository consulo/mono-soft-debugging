package mono.debugger.protocol;

import mono.debugger.JDWPException;
import mono.debugger.PacketStream;
import mono.debugger.TypeMirror;
import mono.debugger.VirtualMachineImpl;
import mono.debugger.CustomAttributeMirror;

/**
 * @author VISTALL
 * @since 23.07.2015
 */
public class Type_GetCustomAttributes implements Type
{
	static final int COMMAND = 10;

	public static Type_GetCustomAttributes process(VirtualMachineImpl vm, TypeMirror typeMirror) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm, typeMirror);
		return waitForReply(vm, ps, typeMirror);
	}

	static PacketStream enqueueCommand(VirtualMachineImpl vm, TypeMirror typeMirror)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		ps.writeId(typeMirror);
		ps.writeInt(0); // attribute id
		ps.send();
		return ps;
	}

	static Type_GetCustomAttributes waitForReply(VirtualMachineImpl vm, PacketStream ps, TypeMirror typeMirror) throws JDWPException
	{
		ps.waitForReply();
		return new Type_GetCustomAttributes(vm, ps, typeMirror);
	}

	public final CustomAttributeMirror[] customAttributeMirrors;

	private Type_GetCustomAttributes(VirtualMachineImpl vm, PacketStream ps, TypeMirror parent)
	{
		customAttributeMirrors = ps.readCustomAttributes();
	}
}
