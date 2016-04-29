package mono.debugger.protocol;

import mono.debugger.AssemblyMirror;
import mono.debugger.JDWPException;
import mono.debugger.PacketStream;
import mono.debugger.TypeMirror;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 18.04.14
 */
public class Assembly_GetType implements Assembly
{
	static final int COMMAND = 5;

	public static Assembly_GetType process(VirtualMachineImpl vm, AssemblyMirror assemblyMirror, String name, boolean ignoreCase) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm, assemblyMirror, name, ignoreCase);
		return waitForReply(vm, ps);
	}

	static PacketStream enqueueCommand(VirtualMachineImpl vm, AssemblyMirror assemblyMirror, String name, boolean ignoreCase)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		ps.writeId(assemblyMirror);
		ps.writeString(name);
		ps.writeByteBool(ignoreCase);
		ps.send();
		return ps;
	}

	static Assembly_GetType waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
	{
		ps.waitForReply();
		return new Assembly_GetType(vm, ps);
	}

	public final TypeMirror type;

	private Assembly_GetType(VirtualMachineImpl vm, PacketStream ps)
	{
		type = ps.readTypeMirror();
	}
}
