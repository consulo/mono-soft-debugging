package mono.debugger;

/**
 * @author VISTALL
 * @since 08.04.14
 */
public class AssemblyReference extends ObjectReferenceImpl
{
	public AssemblyReference(VirtualMachine aVm, long aRef)
	{
		super(aVm, aRef);
	}
	@Override
	byte typeValueKey()
	{
		return JDWP.Tag.ASSEMBLY;
	}
}
