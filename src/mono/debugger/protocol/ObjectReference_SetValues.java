package mono.debugger.protocol;

import mono.debugger.FieldOrPropertyMirror;
import mono.debugger.JDWPException;
import mono.debugger.ObjectValueMirror;
import mono.debugger.PacketStream;
import mono.debugger.Value;
import mono.debugger.VirtualMachineImpl;
import mono.debugger.util.ImmutablePair;

/**
 * @author VISTALL
 * @since 18.04.14
 */
public class ObjectReference_SetValues implements ObjectReference
{
	static final int COMMAND = 6;

	public static ObjectReference_SetValues process(
			VirtualMachineImpl vm,
			ObjectValueMirror objectValueMirror,
			ImmutablePair<FieldOrPropertyMirror, Value<?>>... pairs) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm, objectValueMirror, pairs);
		return waitForReply(vm, ps);
	}

	static PacketStream enqueueCommand(
			VirtualMachineImpl vm,
			ObjectValueMirror objectValueMirror,
			ImmutablePair<FieldOrPropertyMirror, Value<?>>... pairs)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		ps.writeId(objectValueMirror);
		ps.writeInt(pairs.length);
		for(ImmutablePair<FieldOrPropertyMirror, Value<?>> po : pairs)
		{
			ps.writeId(po.getLeft());
		}
		for(ImmutablePair<FieldOrPropertyMirror, Value<?>> po : pairs)
		{
			ps.writeValue(po.getRight());
		}
		ps.send();
		return ps;
	}

	static ObjectReference_SetValues waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
	{
		ps.waitForReply();
		return new ObjectReference_SetValues(vm, ps);
	}

	private ObjectReference_SetValues(VirtualMachineImpl vm, PacketStream ps)
	{
	}
}
