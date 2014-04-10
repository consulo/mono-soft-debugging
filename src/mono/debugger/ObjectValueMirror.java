package mono.debugger;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class ObjectValueMirror extends ValueImpl implements MirrorWithId
{
	private final long myId;

	public ObjectValueMirror(VirtualMachine aVm, long id)
	{
		super(aVm);
		myId = id;
	}

	@Override
	public long id()
	{
		return myId;
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
}
