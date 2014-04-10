package mono.debugger;

import org.jetbrains.annotations.NotNull;
import mono.debugger.protocol.Type_GetInfo;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class TypeMirror extends MirrorWithIdAndName implements MirrorWithId
{
	private Type_GetInfo myInfo;

	public TypeMirror(@NotNull VirtualMachine aVm, long id)
	{
		super(aVm, id);
	}

	private Type_GetInfo info()
	{
		if(myInfo == null)
		{
			try
			{
				myInfo = Type_GetInfo.process(vm, this);
			}
			catch(JDWPException e)
			{
				throw e.toJDIException();
			}
		}
		return myInfo;
	}

	@NotNull
	@Override
	protected String nameImpl() throws JDWPException
	{
		return info().name;
	}

	@NotNull
	public String qualifiedName()
	{
		return info().fullName;
	}

	@NotNull
	public String namespace()
	{
		return info().namespace;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName()).append(" {").append(" id = ").append(id()).append(", fullName = ").append(qualifiedName()).append(" }");
		return builder.toString();
	}
}
