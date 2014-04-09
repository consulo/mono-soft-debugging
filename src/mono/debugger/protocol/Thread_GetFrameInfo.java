package mono.debugger.protocol;

import mono.debugger.JDWPException;
import mono.debugger.Location;
import mono.debugger.PacketStream;
import mono.debugger.ThreadReferenceImpl;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 09.04.14
 */
public class Thread_GetFrameInfo implements Thread
{
	static final int COMMAND = 6;

	public static Thread_GetFrameInfo process(VirtualMachineImpl vm, ThreadReferenceImpl thread, int startFrame, int length) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm, thread, startFrame, length);
		return waitForReply(vm, ps);
	}

	static PacketStream enqueueCommand(VirtualMachineImpl vm, ThreadReferenceImpl thread, int startFrame, int length)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
		{
			vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.ThreadReference.Frames" + (ps.pkt.flags != 0 ? ", " +
					"FLAGS=" + ps.pkt.flags : ""));
		}
		if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
		{
			ps.vm.printTrace("Sending:                 thread(ThreadReferenceImpl): " + (thread == null ? "NULL" : "ref=" + thread.ref()));
		}
		ps.writeId(thread);
		if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
		{
			ps.vm.printTrace("Sending:                 startFrame(int): " + startFrame);
		}
		ps.writeInt(startFrame);
		if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
		{
			ps.vm.printTrace("Sending:                 length(int): " + length);
		}
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
		/**
		 * The ID of this frame.
		 */
		public final long frameID;

		/**
		 * The current location of this frame
		 */
		public final Location location;

		public final byte flags;

		private Frame(VirtualMachineImpl vm, PacketStream ps)
		{
			frameID = ps.readInt();
			if(vm.traceReceives)
			{
				vm.printReceiveTrace(5, "frameID(long): " + frameID);
			}
			location = ps.readLocation();
			if(vm.traceReceives)
			{
				vm.printReceiveTrace(5, "location(Location): " + location);
			}
			flags = ps.readByte();
			if(vm.traceReceives)
			{
				vm.printReceiveTrace(5, "flags(byte): " + location);
			}
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
			vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.ThreadReference.Frames" + (ps.pkt.flags != 0 ? ", " +
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
