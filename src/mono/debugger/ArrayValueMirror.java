package mono.debugger;

import org.jetbrains.annotations.NotNull;
import mono.debugger.protocol.ArrayReference_GetLength;
import mono.debugger.protocol.ArrayReference_GetValues;
import mono.debugger.protocol.ArrayReference_SetValues;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class ArrayValueMirror extends ValueImpl<Object> implements MirrorWithId
{
	private final ObjectValueMirror myObjectValueMirror;
	private ArrayReference_GetLength.DimensionInfo[] myInfos;

	public ArrayValueMirror(VirtualMachine aVm, ObjectValueMirror objectValueMirror)
	{
		super(aVm);
		myObjectValueMirror = objectValueMirror;
	}

	@NotNull
	private ArrayReference_GetLength.DimensionInfo[] dimensionInfos()
	{
		if(myInfos != null)
		{
			return myInfos;
		}
		try
		{
			myInfos = ArrayReference_GetLength.process(vm, myObjectValueMirror).dimensions;
			return myInfos;
		}
		catch(JDWPException e)
		{
			throw e.toJDIException();
		}
	}

	@NotNull
	public Value<?> get(int index)
	{
		try
		{
			return ArrayReference_GetValues.process(vm, myObjectValueMirror, index, 1).values[0];
		}
		catch(JDWPException e)
		{
			throw e.toJDIException();
		}
	}

	public void set(int index, @NotNull Value<?> value)
	{
		try
		{
			ArrayReference_SetValues.process(vm, myObjectValueMirror,index, new Value[] {value});
		}
		catch(JDWPException e)
		{
			throw e.toJDIException();
		}
	}

	public int length()
	{
		return length(0);
	}

	public int length(int dimension)
	{
		return dimensionInfos()[dimension].size;
	}

	public int dimensionSize()
	{
		return myInfos.length;
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

	@NotNull
	public ObjectValueMirror object()
	{
		return myObjectValueMirror;
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
		builder.append("ArrayValue { type = ").append(type()).append(", length = ").append(length(0)).append(" }");
		return builder.toString();
	}

	@Override
	public long id()
	{
		return myObjectValueMirror.id();
	}
}
