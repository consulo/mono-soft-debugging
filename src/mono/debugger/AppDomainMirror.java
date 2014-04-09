package mono.debugger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

	public AppDomainMirror(@NotNull VirtualMachine aVm, long aRef)
	{
		super(aVm, aRef);
	}

	@Nullable
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

	@NotNull
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

	@NotNull
	@Override
	protected String nameImpl() throws JDWPException
	{
		return AppDomain_GetFriendlyName.process(vm, this).name;
	}
}
