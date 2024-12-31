package mono.debugger;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 18.04.14
 */
public class VoidValueMirror extends ValueImpl<Void>
{
	public VoidValueMirror(VirtualMachine aVm)
	{
		super(aVm);
	}

	@Nullable
	@Override
	public TypeMirror type()
	{
		return virtualMachine().rootAppDomain().corlibAssembly().findTypeByQualifiedName(TypeTag.Void.getType(), false);
	}

	@Nullable
	@Override
	public Void value()
	{
		return null;
	}

	@Override
	public void accept(@Nonnull ValueVisitor valueVisitor)
	{

	}
}
