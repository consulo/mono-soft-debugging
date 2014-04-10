package mono.debugger.protocol;

import mono.debugger.JDWPException;
import mono.debugger.ObjectMirror;
import mono.debugger.PacketStream;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class ArrayReference_GetLength implements ArrayReference
{
	static final int COMMAND = 1;

	public static ArrayReference_GetLength process(VirtualMachineImpl vm, ObjectMirror objectMirror) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm, objectMirror);
		return waitForReply(vm, ps);
	}

	static PacketStream enqueueCommand(VirtualMachineImpl vm, ObjectMirror objectMirror)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		ps.writeId(objectMirror);
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
