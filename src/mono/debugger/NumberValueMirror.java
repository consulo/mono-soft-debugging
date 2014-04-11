package mono.debugger;

import org.jetbrains.annotations.NotNull;
import edu.arizona.cs.mbel.signature.SignatureConstants;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class NumberValueMirror extends ValueImpl<Number>
{
	private final byte myTag;
	private final Number myValue;

	public NumberValueMirror(VirtualMachine aVm, byte tag, @NotNull Number value)
	{
		super(aVm);
		myTag = tag;
		myValue = value;
	}

	@Override
	@NotNull
	public Number value()
	{
		return myValue;
	}

	@Override
	public TypeMirror type()
	{
		TypeMirror[] types = null;
		switch(myTag)
		{
			case SignatureConstants.ELEMENT_TYPE_I1:
				types = vm.findTypes("System.SByte", false);
				return types[0];
			case SignatureConstants.ELEMENT_TYPE_I2:
				types = vm.findTypes("System.Short", false);
				return types[0];
			case SignatureConstants.ELEMENT_TYPE_I4:
				types = vm.findTypes("System.Int32", false);
				return types[0];
		}
		return null;
	}

	@Override
	public void accept(@NotNull ValueVisitor valueVisitor)
	{
		valueVisitor.visitNumberValue(this, value());
	}
}
