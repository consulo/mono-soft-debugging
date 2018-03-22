package mono.debugger;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class NoObjectValueMirror extends ValueImpl<Object> implements MirrorWithId
{
	public NoObjectValueMirror(VirtualMachine aVm)
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
	public void accept(@Nonnull ValueVisitor valueVisitor)
	{
		valueVisitor.visitNoObjectValue(this);
	}

	@Override
	public int id()
	{
		return 0;
	}
}
