package mono.debugger;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import mono.debugger.protocol.Assembly_GetLocation;
import mono.debugger.protocol.Assembly_GetName;
import mono.debugger.protocol.Assembly_GetType;

/**
 * @author VISTALL
 * @since 08.04.14
 */
public class AssemblyMirror extends MirrorWithIdAndName
{
	private String myLocation;

	public AssemblyMirror(@Nonnull VirtualMachine aVm, int aRef)
	{
		super(aVm, aRef);
	}

	@Nonnull
	@Override
	protected String nameImpl() throws JDWPException
	{
		return Assembly_GetName.process(vm, this).name;
	}

	@Nullable
	public TypeMirror findTypeByQualifiedName(@Nonnull String name, boolean ignoreCase)
	{
		try
		{
			return Assembly_GetType.process(vm, this,name, ignoreCase).type;
		}
		catch(JDWPException e)
		{
			throw e.asUncheckedException();
		}
	}

	@Nonnull
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
				throw exc.asUncheckedException();
			}
		}
		return myLocation;
	}
}
