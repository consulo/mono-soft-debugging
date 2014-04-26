package mono.debugger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import mono.debugger.protocol.Type_GetFields;
import mono.debugger.protocol.Type_GetInfo;
import mono.debugger.protocol.Type_GetMethods;
import mono.debugger.protocol.Type_GetProperties;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class TypeMirror extends MirrorWithIdAndName implements MirrorWithId, GenericTarget<TypeMirror>
{
	public static final TypeMirror[] EMPTY_ARRAY = new TypeMirror[0];

	private Type_GetInfo myInfo;
	private MethodMirror[] myMethodMirrors;
	private FieldMirror[] myFieldMirrors;
	private PropertyMirror[] myProperties;

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

	/**
	 * In most case it ill return same as {@link #qualifiedName()}. But if it runtime copy - it ill different
	 */
	@NotNull
	public String fullName()
	{
		return info().fullName;
	}

	@NotNull
	public String qualifiedName()
	{
		String namespace = namespace();
		if(namespace.isEmpty())
		{
			return name();
		}
		return namespace + "." + name();
	}

	@Nullable
	@Override
	public TypeMirror original()
	{
		return info().generalType;
	}

	@NotNull
	@Override
	public TypeMirror[] genericArguments()
	{
		return info().genericArguments;
	}

	public boolean isArray()
	{
		return info().rank > 0;
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

	@Nullable
	public MethodMirror findMethodByName(@NotNull String name, boolean deep)
	{
		return findMethodByName(name, deep, EMPTY_ARRAY);
	}

	@Nullable
	public MethodMirror findMethodByName(@NotNull String name, boolean deep, TypeMirror[] expectedParameters)
	{
		loop:for(MethodMirror methodMirror : methods())
		{
			if(methodMirror.name().equals(name))
			{
				MethodParameterMirror[] parameters = methodMirror.parameters();
				if(parameters.length == expectedParameters.length)
				{
					for(int i = 0; i < parameters.length; i++)
					{
						MethodParameterMirror parameter = parameters[i];
						TypeMirror expectedType = expectedParameters[i];
						if(!expectedType.qualifiedName().equals(parameter.type().qualifiedName()))
						{
							continue loop;
						}
					}

					return methodMirror;
				}
			}
		}

		if(deep)
		{
			TypeMirror baseType = baseType();
			if(baseType != null)
			{
				return baseType.findMethodByName(name, true, expectedParameters);
			}
		}
		return null;
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

	@NotNull
	public List<FieldMirror> fieldsDeep()
	{
		List<FieldMirror> list = new ArrayList<FieldMirror>();
		Collections.addAll(list, fields());
		TypeMirror typeMirror = baseType();
		if(typeMirror != null)
		{
			list.addAll(typeMirror.fieldsDeep());
		}
		return list;
	}

	@NotNull
	public PropertyMirror[] properties()
	{
		if(myProperties != null)
		{
			return myProperties;
		}
		try
		{
			return myProperties = Type_GetProperties.process(vm, this).properties;
		}
		catch(JDWPException e)
		{
			throw e.toJDIException();
		}
	}

	@NotNull
	public List<FieldOrPropertyMirror> fieldAndProperties(boolean deep)
	{
		List<FieldOrPropertyMirror> mirrors = new ArrayList<FieldOrPropertyMirror>();
		collectFieldAndProperties(this, mirrors, deep);
		Collections.sort(mirrors, new Comparator<FieldOrPropertyMirror>()
		{
			@Override
			public int compare(FieldOrPropertyMirror o1, FieldOrPropertyMirror o2)
			{
				if(o1 instanceof PropertyMirror)
				{
					return 1;
				}
				return 0;
			}
		});
		return mirrors;
	}

	private static void collectFieldAndProperties(TypeMirror typeMirror, List<FieldOrPropertyMirror> fieldOrPropertyMirrors, boolean deep)
	{
		FieldMirror[] fields = typeMirror.fields();
		PropertyMirror[] properties = typeMirror.properties();

		Collections.addAll(fieldOrPropertyMirrors, properties);

		for(FieldMirror field : fields)
		{
			if(field.name().startsWith("<"))
			{
				continue;
			}
			fieldOrPropertyMirrors.add(field);
		}

		if(deep)
		{
			TypeMirror b = typeMirror.baseType();
			if(b != null)
			{
				collectFieldAndProperties(b, fieldOrPropertyMirrors, true);
			}
		}
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName()).append(" {").append(" id = ").append(id()).append(", fullName = ").append(qualifiedName()).append
				(" }");
		return builder.toString();
	}
}
