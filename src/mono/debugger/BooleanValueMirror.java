package mono.debugger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author VISTALL
 * @since 18.04.14
 */
public class BooleanValueMirror extends ValueImpl<Boolean>
{
	private final Boolean myValue;

	public BooleanValueMirror(VirtualMachine aVm, @NotNull Boolean value)
	{
		super(aVm);
		myValue = value;
	}

	@Nullable
	@Override
	public TypeMirror type()
	{
		return virtualMachine().rootAppDomain().corlibAssembly().findTypeByQualifiedName("System.Boolean", false);
	}

	@NotNull
	@Override
	public Boolean value()
	{
		return myValue;
	}

	@Override
	public void accept(@NotNull ValueVisitor valueVisitor)
	{
		valueVisitor.visitBooleanValue(this, value());
	}
}
