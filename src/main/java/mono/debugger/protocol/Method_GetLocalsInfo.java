package mono.debugger.protocol;

import mono.debugger.JDWPException;
import mono.debugger.LocalVariableMirror;
import mono.debugger.MethodMirror;
import mono.debugger.PacketStream;
import mono.debugger.TypeMirror;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 12.04.14
 */
public class Method_GetLocalsInfo implements Method
{
	static final int COMMAND = 5;

	public static Method_GetLocalsInfo process(VirtualMachineImpl vm, MethodMirror methodMirror) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm, methodMirror);
		return waitForReply(vm, ps);
	}

	static PacketStream enqueueCommand(VirtualMachineImpl vm, MethodMirror methodMirror)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		ps.writeId(methodMirror);
		ps.send();
		return ps;
	}

	static Method_GetLocalsInfo waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
	{
		ps.waitForReply();
		return new Method_GetLocalsInfo(vm, ps);
	}

	public final LocalVariableMirror[] localVariables;

	private Method_GetLocalsInfo(VirtualMachineImpl vm, PacketStream ps)
	{
		int size = ps.readInt();

		this.localVariables = new LocalVariableMirror[size];
		TypeMirror[] parameterTypes = new TypeMirror[size];
		String[] names = new String[size];
		int[] liveRangeStart = new int[size];
		int[] liveRangeEnd = new int[size];
		for(int i = 0; i < size; i++)
		{
			parameterTypes[i] = ps.readTypeMirror();
		}

		for(int i = 0; i < size; i++)
		{
			names[i] = ps.readString();
		}

		for(int i = 0; i < size; i++)
		{
			liveRangeStart[i] = ps.readInt();
			liveRangeEnd[i] = ps.readInt();
		}

		for(int i = 0; i < size; i++)
		{
			localVariables[i] = new LocalVariableMirror(vm, i, parameterTypes[i], names[i], liveRangeStart[i], liveRangeEnd[i]);
		}
	}
}
