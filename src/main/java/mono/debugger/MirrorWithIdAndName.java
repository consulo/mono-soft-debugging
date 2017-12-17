package mono.debugger;

import org.jetbrains.annotations.NotNull;

/**
 * @author VISTALL
 * @since 09.04.14
 */
public abstract class MirrorWithIdAndName extends MirrorImpl implements MirrorWithId
{
	private final int myId;
	private String myName;

	public MirrorWithIdAndName(@NotNull VirtualMachine aVm, int id)
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
				throw e.asUncheckedException();
			}
		}
		return myName;
	}

	@NotNull
	protected abstract String nameImpl() throws JDWPException;

	@Override
	public int id()
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
		String name = myName;
		if(name == null)
		{
			name = "'not loaded'";
		}
		builder.append(getClass().getSimpleName()).append(" {").append(" id = ").append(id()).append(", name = ").append(name).append(" }");
		return builder.toString();
	}
}
