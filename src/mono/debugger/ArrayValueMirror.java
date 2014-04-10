package mono.debugger;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class ArrayValueMirror extends ValueImpl
{
	private final byte myTag;
	private final long myObjectRef;

	public ArrayValueMirror(VirtualMachine aVm, byte tag, long objectRef)
	{
		super(aVm);
		myTag = tag;
		myObjectRef = objectRef;
	}

	@Override
	ValueImpl prepareForAssignmentTo(ValueContainer destination) throws InvalidTypeException, ClassNotLoadedException
	{
		return null;
	}

	@Override
	int typeValueKey()
	{
		return myTag;
	}

	@Override
	public Type type()
	{
		return null;
	}
}
