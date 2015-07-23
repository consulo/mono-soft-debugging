package mono.debugger.protocol;

import mono.debugger.JDWPException;
import mono.debugger.PacketStream;
import mono.debugger.TypeMirror;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 23.07.2015
 */
public class Type_IsAssignableFrom implements Type
{
	static final int COMMAND = 8;

	public static Type_IsAssignableFrom process(VirtualMachineImpl vm, TypeMirror typeMirror, TypeMirror typeMirror2) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm, typeMirror, typeMirror2);
		return waitForReply(vm, ps);
	}

	static PacketStream enqueueCommand(VirtualMachineImpl vm, TypeMirror typeMirror, TypeMirror typeMirror2)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		ps.writeId(typeMirror);
		ps.writeId(typeMirror2);
		ps.send();
		return ps;
	}

	static Type_IsAssignableFrom waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
	{
		ps.waitForReply();
		return new Type_IsAssignableFrom(vm, ps);
	}

	public final boolean value;

	private Type_IsAssignableFrom(VirtualMachineImpl vm, PacketStream ps)
	{
		value = ps.readByte() != 0;
	}
}
