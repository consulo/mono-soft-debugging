package mono.debugger.protocol;

import mono.debugger.JDWPException;
import mono.debugger.MethodMirror;
import mono.debugger.PacketStream;
import mono.debugger.PropertyMirror;
import mono.debugger.TypeMirror;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 11.04.14
 */
public class Type_GetProperties implements Type
{
	static final int COMMAND = 9;

	public static Type_GetProperties process(VirtualMachineImpl vm, TypeMirror typeMirror) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm, typeMirror);
		return waitForReply(vm, ps, typeMirror);
	}

	static PacketStream enqueueCommand(VirtualMachineImpl vm, TypeMirror typeMirror)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		ps.writeId(typeMirror);
		ps.send();
		return ps;
	}

	static Type_GetProperties waitForReply(VirtualMachineImpl vm, PacketStream ps, TypeMirror typeMirror) throws JDWPException
	{
		ps.waitForReply();
		return new Type_GetProperties(vm, ps, typeMirror);
	}

	public final PropertyMirror[] properties;

	private Type_GetProperties(VirtualMachineImpl vm, PacketStream ps, TypeMirror parent)
	{
		int size = ps.readInt();
		properties = new PropertyMirror[size];
		for(int i = 0; i < size; i++)
		{
			int id = ps.readId();
			String name = ps.readString();
			MethodMirror getMethod = ps.readMethodMirror();
			MethodMirror setMethod = ps.readMethodMirror();
			int attributes = ps.readInt();
			properties[i] = new PropertyMirror(vm, id, name, getMethod, setMethod, parent, attributes);
		}
	}

}
