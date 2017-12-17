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
		public final int frameID;

		public final Location location;

		public final byte flags;

		private Frame(VirtualMachineImpl vm, PacketStream ps)
		{
			frameID = ps.readInt();
			location = ps.readLocation();
			flags = ps.readByte();
		}
	}

	public final Frame[] frames;

	private Thread_GetFrameInfo(VirtualMachineImpl vm, PacketStream ps)
	{
		int framesCount = ps.readInt();
		frames = new Frame[framesCount];
		for(int i = 0; i < framesCount; i++)
		{
			frames[i] = new Frame(vm, ps);
		}
	}
}
