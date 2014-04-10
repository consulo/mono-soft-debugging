package mono.debugger.protocol;

import mono.debugger.AppDomainMirror;
import mono.debugger.AssemblyMirror;
import mono.debugger.JDWPException;
import mono.debugger.PacketStream;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 09.04.14
 */
public class AppDomain_GetAssemblies implements AppDomain
{
	static final int COMMAND = 3;

	public static AppDomain_GetAssemblies process(VirtualMachineImpl vm, AppDomainMirror appDomainMirror) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm, appDomainMirror);
		return waitForReply(vm, ps);
	}

	static PacketStream enqueueCommand(VirtualMachineImpl vm, AppDomainMirror appDomainMirror)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
		{
			vm.printTrace("Sending Command(id=" + ps.pkt.id + ") AppDomain_GetAssemblies" + (ps.pkt.flags != 0 ? ", " +"" + "FLAGS=" + ps.pkt.flags : ""));
		}
		ps.writeId(appDomainMirror);
		ps.send();
		return ps;
	}

	static AppDomain_GetAssemblies waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
	{
		ps.waitForReply();
		return new AppDomain_GetAssemblies(vm, ps);
	}

	public AssemblyMirror[] assemblies;

	private AppDomain_GetAssemblies(VirtualMachineImpl vm, PacketStream ps)
	{
		if(vm.traceReceives)
		{
			vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") AppDomain_GetAssemblies" + (ps.pkt.flags != 0 ? ", " +
					"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
		}

		int count = ps.readInt();
		assemblies = new AssemblyMirror[count];
		for(int i = 0; i < assemblies.length; i++)
		{
			assemblies[i] = ps.readAssemblyMirror();
		}
	}
}
