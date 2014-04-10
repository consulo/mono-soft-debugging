package mono.debugger.protocol;

import mono.debugger.JDWPException;
import mono.debugger.Location;
import mono.debugger.PacketStream;
import mono.debugger.ThreadMirror;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 09.04.14
 */
public class Thread_GetFrameInfo implements Thread
{
	static final int COMMAND = 1;

	public static Thread_GetFrameInfo process(VirtualMachineImpl vm, ThreadMirror thread, int startFrame, int length) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm, thread, startFrame, length);
		return waitForReply(vm, ps);
	}

	static PacketStream enqueueCommand(VirtualMachineImpl vm, ThreadMirror thread, int startFrame, int length)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		ps.writeId(thread);
		ps.writeInt(startFrame);
		ps.writeInt(length);
		ps.send();
		return ps;
	}

	static Thread_GetFrameInfo waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
	{
		ps.waitForReply();
		return new Thread_GetFrameInfo(vm, ps);
	}

	public static class Frame
	{
		public final long frameID;

		public final Location location;

		public final byte flags;

		private Frame(VirtualMachineImpl vm, PacketStream ps)
		{
			frameID = ps.readInt();
			location = ps.readLocation();
			flags = ps.readByte();
		}
	}


	/**
	 * The number of frames retreived
	 */
	public final Frame[] frames;

	private Thread_GetFrameInfo(VirtualMachineImpl vm, PacketStream ps)
	{
		if(vm.traceReceives)
		{
			vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") Thread_GetFrameInfo" + (ps.pkt.flags != 0 ? ", " +
					"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
		}
		if(vm.traceReceives)
		{
			vm.printReceiveTrace(4, "frames(Frame[]): " + "");
		}
		int framesCount = ps.readInt();
		frames = new Frame[framesCount];
		for(int i = 0; i < framesCount; i++)
		{
			if(vm.traceReceives)
			{
				vm.printReceiveTrace(5, "frames[i](Frame): " + "");
			}
			frames[i] = new Frame(vm, ps);
		}
	}
}
