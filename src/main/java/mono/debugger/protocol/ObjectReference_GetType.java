package mono.debugger.protocol;

import mono.debugger.JDWPException;
import mono.debugger.ObjectValueMirror;
import mono.debugger.PacketStream;
import mono.debugger.TypeMirror;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 11.04.14
 */
public class ObjectReference_GetType implements ObjectReference
{
	static final int COMMAND = 1;

	public static ObjectReference_GetType process(VirtualMachineImpl vm, ObjectValueMirror objectValueMirror) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm, objectValueMirror);
		return waitForReply(vm, ps);
	}

	static PacketStream enqueueCommand(VirtualMachineImpl vm, ObjectValueMirror objectValueMirror)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		ps.writeId(objectValueMirror);
		ps.send();
		return ps;
	}

	static ObjectReference_GetType waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
	{
		ps.waitForReply();
		return new ObjectReference_GetType(vm, ps);
	}

	public TypeMirror type;

	private ObjectReference_GetType(VirtualMachineImpl vm, PacketStream ps)
	{
		type = ps.readTypeMirror();
	}
}
