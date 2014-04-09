package mono.debugger.protocol;

import mono.debugger.AssemblyMirror;
import mono.debugger.JDWPException;
import mono.debugger.PacketStream;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 09.04.14
 */
public class Assembly_GetName implements Assembly
{
	static final int COMMAND = 6;

	public static Assembly_GetName process(VirtualMachineImpl vm, AssemblyMirror assemblyMirror) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm, assemblyMirror);
		return waitForReply(vm, ps);
	}

	static PacketStream enqueueCommand(VirtualMachineImpl vm, AssemblyMirror assemblyMirror)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
		{
			vm.printTrace("Sending Command(id=" + ps.pkt.id + ") Assembly_GetName" + (ps.pkt.flags != 0 ? ", " +
					"FLAGS=" + ps.pkt.flags : ""));
		}
		if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
		{
			ps.vm.printTrace("Sending:                 assembly(AssemblyReference): " + "ref=" + assemblyMirror.id());
		}
		ps.writeId(assemblyMirror);
		ps.send();
		return ps;
	}

	static Assembly_GetName waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
	{
		ps.waitForReply();
		return new Assembly_GetName(vm, ps);
	}


	public final String name;

	private Assembly_GetName(VirtualMachineImpl vm, PacketStream ps)
	{
		if(vm.traceReceives)
		{
			vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") Assembly_GetName" + (ps.pkt.flags != 0 ? ", " +
					"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
		}
		name = ps.readString();
		if(vm.traceReceives)
		{
			vm.printReceiveTrace(4, "name): " + name);
		}
	}
}
