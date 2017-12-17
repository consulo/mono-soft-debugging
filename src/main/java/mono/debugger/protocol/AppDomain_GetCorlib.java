package mono.debugger.protocol;

import mono.debugger.AppDomainMirror;
import mono.debugger.AssemblyMirror;
import mono.debugger.JDWPException;
import mono.debugger.PacketStream;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 13.04.14
 */
public class AppDomain_GetCorlib implements AppDomain
{
	static final int COMMAND = 6;

	public static AppDomain_GetCorlib process(VirtualMachineImpl vm, AppDomainMirror appDomainMirror) throws JDWPException
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

	static AppDomain_GetCorlib waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
	{
		ps.waitForReply();
		return new AppDomain_GetCorlib(vm, ps);
	}

	public AssemblyMirror assembly;

	private AppDomain_GetCorlib(VirtualMachineImpl vm, PacketStream ps)
	{
		assembly = ps.readAssemblyMirror();
	}
}
