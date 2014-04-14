package mono.debugger.protocol;

import mono.debugger.JDWPException;
import mono.debugger.PacketStream;
import mono.debugger.TypeMirror;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 14.04.14
 */
public class VirtualMachine_GetTypesForSourceFile implements VirtualMachine
{
	static final int COMMAND = 11;

	public static VirtualMachine_GetTypesForSourceFile process(VirtualMachineImpl vm, String sourceFile, boolean ignoreCase) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm, sourceFile, ignoreCase);
		return waitForReply(vm, ps);
	}

	static PacketStream enqueueCommand(VirtualMachineImpl vm, String sourceFile, boolean ignoreCase)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		ps.writeString(sourceFile);
		ps.writeBoolean(ignoreCase);
		ps.send();
		return ps;
	}

	static VirtualMachine_GetTypesForSourceFile waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
	{
		ps.waitForReply();
		return new VirtualMachine_GetTypesForSourceFile(vm, ps);
	}

	public final TypeMirror[] types;

	private VirtualMachine_GetTypesForSourceFile(VirtualMachineImpl vm, PacketStream ps)
	{
		int classesCount = ps.readInt();
		types = new TypeMirror[classesCount];
		for(int i = 0; i < classesCount; i++)
		{
			types[i] = ps.readTypeMirror();
		}
	}
}
