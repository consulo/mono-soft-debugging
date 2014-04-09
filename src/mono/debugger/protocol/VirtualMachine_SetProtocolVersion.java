package mono.debugger.protocol;

import mono.debugger.JDWPException;
import mono.debugger.PacketStream;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 09.04.14
 */
public class VirtualMachine_SetProtocolVersion implements VirtualMachine
{
	static final int COMMAND = 8;

	public static VirtualMachine_SetProtocolVersion process(VirtualMachineImpl vm, int major, int minor) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm, major, minor);
		return waitForReply(vm, ps);
	}

	static PacketStream enqueueCommand(VirtualMachineImpl vm, int major, int minor)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
		{
			vm.printTrace("Sending Command(id=" + ps.pkt.id + ") VirtualMachine_SetProtocolVersion" + (ps.pkt.flags != 0 ? ", " +
					"" + "FLAGS=" + ps.pkt.flags : ""));
		}
		if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
		{
			ps.vm.printTrace("Sending:                 major: " + major);
		}
		ps.writeInt(major);
		if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
		{
			ps.vm.printTrace("Sending:                 minor: " + minor);
		}
		ps.writeInt(minor);
		ps.send();
		return ps;
	}

	static VirtualMachine_SetProtocolVersion waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
	{
		ps.waitForReply();
		return new VirtualMachine_SetProtocolVersion(vm, ps);
	}

	private VirtualMachine_SetProtocolVersion(VirtualMachineImpl vm, PacketStream ps)
	{
		if(vm.traceReceives)
		{
			vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") VirtualMachine_SetProtocolVersion" + (ps.pkt.flags != 0 ? ", " +
					"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
		}
	}
}
