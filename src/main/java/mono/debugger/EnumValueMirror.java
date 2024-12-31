package mono.debugger;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 05.01.2016
 */
public class EnumValueMirror extends ValueTypeValueMirror<Value<?>>
{
	public EnumValueMirror(VirtualMachine aVm, @Nonnull TypeMirror typeMirror, Value[] values)
	{
		super(aVm, typeMirror, values);
	}

	@Override
	public boolean isEnum()
	{
		return true;
	}

	@Nullable
	@Override
	public Value<?> value()
	{
		return fieldValues()[0];
	}

	@Override
	public void accept(@Nonnull ValueVisitor valueVisitor)
	{
		valueVisitor.visitEnumValue(this);
	}
}
