package mono.debugger.protocol;

import mono.debugger.JDWPException;
import mono.debugger.MirrorWithId;
import mono.debugger.PacketStream;
import mono.debugger.Value;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 26.04.14
 */
public class ArrayReference_SetValues implements ArrayReference
{
	static final int COMMAND = 3;

	public static ArrayReference_SetValues process(VirtualMachineImpl vm, MirrorWithId objectValueMirror, int index,
			Value<?>[] values) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm, objectValueMirror, index, values);
		return waitForReply(vm, ps);
	}

	static PacketStream enqueueCommand(VirtualMachineImpl vm, MirrorWithId objectValueMirror, int index, Value<?>[] values)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		ps.writeId(objectValueMirror);
		ps.writeInt(index);
		ps.writeInt(values.length);
		for(Value<?> value : values)
		{
			ps.writeValue(value);
		}
		ps.send();
		return ps;
	}

	static ArrayReference_SetValues waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
	{
		ps.waitForReply();
		return new ArrayReference_SetValues(vm, ps);
	}

	private ArrayReference_SetValues(VirtualMachineImpl vm, PacketStream ps)
	{
	}
}
