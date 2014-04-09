package mono.debugger;

import mono.debugger.protocol.Assembly_GetLocation;
import mono.debugger.protocol.Assembly_GetName;

/**
 * @author VISTALL
 * @since 08.04.14
 */
public class AssemblyMirror extends MirrorWithIdAndName
{
	private String myLocation;

	public AssemblyMirror(VirtualMachine aVm, long aRef)
	{
		super(aVm, aRef);
	}

	@Override
	protected String nameImpl() throws JDWPException
	{
		return Assembly_GetName.process(vm, this).name;
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
}
