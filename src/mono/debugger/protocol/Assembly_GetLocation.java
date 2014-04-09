package mono.debugger.protocol;

import mono.debugger.AssemblyReference;
import mono.debugger.JDWPException;
import mono.debugger.PacketStream;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 09.04.14
 */
public class Assembly_GetLocation implements Assembly
{
	static final int COMMAND = 1;

	public static Assembly_GetLocation process(VirtualMachineImpl vm, AssemblyReference assemblyReference) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm, assemblyReference);
		return waitForReply(vm, ps);
	}

	static PacketStream enqueueCommand(VirtualMachineImpl vm, AssemblyReference assemblyReference)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
		{
			vm.printTrace("Sending Command(id=" + ps.pkt.id + ") Assembly_GetLocation" + (ps.pkt.flags != 0 ? ", " +
					"FLAGS=" + ps.pkt.flags : ""));
		}
		if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
		{
			ps.vm.printTrace("Sending:                 assembly(AssemblyReference): " + "ref=" + assemblyReference.ref());
		}
		ps.writeId(assemblyReference.ref());
		ps.send();
		return ps;
	}

	static Assembly_GetLocation waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
	{
		ps.waitForReply();
		return new Assembly_GetLocation(vm, ps);
	}


	public final String location;

	private Assembly_GetLocation(VirtualMachineImpl vm, PacketStream ps)
	{
		if(vm.traceReceives)
		{
			vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") Assembly_GetLocation" + (ps.pkt.flags != 0 ? ", " +
					"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
		}
		location = ps.readString();
		if(vm.traceReceives)
		{
			vm.printReceiveTrace(4, "location): " + location);
		}
	}
}
