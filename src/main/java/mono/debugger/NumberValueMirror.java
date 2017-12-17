package mono.debugger;

import org.jetbrains.annotations.NotNull;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class NumberValueMirror extends ValueImpl<Number>
{
	private final int myTag;
	private final Number myValue;

	public NumberValueMirror(VirtualMachine aVm, int tag, @NotNull Number value)
	{
		super(aVm);
		myTag = tag;
		myValue = value;
	}

	@Override
	@NotNull
	public Number value()
	{
		return myValue;
	}

	@Override
	public TypeMirror type()
	{
		String type = TypeTag.typeByTag(myTag);
		return virtualMachine().rootAppDomain().corlibAssembly().findTypeByQualifiedName(type, false);
	}

	@Override
	public void accept(@NotNull ValueVisitor valueVisitor)
	{
		valueVisitor.visitNumberValue(this, value());
	}

	public int getTag()
	{
		return myTag;
	}
}
