package mono.debugger;

import org.jetbrains.annotations.NotNull;
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

	@NotNull
	public ObjectValueMirror object()
	{
		return myObjectValueMirror;
	}

	@NotNull
	@Override
	public String value()
	{
		try
		{
			return StringReference_GetValue.process(vm, myObjectValueMirror).value;
		}
		catch(JDWPException e)
		{
			throw e.toJDIException();
		}
	}

	@Override
	public TypeMirror type()
	{
		return virtualMachine().rootAppDomain().corlibAssembly().findTypeByQualifiedName(TypeTag.String.getType(), false);
	}

	@Override
	public void accept(@NotNull ValueVisitor valueVisitor)
	{
		valueVisitor.visitStringValue(this, value());
	}
}
