package mono.debugger;

import org.jetbrains.annotations.NotNull;
import mono.debugger.protocol.Assembly_GetLocation;
import mono.debugger.protocol.Assembly_GetName;

/**
 * @author VISTALL
 * @since 08.04.14
 */
public class AssemblyMirror extends MirrorWithIdAndName
{
	private String myLocation;

	public AssemblyMirror(@NotNull VirtualMachine aVm, long aRef)
	{
		super(aVm, aRef);
	}

	@NotNull
	@Override
	protected String nameImpl() throws JDWPException
	{
		return Assembly_GetName.process(vm, this).name;
	}

	@NotNull
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
}
