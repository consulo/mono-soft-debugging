package mono.debugger.protocol;

import mono.debugger.JDWPException;
import mono.debugger.PacketStream;
import mono.debugger.ThreadMirror;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 11.02.15
 */
public class Thread_GetId implements Thread
{
	static final int COMMAND = 5;

	public static Thread_GetId process(VirtualMachineImpl vm, ThreadMirror thread) throws JDWPException
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

	static Thread_GetId waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
	{
		ps.waitForReply();
		return new Thread_GetId(vm, ps);
	}

	public final int id;

	private Thread_GetId(VirtualMachineImpl vm, PacketStream ps)
	{
		id = ps.readId();
	}
}
