package mono.debugger;

import mono.debugger.protocol.Assembly_GetLocation;
import mono.debugger.protocol.Assembly_GetName;

/**
 * @author VISTALL
 * @since 08.04.14
 */
public class AssemblyReference extends ObjectReferenceImpl
{
	private String myName;
	private String myLocation;

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
				myName = Assembly_GetName.process(vm, this).name;
			}
			catch(JDWPException exc)
			{
				throw exc.toJDIException();
			}
		}
		return myName;
	}

	public String location()
	{
		if(myLocation == null)
		{
			try
			{
				myLocation = Assembly_GetLocation.process(vm, this).location;
			}
			catch(JDWPException exc)
			{
				throw exc.toJDIException();
			}
		}
		return myLocation;
	}

	@Override
	int typeValueKey()
	{
		return JDWP.Tag.ASSEMBLY;
	}
}
