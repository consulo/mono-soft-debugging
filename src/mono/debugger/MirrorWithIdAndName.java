package mono.debugger;

/**
 * @author VISTALL
 * @since 09.04.14
 */
public abstract class MirrorWithIdAndName extends MirrorImpl implements MirrorWithId
{
	private final long myId;
	private String myName;

	public MirrorWithIdAndName(VirtualMachine aVm, long id)
	{
		super(aVm);
		myId = id;
	}

	public String name()
	{
		if(myName == null)
		{
			try
			{
				myName = nameImpl();
			}
			catch(JDWPException e)
			{
				throw e.toJDIException();
			}
		}
		return myName;
	}


	protected abstract String nameImpl() throws JDWPException;

	@Override
	public long id()
	{
		return myId;
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj != null && obj.getClass() == getClass() && id() == ((MirrorWithIdAndName) obj).id();
	}
}
