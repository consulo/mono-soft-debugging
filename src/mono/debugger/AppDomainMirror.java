package mono.debugger;

import mono.debugger.protocol.AppDomain_GetAssemblies;
import mono.debugger.protocol.AppDomain_GetEntryAssembly;
import mono.debugger.protocol.AppDomain_GetFriendlyName;

/**
 * @author VISTALL
 * @since 09.04.14
 */
public class AppDomainMirror extends MirrorWithIdAndName
{
	private AssemblyMirror myEntryAssemblyMirror;
	private AssemblyMirror[] myAssemblyMirrors;

	public AppDomainMirror(VirtualMachine aVm, long aRef)
	{
		super(aVm, aRef);
	}

	public AssemblyMirror entryAssembly()
	{
		if(myEntryAssemblyMirror == null)
		{
			try
			{
				myEntryAssemblyMirror = AppDomain_GetEntryAssembly.process(vm, this).assembly;
			}
			catch(JDWPException e)
			{
				throw e.toJDIException();
			}
		}
		return myEntryAssemblyMirror;
	}

	public AssemblyMirror[] assemblies()
	{
		if(myAssemblyMirrors == null)
		{
			try
			{
				myAssemblyMirrors = AppDomain_GetAssemblies.process(vm, this).assemblies;
			}
			catch(JDWPException e)
			{
				throw e.toJDIException();
			}
		}
		return myAssemblyMirrors;
	}

	@Override
	protected String nameImpl() throws JDWPException
	{
		return AppDomain_GetFriendlyName.process(vm, this).name;
	}
}
