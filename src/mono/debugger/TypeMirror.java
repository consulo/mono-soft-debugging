package mono.debugger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import edu.arizona.cs.mbel.signature.TypeAttributes;
import mono.debugger.protocol.Type_GetCustomAttributes;
import mono.debugger.protocol.Type_GetFields;
import mono.debugger.protocol.Type_GetInfo;
import mono.debugger.protocol.Type_GetInterfaces;
import mono.debugger.protocol.Type_GetMethods;
import mono.debugger.protocol.Type_GetProperties;
import mono.debugger.protocol.Type_IsAssignableFrom;
import mono.debugger.util.BitUtil;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class TypeMirror extends CustomAttributeMirrorOwner implements MirrorWithId, GenericTarget<TypeMirror>
{
	public static final TypeMirror[] EMPTY_ARRAY = new TypeMirror[0];
	private static final int[] ourNestedAttributes = new int[]{
			TypeAttributes.NestedPublic,
			TypeAttributes.NestedPrivate,
			TypeAttributes.NestedFamily,
			TypeAttributes.NestedAssembly,
			TypeAttributes.NestedFamANDAssem,
			TypeAttributes.NestedFamORAssem
	};

	private final TypeMirror myParent;
	private Type_GetInfo myInfo;
	private MethodMirror[] myMethodMirrors;
	private FieldMirror[] myFieldMirrors;
	private PropertyMirror[] myProperties;
	private TypeMirror[] myInterfaces;

	public TypeMirror(@NotNull VirtualMachine aVm, @Nullable TypeMirror parent, int id)
	{
		super(aVm, id);
		myParent = parent;
	}

	@Nullable
	public AssemblyMirror assembly()
	{
		return info().assemblyMirror;
	}

	@NotNull
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
				throw e.asUncheckedException();
			}
		}
		return myInfo;
	}

	public boolean isNested()
	{
		for(int nestedAttribute : ourNestedAttributes)
		{
			if(BitUtil.isSet(info().attributes, TypeAttributes.VisibilityMask, nestedAttribute))
			{
				return true;
			}
		}
		return false;
	}

	public boolean isAssignableFrom(@NotNull TypeMirror typeMirror)
	{
		try
		{
			return Type_IsAssignableFrom.process(vm, this, typeMirror).value;
		}
		catch(JDWPException e)
		{
			throw e.asUncheckedException();
		}
	}

	@Nullable
	public TypeMirror parentType()
	{
		return myParent;
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
		TypeMirror parentType = parentType();
		if(parentType != null)
		{
			return parentType.qualifiedName() + "/" + name();
		}

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

	@NotNull
	public TypeMirror[] nestedTypes()
	{
		return info().nestedTypes;
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
			throw e.asUncheckedException();
		}
	}

	@NotNull
	public TypeMirror[] getInterfaces()
	{
		if(myInterfaces != null)
		{
			return myInterfaces;
		}
		try
		{
			if(virtualMachine().isAtLeastVersion(2, 11))
			{
				return myInterfaces = Type_GetInterfaces.process(vm, this).interfaces;
			}
			else
			{
				return myInterfaces = TypeMirror.EMPTY_ARRAY;
			}
		}
		catch(JDWPException e)
		{
			throw e.asUncheckedException();
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
		loop:
		for(MethodMirror methodMirror : methods())
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
						if(!parameter.type().isAssignableFrom(expectedType))
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
	public CustomAttributeMirror[] customAttributesImpl() throws JDWPException
	{
		return Type_GetCustomAttributes.process(vm, this).customAttributeMirrors;
	}

	@Override
	public boolean isStatic()
	{
		return !isNested();
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
			throw e.asUncheckedException();
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
			throw e.asUncheckedException();
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
