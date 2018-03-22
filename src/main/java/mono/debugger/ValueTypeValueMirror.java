package mono.debugger;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 05.01.2016
 */
public abstract class ValueTypeValueMirror<T> extends ValueImpl<T>
{
	private TypeMirror myTypeMirror;
	private Value[] myValues;

	public ValueTypeValueMirror(VirtualMachine aVm, @Nonnull TypeMirror typeMirror, Value[] values)
	{
		super(aVm);
		myTypeMirror = typeMirror;
		myValues = values;
	}

	public abstract boolean isEnum();

	@Nonnull
	public Value[] fieldValues()
	{
		return myValues;
	}

	@Nonnull
	@Override
	public TypeMirror type()
	{
		return myTypeMirror;
	}
}
