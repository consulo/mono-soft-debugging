package mono.debugger;

import org.jetbrains.annotations.Nullable;
import edu.arizona.cs.mbel.signature.SignatureConstants;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class PrimitiveValueMirror extends ValueImpl
{
	private final byte myTag;
	private final Object myValue;

	public PrimitiveValueMirror(VirtualMachine aVm, byte tag, Object value)
	{
		super(aVm);
		myTag = tag;
		myValue = value;
	}

	@Nullable
	public Object value()
	{
		return myValue;
	}

	@Override
	public TypeMirror type()
	{
		switch(myTag)
		{
			case SignatureConstants.ELEMENT_TYPE_I4:
				TypeMirror[] types = vm.findTypes("System.Int32", false);
				return types[0];
		}
		return null;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Value { type = ").append(type()).append(", value = ").append(value()).append(" }");
		return builder.toString();
	}
}
