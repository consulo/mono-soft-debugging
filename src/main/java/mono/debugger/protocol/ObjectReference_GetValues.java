package mono.debugger.protocol;

import mono.debugger.JDWPException;
import mono.debugger.MirrorWithId;
import mono.debugger.ObjectValueMirror;
import mono.debugger.PacketStream;
import mono.debugger.Value;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 11.04.14
 */
public class ObjectReference_GetValues implements ObjectReference
{
	static final int COMMAND = 2;

	public static ObjectReference_GetValues process(
			VirtualMachineImpl vm,
			ObjectValueMirror objectValueMirror,
			MirrorWithId... mirrorWithIds) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm, objectValueMirror, mirrorWithIds);
		return waitForReply(vm, ps, mirrorWithIds.length);
	}

	static PacketStream enqueueCommand(VirtualMachineImpl vm, ObjectValueMirror objectValueMirror, MirrorWithId... mirrorWithIds)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		ps.writeId(objectValueMirror);
		ps.writeInt(mirrorWithIds.length);
		for(MirrorWithId fieldMirror : mirrorWithIds)
		{
			ps.writeId(fieldMirror);
		}
		ps.send();
		return ps;
	}

	static ObjectReference_GetValues waitForReply(VirtualMachineImpl vm, PacketStream ps, int length) throws JDWPException
	{
		ps.waitForReply();
		return new ObjectReference_GetValues(vm, ps, length);
	}

	public final Value<?>[] values;

	private ObjectReference_GetValues(VirtualMachineImpl vm, PacketStream ps, int length)
	{
		values = new Value[length];
		for(int i = 0; i < length; i++)
		{
			values[i] = ps.readValue();
		}
	}
}
