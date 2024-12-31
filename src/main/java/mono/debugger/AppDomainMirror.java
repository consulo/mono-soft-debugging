package mono.debugger;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import mono.debugger.protocol.AppDomain_CreateBoxValue;
import mono.debugger.protocol.AppDomain_CreateString;
import mono.debugger.protocol.AppDomain_GetAssemblies;
import mono.debugger.protocol.AppDomain_GetCorlib;
import mono.debugger.protocol.AppDomain_GetEntryAssembly;
import mono.debugger.protocol.AppDomain_GetFriendlyName;

/**
 * @author VISTALL
 * @since 09.04.14
 */
public class AppDomainMirror extends MirrorWithIdAndName
{
	private AssemblyMirror myEntryAssemblyMirror;
	private AssemblyMirror myCorlibAssemblyMirror;
	private AssemblyMirror[] myAssemblyMirrors;

	public AppDomainMirror(@Nonnull VirtualMachine aVm, int aRef)
	{
		super(aVm, aRef);
	}

	@Nonnull
	public StringValueMirror createString(@Nonnull String str)
	{
		try
		{
			return AppDomain_CreateString.process(vm, this, str).value;
		}
		catch(JDWPException e)
		{
			throw e.asUncheckedException();
		}
	}

	@Nonnull
	public ObjectValueMirror createBoxValue(int tag, @Nonnull Number value)
	{
		try
		{
			return AppDomain_CreateBoxValue.process(vm, this, tag, value).value;
		}
		catch(JDWPException e)
		{
			throw e.asUncheckedException();
		}
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
				throw e.asUncheckedException();
			}
		}
		return myEntryAssemblyMirror;
	}

	@Nonnull
	public AssemblyMirror corlibAssembly()
	{
		if(myCorlibAssemblyMirror == null)
		{
			try
			{
				myCorlibAssemblyMirror = AppDomain_GetCorlib.process(vm, this).assembly;
			}
			catch(JDWPException e)
			{
				throw e.asUncheckedException();
			}
		}
		return myCorlibAssemblyMirror;
	}

	@Nonnull
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
				throw e.asUncheckedException();
			}
		}
		return myAssemblyMirrors;
	}

	@Nonnull
	@Override
	protected String nameImpl() throws JDWPException
	{
		return AppDomain_GetFriendlyName.process(vm, this).name;
	}
}
