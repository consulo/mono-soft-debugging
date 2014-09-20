package mono.debugger.protocol;

import mono.debugger.JDWPException;
import mono.debugger.PacketStream;
import mono.debugger.TypeMirror;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 20.09.14
 */
public class Type_GetInterfaces implements Type
{
	static final int COMMAND = 16;

	public static Type_GetInterfaces process(VirtualMachineImpl vm, TypeMirror typeMirror) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm, typeMirror);
		return waitForReply(vm, ps);
	}

	static PacketStream enqueueCommand(VirtualMachineImpl vm, TypeMirror typeMirror)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		ps.writeId(typeMirror);
		ps.send();
		return ps;
	}

	static Type_GetInterfaces waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
	{
		ps.waitForReply();
		return new Type_GetInterfaces(vm, ps);
	}

	public final TypeMirror[] interfaces;

	private Type_GetInterfaces(VirtualMachineImpl vm, PacketStream ps)
	{
		int size = ps.readInt();
		interfaces = new TypeMirror[size];
		for(int i = 0; i < size; i++)
		{
			interfaces[i] = ps.readTypeMirror();
		}
	}
}
