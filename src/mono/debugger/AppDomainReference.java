package mono.debugger;

import mono.debugger.protocol.AppDomain_GetAssemblies;
import mono.debugger.protocol.AppDomain_GetEntryAssembly;
import mono.debugger.protocol.AppDomain_GetFriendlyName;

/**
 * @author VISTALL
 * @since 09.04.14
 */
public class AppDomainReference extends ObjectReferenceImpl
{
	private String myName;
	private AssemblyReference myEntryAssemblyReference;
	private AssemblyReference[] myAssemblyReferences;

	public AppDomainReference(VirtualMachine aVm, long aRef)
	{
		super(aVm, aRef);
	}

	public AssemblyReference entryAssembly()
	{
		if(myEntryAssemblyReference == null)
		{
			try
			{
				myEntryAssemblyReference = AppDomain_GetEntryAssembly.process(vm, this).assembly;
			}
			catch(JDWPException e)
			{
				throw e.toJDIException();
			}
		}
		return myEntryAssemblyReference;
	}

	public AssemblyReference[] assemblies()
	{
		if(myAssemblyReferences == null)
		{
			try
			{
				myAssemblyReferences = AppDomain_GetAssemblies.process(vm, this).assemblies;
			}
			catch(JDWPException e)
			{
				throw e.toJDIException();
			}
		}
		return myAssemblyReferences;
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
