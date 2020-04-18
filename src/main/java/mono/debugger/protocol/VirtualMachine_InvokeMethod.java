package mono.debugger.protocol;

import mono.debugger.InvokeResult;
import mono.debugger.JDWPException;
import mono.debugger.MethodMirror;
import mono.debugger.PacketStream;
import mono.debugger.ThreadMirror;
import mono.debugger.ThrowValueException;
import mono.debugger.Value;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 09.04.14
 */
public class VirtualMachine_InvokeMethod implements VirtualMachine
{
	static final int COMMAND = 7;

	public static VirtualMachine_InvokeMethod process(VirtualMachineImpl vm, ThreadMirror threadMirror, int invokeFlags,
													  MethodMirror methodMirror, Value<?> thisObjectMirror, Value<?>... arguments) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm, threadMirror, invokeFlags, methodMirror, thisObjectMirror, arguments);
		return waitForReply(vm, ps);
	}

	static PacketStream enqueueCommand(VirtualMachineImpl vm, ThreadMirror threadMirror, int invokeFlags, MethodMirror methodMirror,
									   Value<?> thisObjectMirror, Value<?>[] arguments)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		ps.writeId(threadMirror);
		ps.writeInt(invokeFlags);
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

	private boolean myThrowException;
	private Value myValue;
	private Value myOutThis;
	private Value[] myOutArgs;

	private VirtualMachine_InvokeMethod(VirtualMachineImpl vm, PacketStream ps)
	{
		byte resflags = ps.readByte();
		myThrowException = resflags == 0;
		myValue = ps.readValue();
		if((resflags & 2) != 0)
		{
			myOutThis = ps.readValue();
		}

		if((resflags & 4) != 0)
		{
			int args = ps.readInt();
			myOutArgs = new Value[args];

			for(int i = 0; i < args; i++)
			{
				myOutArgs[i] = ps.readValue();
			}
		}
	}

	public InvokeResult getValue()
	{
		if(myThrowException)
		{
			throw new ThrowValueException(myValue);
		}
		else
		{
			return new InvokeResult(myThrowException, myValue, myOutThis, myOutArgs);
		}
	}
}
