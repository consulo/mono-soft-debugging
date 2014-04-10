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
	ValueImpl prepareForAssignmentTo(ValueContainer destination) throws InvalidTypeException, ClassNotLoadedException
	{
		return null;
	}

	@Override
	int typeValueKey()
	{
		return 0;
	}

	@Override
	public Type type()
	{
		return null;
	}

	@Override
	public String toString()
	{
		return "<static>";
	}
}
