package mono.debugger.protocol;

import mono.debugger.AppDomainMirror;
import mono.debugger.JDWPException;
import mono.debugger.PacketStream;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 09.04.14
 */
public class AppDomain_GetFriendlyName implements AppDomain
{
	static final int COMMAND = 2;

	public static AppDomain_GetFriendlyName process(VirtualMachineImpl vm, AppDomainMirror appDomainMirror) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm, appDomainMirror);
		return waitForReply(vm, ps);
	}

	static PacketStream enqueueCommand(VirtualMachineImpl vm, AppDomainMirror appDomainMirror)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		ps.writeId(appDomainMirror);
		ps.send();
		return ps;
	}

	static AppDomain_GetFriendlyName waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
	{
		ps.waitForReply();
		return new AppDomain_GetFriendlyName(vm, ps);
	}

	public String name;

	private AppDomain_GetFriendlyName(VirtualMachineImpl vm, PacketStream ps)
	{
		if(vm.traceReceives)
		{
			vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") AppDomain_GetFriendlyName" + (ps.pkt.flags != 0 ? ", " +
					"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
		}
		name = ps.readString();
		if(vm.traceReceives)
		{
			vm.printReceiveTrace(4, "name: " + name);
		}
	}
}
