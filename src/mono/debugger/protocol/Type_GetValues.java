package mono.debugger.protocol;

import mono.debugger.FieldMirror;
import mono.debugger.JDWPException;
import mono.debugger.PacketStream;
import mono.debugger.TypeMirror;
import mono.debugger.Value;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 11.04.14
 */
public class Type_GetValues implements Type
{
	static final int COMMAND = 4;

	public static Type_GetValues process(VirtualMachineImpl vm, TypeMirror typeMirror, FieldMirror... fieldMirrors) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm, typeMirror, fieldMirrors);
		return waitForReply(vm, ps, fieldMirrors.length);
	}

	static PacketStream enqueueCommand(VirtualMachineImpl vm, TypeMirror typeMirror, FieldMirror[] fieldMirrors)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		ps.writeId(typeMirror);
		ps.writeInt(fieldMirrors.length);
		for(FieldMirror fieldMirror : fieldMirrors)
		{
			ps.writeId(fieldMirror);
		}
		ps.send();
		return ps;
	}

	static Type_GetValues waitForReply(VirtualMachineImpl vm, PacketStream ps, int length) throws JDWPException
	{
		ps.waitForReply();
		return new Type_GetValues(vm, ps, length);
	}

	public final Value<?>[] values;

	private Type_GetValues(VirtualMachineImpl vm, PacketStream ps, int length)
	{
		values = new Value[length];
		for(int i = 0; i < length; i++)
		{
			values[i] = ps.readValue();
		}
	}
}
