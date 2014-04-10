package mono.debugger;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class NoObjectValue extends ValueImpl
{
	public NoObjectValue(VirtualMachine aVm)
	{
		super(aVm);
	}
	@Override
	public TypeMirror type()
	{
		return null;
	}

	@Override
	public String toString()
	{
		return "<static>";
	}
}
