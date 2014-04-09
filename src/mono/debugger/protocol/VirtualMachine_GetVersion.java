package mono.debugger.protocol;

import mono.debugger.JDWPException;
import mono.debugger.PacketStream;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 09.04.14
 */
public class VirtualMachine_GetVersion implements VirtualMachine
{
	static final int COMMAND = 1;

	public static VirtualMachine_GetVersion process(VirtualMachineImpl vm) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm);
		return waitForReply(vm, ps);
	}

	static PacketStream enqueueCommand(VirtualMachineImpl vm)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
		{
			vm.printTrace("Sending Command(id=" + ps.pkt.id + ") VirtualMachine_GetVersion" + (ps.pkt.flags != 0 ? ", " +
					"FLAGS=" + ps.pkt.flags : ""));
		}
		ps.send();
		return ps;
	}

	static VirtualMachine_GetVersion waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
	{
		ps.waitForReply();
		return new VirtualMachine_GetVersion(vm, ps);
	}


	/**
	 * Text information on the VM version
	 */
	public final String description;

	/**
	 * Major DWP Version number
	 */
	public final int jdwpMajor;

	/**
	 * Minor DWP Version number
	 */
	public final int jdwpMinor;

	private VirtualMachine_GetVersion(VirtualMachineImpl vm, PacketStream ps)
	{
		if(vm.traceReceives)
		{
			vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") VirtualMachine_GetVersion" + (ps.pkt.flags != 0 ? ", " +
					"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
		}
		description = ps.readString();
		if(vm.traceReceives)
		{
			vm.printReceiveTrace(4, "description(String): " + description);
		}
		jdwpMajor = ps.readInt();
		if(vm.traceReceives)
		{
			vm.printReceiveTrace(4, "jdwpMajor(int): " + jdwpMajor);
		}
		jdwpMinor = ps.readInt();
		if(vm.traceReceives)
		{
			vm.printReceiveTrace(4, "jdwpMinor(int): " + jdwpMinor);
		}
	}
}
