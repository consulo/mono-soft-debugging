package mono.debugger.protocol;

import mono.debugger.AppDomainMirror;
import mono.debugger.JDWPException;
import mono.debugger.PacketStream;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 09.04.14
 */
public class AppDomain_GetRootDomain implements AppDomain
{
	static final int COMMAND = 1;

	public static AppDomain_GetRootDomain process(VirtualMachineImpl vm) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm);
		return waitForReply(vm, ps);
	}

	static PacketStream enqueueCommand(VirtualMachineImpl vm)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
		{
			vm.printTrace("Sending Command(id=" + ps.pkt.id + ") AppDomain_GetRootDomain" + (ps.pkt.flags != 0 ? ", " +"" + "FLAGS=" + ps.pkt.flags : ""));
		}
		ps.send();
		return ps;
	}

	static AppDomain_GetRootDomain waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
	{
		ps.waitForReply();
		return new AppDomain_GetRootDomain(vm, ps);
	}

	public AppDomainMirror myAppDomainMirror;

	private AppDomain_GetRootDomain(VirtualMachineImpl vm, PacketStream ps)
	{
		if(vm.traceReceives)
		{
			vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") AppDomain_GetRootDomain" + (ps.pkt.flags != 0 ? ", " +
					"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
		}
		myAppDomainMirror = ps.readAppDomainMirror();
		if(vm.traceReceives)
		{
			vm.printReceiveTrace(4, "appDomainReference: " + myAppDomainMirror);
		}
	}
}
