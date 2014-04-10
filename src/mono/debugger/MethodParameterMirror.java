package mono.debugger;

import org.jetbrains.annotations.NotNull;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class MethodParameterMirror extends MirrorWithIdAndName
{
	private final TypeMirror myType;
	private final String myName;

	public MethodParameterMirror(VirtualMachineImpl vm, int i, TypeMirror type, String name)
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
