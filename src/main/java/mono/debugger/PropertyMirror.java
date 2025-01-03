package mono.debugger;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import mono.debugger.protocol.Type_GetPropertyCustomAttributes;

/**
 * @author VISTALL
 * @since 11.04.14
 */
public class PropertyMirror extends FieldOrPropertyMirror
{
	private final MethodMirror myGetMethod;
	private final MethodMirror mySetMethod;

	public PropertyMirror(
			@Nonnull VirtualMachine aVm,
			int id,
			@Nonnull String name,
			@Nullable MethodMirror getMethod,
			@Nullable MethodMirror setMethod,
			@Nonnull TypeMirror parent,
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

	/**
	 * In .NET bytecode - index method like
	 *
	 * T this[int index]
	 * {
	 * }
	 *
	 * Stored in bytecode as Property with name `Item`.
	 * And accessors methods have +1 parameter(index)
	 * For original properties - get have no parameters, set - have one parameter
	 */
	public boolean isArrayProperty()
	{
		if(myGetMethod != null)
		{
			return myGetMethod.parameters().length >= 1;
		}
		else if(mySetMethod != null)
		{
			return mySetMethod.parameters().length >= 2;
		}
		throw new IllegalArgumentException("Not setter and getter");
	}

	@Override
	@Nonnull
	public TypeMirror type()
	{
		if(myGetMethod != null)
		{
			TypeMirror typeMirror = myGetMethod.returnType();
			assert typeMirror != null;
			return typeMirror;
		}
		else if(mySetMethod != null)
		{
			return mySetMethod.parameters()[0].type();
		}
		throw new IllegalArgumentException();
	}

	@Override
	public Value<?> value(@Nullable ThreadMirror threadMirror, @Nullable ObjectValueMirror thisObjectValue)
	{
		if(isStatic() && thisObjectValue != null || !isStatic() && thisObjectValue == null)
		{
			throw new IllegalArgumentException();
		}

		if(threadMirror == null)
		{
			throw new IllegalArgumentException("No thread mirror");
		}
		if(myGetMethod != null)
		{
			return myGetMethod.invoke(threadMirror, InvokeFlags.DISABLE_BREAKPOINTS, thisObjectValue);
		}
		return null;
	}

	@Override
	public void setValue(@Nullable ThreadMirror threadMirror, @Nullable ObjectValueMirror thisObjectValue, @Nonnull Value<?> value)
	{
		if(isStatic() && thisObjectValue != null || !isStatic() && thisObjectValue == null)
		{
			throw new IllegalArgumentException();
		}

		if(threadMirror == null)
		{
			throw new IllegalArgumentException("No thread mirror");
		}

		if(mySetMethod != null)
		{
			mySetMethod.invoke(threadMirror, InvokeFlags.DISABLE_BREAKPOINTS, thisObjectValue, value);
		}
	}

	@Override
	public boolean isStatic()
	{
		if(myGetMethod != null)
		{
			return myGetMethod.isStatic();
		}
		else if(mySetMethod != null)
		{
			return mySetMethod.isStatic();
		}
		throw new IllegalArgumentException();
	}

	@Override
	public boolean isAbstract()
	{
		if(myGetMethod != null)
		{
			return myGetMethod.isAbstract();
		}
		else if(mySetMethod != null)
		{
			return mySetMethod.isAbstract();
		}
		throw new IllegalArgumentException();
	}

	@Nonnull
	@Override
	public CustomAttributeMirror[] customAttributesImpl() throws JDWPException
	{
		return Type_GetPropertyCustomAttributes.process(vm, parent(), this).customAttributeMirrors;
	}
}
