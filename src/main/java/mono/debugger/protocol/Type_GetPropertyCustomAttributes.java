package mono.debugger.protocol;

import mono.debugger.CustomAttributeMirror;
import mono.debugger.JDWPException;
import mono.debugger.PacketStream;
import mono.debugger.PropertyMirror;
import mono.debugger.TypeMirror;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 23.07.2015
 */
public class Type_GetPropertyCustomAttributes implements Type
{
	static final int COMMAND = 12;

	public static Type_GetPropertyCustomAttributes process(VirtualMachineImpl vm, TypeMirror typeMirror, PropertyMirror propertyMirror) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm, typeMirror, propertyMirror);
		return waitForReply(vm, ps, typeMirror);
	}

	static PacketStream enqueueCommand(VirtualMachineImpl vm, TypeMirror typeMirror, PropertyMirror propertyMirror)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		ps.writeId(typeMirror);
		ps.writeId(propertyMirror);
		ps.writeInt(0); // attribute id
		ps.send();
		return ps;
	}

	static Type_GetPropertyCustomAttributes waitForReply(VirtualMachineImpl vm, PacketStream ps, TypeMirror typeMirror) throws JDWPException
	{
		ps.waitForReply();
		return new Type_GetPropertyCustomAttributes(vm, ps, typeMirror);
	}

	public final CustomAttributeMirror[] customAttributeMirrors;

	private Type_GetPropertyCustomAttributes(VirtualMachineImpl vm, PacketStream ps, TypeMirror parent)
	{
		customAttributeMirrors = ps.readCustomAttributes();
	}
}
