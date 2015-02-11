package mono.debugger.protocol;

import mono.debugger.JDWPException;
import mono.debugger.PacketStream;
import mono.debugger.ThreadMirror;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 11.02.15
 */
public class Thread_GetTId implements Thread
{
	static final int COMMAND = 6;

	public static Thread_GetTId process(VirtualMachineImpl vm, ThreadMirror thread) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm, thread);
		return waitForReply(vm, ps);
	}

	static PacketStream enqueueCommand(VirtualMachineImpl vm, ThreadMirror thread)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		ps.writeId(thread);
		ps.send();
		return ps;
	}

	static Thread_GetTId waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
	{
		ps.waitForReply();
		return new Thread_GetTId(vm, ps);
	}

	public final int id;

	private Thread_GetTId(VirtualMachineImpl vm, PacketStream ps)
	{
		id = ps.readId();
	}
}
