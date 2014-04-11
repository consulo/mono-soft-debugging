package mono.debugger;

import org.jetbrains.annotations.NotNull;

/**
 * @author VISTALL
 * @since 11.04.14
 */
public class PropertyMirror extends FieldOrPropertyMirror
{
	private final MethodMirror myGetMethod;
	private final MethodMirror mySetMethod;

	public PropertyMirror(
			@NotNull VirtualMachine aVm,
			long id,
			@NotNull String name,
			MethodMirror getMethod,
			MethodMirror setMethod,
			@NotNull TypeMirror parent,
			int attributes)
	{
		super(aVm, id, parent, attributes, name);
		myGetMethod = getMethod;
		mySetMethod = setMethod;
	}

	public MethodMirror methodGet()
	{
		return myGetMethod;
	}

	public MethodMirror methodSet()
	{
		return mySetMethod;
	}

	@NotNull
	public FieldMirror field()
	{
		TypeMirror parent = parent();
		for(FieldMirror fieldMirror : parent.fields())
		{
			if(fieldMirror.id() == id())
			{
				return fieldMirror;
			}
		}
		throw new IllegalArgumentException();
	}

	@Override
	@NotNull
	public TypeMirror type()
	{
		return field().type();
	}

	@Override
	public boolean isStatic()
	{
		return field().isStatic();
	}
}
