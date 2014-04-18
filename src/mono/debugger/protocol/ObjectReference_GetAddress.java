package mono.debugger.protocol;

import mono.debugger.JDWPException;
import mono.debugger.ObjectValueMirror;
import mono.debugger.PacketStream;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 18.04.14
 */
public class ObjectReference_GetAddress implements ObjectReference
{
	static final int COMMAND = 4;

	public static ObjectReference_GetAddress process(VirtualMachineImpl vm, ObjectValueMirror objectValueMirror) throws JDWPException
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

	static ObjectReference_GetAddress waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
	{
		ps.waitForReply();
		return new ObjectReference_GetAddress(vm, ps);
	}

	public long address;

	private ObjectReference_GetAddress(VirtualMachineImpl vm, PacketStream ps)
	{
		address = ps.readLong();
	}

}
