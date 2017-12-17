package mono.debugger.protocol;

import mono.debugger.JDWPException;
import mono.debugger.MethodMirror;
import mono.debugger.PacketStream;
import mono.debugger.TypeMirror;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 11.04.14
 */
public class Type_GetMethods implements Type
{
	static final int COMMAND = 2;

	public static Type_GetMethods process(VirtualMachineImpl vm, TypeMirror typeMirror) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm, typeMirror);
		return waitForReply(vm, ps);
	}

	static PacketStream enqueueCommand(VirtualMachineImpl vm, TypeMirror typeMirror)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		ps.writeId(typeMirror);
		ps.send();
		return ps;
	}

	static Type_GetMethods waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
	{
		ps.waitForReply();
		return new Type_GetMethods(vm, ps);
	}

	public final MethodMirror[] methods;

	private Type_GetMethods(VirtualMachineImpl vm, PacketStream ps)
	{
		int size = ps.readInt();
		methods = new MethodMirror[size];
		for(int i = 0; i < size; i++)
		{
			methods[i] = ps.readMethodMirror();
		}
	}
}
