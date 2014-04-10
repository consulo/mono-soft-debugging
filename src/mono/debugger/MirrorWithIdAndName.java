package mono.debugger;

import org.jetbrains.annotations.NotNull;

/**
 * @author VISTALL
 * @since 09.04.14
 */
public abstract class MirrorWithIdAndName extends MirrorImpl implements MirrorWithId
{
	private final long myId;
	private String myName;

	public MirrorWithIdAndName(@NotNull VirtualMachine aVm, long id)
	{
		super(aVm);
		myId = id;
	}

	@NotNull
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

	@NotNull
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

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName()).append(" {").append(" id = ").append(id()).append(", name = ").append(name()).append(" }");
		return builder.toString();
	}
}
