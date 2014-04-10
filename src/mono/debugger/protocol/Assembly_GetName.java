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
		name = ps.readString();
	}
}
