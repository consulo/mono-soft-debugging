package mono.debugger.protocol;

import mono.debugger.JDWPException;
import mono.debugger.ObjectValueMirror;
import mono.debugger.PacketStream;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 11.04.14
 */
public class StringReference_GetValue implements StringReference
{
	static final int COMMAND = 1;

	public static StringReference_GetValue process(VirtualMachineImpl vm, ObjectValueMirror objectValueMirror) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm, objectValueMirror);
		return waitForReply(vm, ps);
	}

	static PacketStream enqueueCommand(VirtualMachineImpl vm, ObjectValueMirror objectValueMirror)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		ps.writeId(objectValueMirror);
		ps.send();
		return ps;
	}

	static StringReference_GetValue waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
	{
		ps.waitForReply();
		return new StringReference_GetValue(vm, ps);
	}

	public final String value;

	private StringReference_GetValue(VirtualMachineImpl vm, PacketStream ps)
	{
		boolean is_utf16 = false;
		if(vm.isAtLeastVersion(2, 41))
		{
			is_utf16 = ps.readByteBool();
		}

		if(is_utf16)
		{
			value = ps.readStringUTF16();
		}
		else
		{
			value = ps.readString();
		}
	}
}
