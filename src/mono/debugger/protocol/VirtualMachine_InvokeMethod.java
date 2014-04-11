package mono.debugger.protocol;

import mono.debugger.InvokeFlags;
import mono.debugger.JDWPException;
import mono.debugger.MethodMirror;
import mono.debugger.ObjectValueMirror;
import mono.debugger.PacketStream;
import mono.debugger.ThreadMirror;
import mono.debugger.Value;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 09.04.14
 */
public class VirtualMachine_InvokeMethod implements VirtualMachine
{
	static final int COMMAND = 7;

	public static VirtualMachine_InvokeMethod process(
			VirtualMachineImpl vm,
			ThreadMirror threadMirror,
			InvokeFlags invokeFlags,
			MethodMirror methodMirror,
			ObjectValueMirror thisObjectMirror,
			Value<?>... arguments) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm, threadMirror, invokeFlags, methodMirror, thisObjectMirror, arguments);
		return waitForReply(vm, ps);
	}

	static PacketStream enqueueCommand(
			VirtualMachineImpl vm,
			ThreadMirror threadMirror,
			InvokeFlags invokeFlags,
			MethodMirror methodMirror,
			ObjectValueMirror thisObjectMirror,
			Value<?>[] arguments)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		ps.writeId(threadMirror);
		ps.writeInt(invokeFlags.ordinal());
		ps.writeId(methodMirror);
		ps.writeValue(thisObjectMirror);
		ps.writeInt(arguments.length);
		for(Value<?> argument : arguments)
		{
			ps.writeValue(argument);
		}
		ps.send();
		return ps;
	}

	static VirtualMachine_InvokeMethod waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
	{
		ps.waitForReply();
		return new VirtualMachine_InvokeMethod(vm, ps);
	}

	public Value value;

	private VirtualMachine_InvokeMethod(VirtualMachineImpl vm, PacketStream ps)
	{
		byte result = ps.readByte();
		if(result != 0)
		{
			value = ps.readValue();
		}
	}
}
