package mono.debugger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import mono.debugger.protocol.Type_GetFields;
import mono.debugger.protocol.Type_GetInfo;
import mono.debugger.protocol.Type_GetMethods;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class TypeMirror extends MirrorWithIdAndName implements MirrorWithId
{
	private Type_GetInfo myInfo;
	private MethodMirror[] myMethodMirrors;
	private FieldMirror[] myFieldMirrors;

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

	@Nullable
	public TypeMirror baseType()
	{
		return info().baseType;
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

	@NotNull
	public MethodMirror[] methods()
	{
		if(myMethodMirrors != null)
		{
			return myMethodMirrors;
		}
		try
		{
			return myMethodMirrors = Type_GetMethods.process(vm, this).methods;
		}
		catch(JDWPException e)
		{
			throw e.toJDIException();
		}
	}

	@NotNull
	public FieldMirror[] fields()
	{
		if(myFieldMirrors != null)
		{
			return myFieldMirrors;
		}
		try
		{
			return myFieldMirrors = Type_GetFields.process(vm, this).fields;
		}
		catch(JDWPException e)
		{
			throw e.toJDIException();
		}
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName()).append(" {").append(" id = ").append(id()).append(", fullName = ").append(qualifiedName()).append(" }");
		return builder.toString();
	}
}
