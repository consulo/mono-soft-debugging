package mono.debugger;

import mono.debugger.protocol.AppDomain_GetFriendlyName;

/**
 * @author VISTALL
 * @since 09.04.14
 */
public class AppDomainReference extends ObjectReferenceImpl
{
	private String myName;

	public AppDomainReference(VirtualMachine aVm, long aRef)
	{
		super(aVm, aRef);
	}

	public String name()
	{
		if(myName == null)
		{
			try
			{
				myName = AppDomain_GetFriendlyName.process(vm, this).name;
			}
			catch(JDWPException e)
			{
				throw e.toJDIException();
			}
		}
		return myName;
	}

	@Override
	int typeValueKey()
	{
		return JDWP.Tag.APP_DOMAIN;
	}
}
