package mono.debugger.protocol;

import mono.debugger.JDWPException;
import mono.debugger.ObjectValueMirror;
import mono.debugger.PacketStream;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class ArrayReference_GetLength implements ArrayReference
{
	static final int COMMAND = 1;

	public static ArrayReference_GetLength process(VirtualMachineImpl vm, ObjectValueMirror objectValueMirror) throws JDWPException
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

	static ArrayReference_GetLength waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
	{
		ps.waitForReply();
		return new ArrayReference_GetLength(vm, ps);
	}


	private ArrayReference_GetLength(VirtualMachineImpl vm, PacketStream ps)
	{
		int rank = ps.readInt();
		for(int i = 0; i < rank; i++)
		{
			int size = ps.readInt();
			int lower = ps.readInt();

		}
	}
}
