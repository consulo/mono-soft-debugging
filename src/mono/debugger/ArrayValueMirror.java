package mono.debugger;

import mono.debugger.protocol.ArrayReference_GetLength;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class ArrayValueMirror extends ValueImpl
{
	private final byte myTag;
	private final ObjectMirror myObjectMirror;

	public ArrayValueMirror(VirtualMachine aVm, byte tag, ObjectMirror objectMirror)
	{
		super(aVm);
		myTag = tag;
		myObjectMirror = objectMirror;
	}

	public int length()
	{
		try
		{
			ArrayReference_GetLength.process(vm, myObjectMirror);
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
}
