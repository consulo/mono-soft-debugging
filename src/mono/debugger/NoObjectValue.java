package mono.debugger;

import org.jetbrains.annotations.NotNull;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class NoObjectValue extends ValueImpl<Object>
{
	public NoObjectValue(VirtualMachine aVm)
	{
		super(aVm);
	}

	@Override
	public Object value()
	{
		return "<static>";
	}

	@Override
	public TypeMirror type()
	{
		return null;
	}

	@Override
	public void accept(@NotNull ValueVisitor valueVisitor)
	{
		valueVisitor.visitNoObjectValue(this);
	}
}
