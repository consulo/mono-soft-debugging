package mono.debugger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author VISTALL
 * @since 05.01.2016
 */
public class EnumValueMirror extends ValueTypeValueMirror<Value<?>>
{
	public EnumValueMirror(VirtualMachine aVm, @NotNull TypeMirror typeMirror, Value[] values)
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
	public void accept(@NotNull ValueVisitor valueVisitor)
	{
		valueVisitor.visitEnumValue(this);
	}
}
