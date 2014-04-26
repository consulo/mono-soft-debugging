package mono.debugger;

import org.jetbrains.annotations.NotNull;
import mono.debugger.protocol.ArrayReference_GetLength;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class ArrayValueMirror extends ValueImpl<Object> implements MirrorWithId
{
	private final ObjectValueMirror myObjectValueMirror;

	public ArrayValueMirror(VirtualMachine aVm, ObjectValueMirror objectValueMirror)
	{
		super(aVm);
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
	public TypeMirror type()
	{
		return myObjectValueMirror.type();
	}

	@Override
	public void accept(@NotNull ValueVisitor valueVisitor)
	{
		valueVisitor.visitArrayValue(this);
	}

	@Override
	public Object value()
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
