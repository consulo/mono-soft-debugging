package mono.debugger;

import org.jetbrains.annotations.NotNull;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class NoObjectValue extends ValueImpl<Object> implements MirrorWithId
{
	public NoObjectValue(VirtualMachine aVm)
	{
		super(aVm);
	}

	@Override
	public Object value()
	{
		return "<no object>";
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

	@Override
	public long id()
	{
		return 0;
	}
}
