package mono.debugger;

import mono.debugger.protocol.ArrayReference_GetLength;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class ArrayValueMirror extends ValueImpl implements MirrorWithId
{
	private final byte myTag;
	private final ObjectValueMirror myObjectValueMirror;

	public ArrayValueMirror(VirtualMachine aVm, byte tag, ObjectValueMirror objectValueMirror)
	{
		super(aVm);
		myTag = tag;
		myObjectValueMirror = objectValueMirror;
	}

	public int length()
	{
		try
		{
			ArrayReference_GetLength.process(vm, myObjectValueMirror);
		}
		catch(JDWPException e)
		{
			throw e.toJDIException();
		}
		return 0;
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

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("ArrayValue { type = ").append(type()).append(", length = ").append(length()).append(" }");
		return builder.toString();
	}

	@Override
	public long id()
	{
		return myObjectValueMirror.id();
	}
}
