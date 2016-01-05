package mono.debugger;

import org.jetbrains.annotations.NotNull;

/**
 * @author VISTALL
 * @since 05.01.2016
 */
public abstract class ValueTypeValueMirror<T> extends ValueImpl<T>
{
	private TypeMirror myTypeMirror;
	private Value[] myValues;

	public ValueTypeValueMirror(VirtualMachine aVm, @NotNull TypeMirror typeMirror, Value[] values)
	{
		super(aVm);
		myTypeMirror = typeMirror;
		myValues = values;
	}

	public abstract boolean isEnum();

	@NotNull
	public Value[] fieldValues()
	{
		return myValues;
	}

	@NotNull
	@Override
	public TypeMirror type()
	{
		return myTypeMirror;
	}
}
