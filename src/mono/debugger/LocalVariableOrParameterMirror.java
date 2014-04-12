package mono.debugger;

import org.jetbrains.annotations.NotNull;

/**
 * @author VISTALL
 * @since 12.04.14
 */
public abstract class LocalVariableOrParameterMirror  extends MirrorWithIdAndName
{
	private final TypeMirror myType;
	private final String myName;

	public LocalVariableOrParameterMirror(VirtualMachineImpl vm, int i, TypeMirror type, String name)
	{
		super(vm, i);
		myType = type;
		myName = name;
	}

	@NotNull
	public TypeMirror type()
	{
		return myType;
	}

	public abstract int idForStackFrame();

	@NotNull
	@Override
	protected String nameImpl() throws JDWPException
	{
		return myName;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName()).append(" {").append(" id = ").append(id()).append(", name = ").append(name()).append(", type = ")
				.append(type()).append(" }");
		return builder.toString();
	}
}
