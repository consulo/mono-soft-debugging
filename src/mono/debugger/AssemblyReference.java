package mono.debugger;

/**
 * @author VISTALL
 * @since 08.04.14
 */
public class AssemblyReference extends ObjectReferenceImpl
{
	private String myName;

	public AssemblyReference(VirtualMachine aVm, long aRef)
	{
		super(aVm, aRef);
	}

	public String name()
	{
		if(myName == null)
		{
			try
			{
				myName = JDWP.Assembly.Name.process(vm, this).name;
			}
			catch(JDWPException exc)
			{
				throw exc.toJDIException();
			}
		}
		return myName;
	}

	@Override
	byte typeValueKey()
	{
		return JDWP.Tag.ASSEMBLY;
	}
}
