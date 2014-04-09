package mono.debugger.protocol;

import mono.debugger.AppDomainReference;
import mono.debugger.AssemblyReference;
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

	public static AppDomain_GetAssemblies process(VirtualMachineImpl vm, AppDomainReference appDomainReference) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm, appDomainReference);
		return waitForReply(vm, ps);
	}

	static PacketStream enqueueCommand(VirtualMachineImpl vm, AppDomainReference appDomainReference)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
		{
			vm.printTrace("Sending Command(id=" + ps.pkt.id + ") AppDomain_GetAssemblies" + (ps.pkt.flags != 0 ? ", " +"" + "FLAGS=" + ps.pkt.flags : ""));
		}
		ps.writeId(appDomainReference.ref());
		ps.send();
		return ps;
	}

	static AppDomain_GetAssemblies waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
	{
		ps.waitForReply();
		return new AppDomain_GetAssemblies(vm, ps);
	}

	public AssemblyReference[] assemblies;

	private AppDomain_GetAssemblies(VirtualMachineImpl vm, PacketStream ps)
	{
		if(vm.traceReceives)
		{
			vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") AppDomain_GetAssemblies" + (ps.pkt.flags != 0 ? ", " +
					"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
		}

		int count = ps.readInt();
		assemblies = new AssemblyReference[count];
		for(int i = 0; i < assemblies.length; i++)
		{
			assemblies[i] = ps.readAssemblyReference();
		}
	}
}
