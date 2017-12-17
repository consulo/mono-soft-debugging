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
		myAppDomainMirror = ps.readAppDomainMirror();
	}
}
