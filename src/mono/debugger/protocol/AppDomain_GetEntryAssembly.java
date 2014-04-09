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
public class AppDomain_GetEntryAssembly implements AppDomain
{
	static final int COMMAND = 4;

	public static AppDomain_GetEntryAssembly process(VirtualMachineImpl vm, AppDomainReference appDomainReference) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm, appDomainReference);
		return waitForReply(vm, ps);
	}

	static PacketStream enqueueCommand(VirtualMachineImpl vm, AppDomainReference appDomainReference)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
		{
			vm.printTrace("Sending Command(id=" + ps.pkt.id + ") AppDomain_GetEntryAssembly" + (ps.pkt.flags != 0 ? ", " +"" + "FLAGS=" + ps.pkt.flags : ""));
		}
		ps.writeId(appDomainReference.ref());
		ps.send();
		return ps;
	}

	static AppDomain_GetEntryAssembly waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
	{
		ps.waitForReply();
		return new AppDomain_GetEntryAssembly(vm, ps);
	}

	public AssemblyReference assembly;

	private AppDomain_GetEntryAssembly(VirtualMachineImpl vm, PacketStream ps)
	{
		if(vm.traceReceives)
		{
			vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") AppDomain_GetEntryAssembly" + (ps.pkt.flags != 0 ? ", " +
					"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
		}

		assembly = ps.readAssemblyReference();
	}
}
