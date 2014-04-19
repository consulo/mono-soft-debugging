package mono.debugger.protocol;

import mono.debugger.AssemblyMirror;
import mono.debugger.JDWPException;
import mono.debugger.PacketStream;
import mono.debugger.TypeMirror;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class Type_GetInfo implements Type
{
	static final int COMMAND = 1;

	public static Type_GetInfo process(VirtualMachineImpl vm, TypeMirror typeMirror) throws JDWPException
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

	static Type_GetInfo waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
	{
		ps.waitForReply();
		return new Type_GetInfo(vm, ps);
	}

	public final String namespace;
	public final String name;
	public final String fullName;
	public final AssemblyMirror assemblyMirror;
	public final TypeMirror baseType;
	public TypeMirror generalType;
	public TypeMirror[] genericArguments = TypeMirror.EMPTY_ARRAY;

	private Type_GetInfo(VirtualMachineImpl vm, PacketStream ps)
	{
		namespace = ps.readString();
		name = ps.readString();
		fullName = ps.readString();
		assemblyMirror = ps.readAssemblyMirror();
		ps.readId(); //TODO [VISTALL] ModuleMirror
		baseType = ps.readTypeMirror();
		TypeMirror elementType = ps.readTypeMirror();
		int token = ps.readInt();
		int rank = ps.readByte();
		int attributes = ps.readInt();
		byte runtimeAttributes = ps.readByte();

		boolean is_byref = (runtimeAttributes & 1) != 0;
		boolean is_pointer = (runtimeAttributes & 2) != 0;
		boolean is_primitive = (runtimeAttributes & 4) != 0;
		boolean is_valuetype = (runtimeAttributes & 8) != 0;
		boolean is_enum = (runtimeAttributes & 16) != 0;
		boolean is_gtd = (runtimeAttributes & 32) != 0;
		boolean is_generic_type = (runtimeAttributes & 64) != 0;

		int nestedTypesSize = ps.readInt();
		TypeMirror[] nestedTypes = new TypeMirror[nestedTypesSize];
		for(int i = 0; i < nestedTypesSize; i++)
		{
			nestedTypes[i] = ps.readTypeMirror();
		}

		if(vm.isAtLeastVersion(2, 12))
		{
			generalType = ps.readTypeMirror();
		}

		if(vm.isAtLeastVersion(2, 15) && is_generic_type)
		{
			int n = ps.readInt();
			genericArguments = new TypeMirror[n];
			for(int i = 0; i < n; i++)
			{
				genericArguments[i] = ps.readTypeMirror();
			}
		}
	}
}
