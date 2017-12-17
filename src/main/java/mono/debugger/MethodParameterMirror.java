package mono.debugger;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class MethodParameterMirror extends LocalVariableOrParameterMirror
{
	public MethodParameterMirror(VirtualMachineImpl vm, int i, TypeMirror type, String name)
	{
		super(vm, i, type, name);
	}

	@Override
	public int idForStackFrame()
	{
		return (int) -(id() + 1);
	}
}
