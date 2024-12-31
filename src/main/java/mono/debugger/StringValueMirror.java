package mono.debugger;

import jakarta.annotation.Nonnull;

import mono.debugger.protocol.StringReference_GetValue;

/**
 * @author VISTALL
 * @since 11.04.14
 */
public class StringValueMirror extends ValueImpl<String>
{
	private final ObjectValueMirror myObjectValueMirror;

	public StringValueMirror(VirtualMachine aVm, ObjectValueMirror objectValueMirror)
	{
		super(aVm);
		myObjectValueMirror = objectValueMirror;
	}

	@Nonnull
	public ObjectValueMirror object()
	{
		return myObjectValueMirror;
	}

	@Nonnull
	@Override
	public String value()
	{
		try
		{
			return StringReference_GetValue.process(vm, myObjectValueMirror).value;
		}
		catch(JDWPException e)
		{
			throw e.asUncheckedException();
		}
	}

	@Override
	public TypeMirror type()
	{
		return virtualMachine().rootAppDomain().corlibAssembly().findTypeByQualifiedName(TypeTag.String.getType(), false);
	}

	@Override
	public void accept(@Nonnull ValueVisitor valueVisitor)
	{
		valueVisitor.visitStringValue(this, value());
	}
}
