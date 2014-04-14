package mono.debugger.protocol;

import mono.debugger.JDWPException;
import mono.debugger.PacketStream;
import mono.debugger.TypeMirror;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 14.04.14
 */
public class VirtualMachine_GetTypes implements VirtualMachine
{
	static final int COMMAND = 12;

	public static VirtualMachine_GetTypes process(VirtualMachineImpl vm, String signature, boolean ignoreCase) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm, signature, ignoreCase);
		return waitForReply(vm, ps);
	}

	static PacketStream enqueueCommand(VirtualMachineImpl vm, String signature, boolean ignoreCase)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		ps.writeString(signature);
		ps.writeBoolean(ignoreCase);
		ps.send();
		return ps;
	}

	static VirtualMachine_GetTypes waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
	{
		ps.waitForReply();
		return new VirtualMachine_GetTypes(vm, ps);
	}

	public final TypeMirror[] types;

	private VirtualMachine_GetTypes(VirtualMachineImpl vm, PacketStream ps)
	{
		int classesCount = ps.readInt();
		types = new TypeMirror[classesCount];
		for(int i = 0; i < classesCount; i++)
		{
			types[i] = ps.readTypeMirror();
		}
	}
}
