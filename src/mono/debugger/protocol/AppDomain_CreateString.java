package mono.debugger.protocol;

import mono.debugger.AppDomainMirror;
import mono.debugger.JDWPException;
import mono.debugger.PacketStream;
import mono.debugger.StringValueMirror;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 13.04.14
 */
public class AppDomain_CreateString implements AppDomain
{
	static final int COMMAND = 5;

	public static AppDomain_CreateString process(VirtualMachineImpl vm, AppDomainMirror domainMirror, String value) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm, domainMirror, value);
		return waitForReply(vm, ps);
	}

	static PacketStream enqueueCommand(VirtualMachineImpl vm, AppDomainMirror domainMirror, String value)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		ps.writeId(domainMirror);
		ps.writeString(value);
		ps.send();
		return ps;
	}

	static AppDomain_CreateString waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
	{
		ps.waitForReply();
		return new AppDomain_CreateString(vm, ps);
	}

	public StringValueMirror value;

	private AppDomain_CreateString(VirtualMachineImpl vm, PacketStream ps)
	{
		value = new StringValueMirror(vm, ps.readObjectMirror());
	}
}
