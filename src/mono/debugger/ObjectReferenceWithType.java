package mono.debugger;

/**
 * @author VISTALL
 * @since 09.04.14
 */
public class ObjectReferenceWithType extends ObjectReferenceImpl
{
	private ReferenceType type = null;

	public ObjectReferenceWithType(VirtualMachine aVm, long aRef)
	{
		super(aVm, aRef);
	}

	@Override
	public ReferenceType referenceType()
	{
		if(type == null)
		{
			try
			{
				JDWP.ObjectReference.ReferenceType rtinfo = JDWP.ObjectReference.ReferenceType.process(vm, this);
				type = vm.referenceType(rtinfo.typeID);
			}
			catch(JDWPException exc)
			{
				throw exc.toJDIException();
			}
		}
		return type;
	}
}
