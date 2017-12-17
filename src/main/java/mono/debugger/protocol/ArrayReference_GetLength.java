package mono.debugger.protocol;

import mono.debugger.JDWPException;
import mono.debugger.MirrorWithId;
import mono.debugger.PacketStream;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class ArrayReference_GetLength implements ArrayReference
{
	public static class DimensionInfo
	{
		public final int size;
		public final int lower;

		public DimensionInfo(int size, int lower)
		{
			this.size = size;
			this.lower = lower;
		}
	}

	static final int COMMAND = 1;

	public final DimensionInfo[] dimensions;

	public static ArrayReference_GetLength process(VirtualMachineImpl vm, MirrorWithId objectValueMirror) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm, objectValueMirror);
		return waitForReply(vm, ps);
	}

	static PacketStream enqueueCommand(VirtualMachineImpl vm, MirrorWithId objectValueMirror)
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
		dimensions = new DimensionInfo[rank];
		for(int i = 0; i < rank; i++)
		{
			int size = ps.readInt();
			int lower = ps.readInt();
			dimensions[i] = new DimensionInfo(size, lower);
		}
	}
}
