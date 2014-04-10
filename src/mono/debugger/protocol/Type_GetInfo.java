package mono.debugger.protocol;

import mono.debugger.AssemblyMirror;
import mono.debugger.JDWPException;
import mono.debugger.PacketStream;
import mono.debugger.TypeMirror;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class Type_GetInfo implements Type
{
	static final int COMMAND = 1;

	public static Type_GetInfo process(VirtualMachineImpl vm, TypeMirror typeMirror) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm, typeMirror);
		return waitForReply(vm, ps);
	}

	static PacketStream enqueueCommand(VirtualMachineImpl vm, TypeMirror typeMirror)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		ps.writeId(typeMirror);
		ps.send();
		return ps;
	}

	static Type_GetInfo waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
	{
		ps.waitForReply();
		return new Type_GetInfo(vm, ps);
	}

	public final String namespace;
	public final String name;
	public final String fullName;
	public final AssemblyMirror assemblyMirror;

	private Type_GetInfo(VirtualMachineImpl vm, PacketStream ps)
	{
		namespace = ps.readString();
		name = ps.readString();
		fullName = ps.readString();
		assemblyMirror = ps.readAssemblyMirror();
	}
}
