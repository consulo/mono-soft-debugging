package mono.debugger.protocol;

import mono.debugger.AssemblyMirror;
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

	public static Assembly_GetLocation process(VirtualMachineImpl vm, AssemblyMirror assemblyMirror) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm, assemblyMirror);
		return waitForReply(vm, ps);
	}

	static PacketStream enqueueCommand(VirtualMachineImpl vm, AssemblyMirror assemblyMirror)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		ps.writeId(assemblyMirror);
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
