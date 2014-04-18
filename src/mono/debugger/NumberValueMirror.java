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
		switch(myTag)
		{
			case SignatureConstants.ELEMENT_TYPE_I1:
				return virtualMachine().rootAppDomain().corlibAssembly().findTypeByQualifiedName("System.SByte", false);
			case SignatureConstants.ELEMENT_TYPE_I2:
				return virtualMachine().rootAppDomain().corlibAssembly().findTypeByQualifiedName("System.Short", false);
			case SignatureConstants.ELEMENT_TYPE_I4:
				return virtualMachine().rootAppDomain().corlibAssembly().findTypeByQualifiedName("System.Int32", false);
		}
		return null;
	}

	@Override
	public void accept(@NotNull ValueVisitor valueVisitor)
	{
		valueVisitor.visitNumberValue(this, value());
	}

	public byte getTag()
	{
		return myTag;
	}
}
