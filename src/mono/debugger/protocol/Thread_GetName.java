package mono.debugger.protocol;

import mono.debugger.JDWPException;
import mono.debugger.PacketStream;
import mono.debugger.ThreadMirror;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 09.04.14
 */
public class Thread_GetName implements Thread
{
	static final int COMMAND = 2;

	public static Thread_GetName process(VirtualMachineImpl vm, ThreadMirror thread) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm, thread);
		return waitForReply(vm, ps);
	}

	static PacketStream enqueueCommand(
			VirtualMachineImpl vm, ThreadMirror thread)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
		{
			vm.printTrace("Sending Command(id=" + ps.pkt.id + ") Thread_GetName" + (ps.pkt.flags != 0 ? ", " +
					"FLAGS=" + ps.pkt.flags : ""));
		}
		if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
		{
			ps.vm.printTrace("Sending:                 thread(ThreadMirror): " + thread.ref());
		}
		ps.writeId(thread);
		ps.send();
		return ps;
	}

	static Thread_GetName waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
	{
		ps.waitForReply();
		return new Thread_GetName(vm, ps);
	}


	/**
	 * The thread name.
	 */
	public final String threadName;

	private Thread_GetName(VirtualMachineImpl vm, PacketStream ps)
	{
		if(vm.traceReceives)
		{
			vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") Thread_GetName" + (ps.pkt.flags != 0 ? ", " +
					"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
		}
		threadName = ps.readString();
		if(vm.traceReceives)
		{
			vm.printReceiveTrace(4, "threadName(String): " + threadName);
		}
	}
}
