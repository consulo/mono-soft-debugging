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
public class ArrayReference_GetValues implements ArrayReference
{
	static final int COMMAND = 2;

	public final Value<?>[] values;

	public static ArrayReference_GetValues process(VirtualMachineImpl vm, MirrorWithId objectValueMirror, int index, int length) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm, objectValueMirror, index, length);
		return waitForReply(vm, ps, length);
	}

	static PacketStream enqueueCommand(VirtualMachineImpl vm, MirrorWithId objectValueMirror, int index, int length)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		ps.writeId(objectValueMirror);
		ps.writeInt(index);
		ps.writeInt(length);
		ps.send();
		return ps;
	}

	static ArrayReference_GetValues waitForReply(VirtualMachineImpl vm, PacketStream ps, int length) throws JDWPException
	{
		ps.waitForReply();
		return new ArrayReference_GetValues(vm, ps, length);
	}

	private ArrayReference_GetValues(VirtualMachineImpl vm, PacketStream ps, int length)
	{
		values = new Value[length];
		for(int i = 0; i < values.length; i++)
		{
			values[i] = ps.readValue();
		}
	}
}
