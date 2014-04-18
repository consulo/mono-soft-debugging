package mono.debugger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import mono.debugger.protocol.ObjectReference_GetValues;
import mono.debugger.protocol.ObjectReference_SetValues;
import mono.debugger.protocol.Type_GetValues;
import mono.debugger.protocol.Type_SetValues;
import mono.debugger.util.ImmutablePair;

/**
 * @author VISTALL
 * @since 11.04.14
 */
public abstract class FieldOrPropertyMirror extends MirrorWithIdAndName implements ModifierOwner
{
	private final TypeMirror myParent;
	protected final int myAttributes;
	@NotNull
	private final String myName;

	public FieldOrPropertyMirror(@NotNull VirtualMachine aVm, long id, TypeMirror parent, int attributes, @NotNull String name)
	{
		super(aVm, id);
		myParent = parent;
		myAttributes = attributes;
		myName = name;
	}

	@NotNull
	@Override
	protected String nameImpl() throws JDWPException
	{
		return myName;
	}

	@NotNull
	public TypeMirror parent()
	{
		return myParent;
	}

	@NotNull
	public abstract TypeMirror type();

	public Value<?> value(@Nullable ObjectValueMirror thisObjectValue)
	{
		if(isStatic() && thisObjectValue != null || !isStatic() && thisObjectValue == null)
		{
			throw new IllegalArgumentException();
		}

		try
		{
			if(thisObjectValue == null)
			{
				Type_GetValues process = Type_GetValues.process(vm, parent(), this);
				return process.values[0];
			}
			else
			{
				ObjectReference_GetValues process = ObjectReference_GetValues.process(vm, thisObjectValue, this);
				return process.values[0];
			}
		}
		catch(JDWPException e)
		{
			throw e.toJDIException();
		}
	}

	@SuppressWarnings("unchecked")
	public void setValue(@Nullable ObjectValueMirror thisObjectValue, @NotNull Value<?> value)
	{
		if(isStatic() && thisObjectValue != null || !isStatic() && thisObjectValue == null)
		{
			throw new IllegalArgumentException();
		}

		try
		{
			if(thisObjectValue == null)
			{
				Type_SetValues.process(vm, parent(), new ImmutablePair<FieldOrPropertyMirror, Value<?>>(this, value));
			}
			else
			{
				ObjectReference_SetValues.process(vm, thisObjectValue, new ImmutablePair<FieldOrPropertyMirror, Value<?>>(this, value));
			}
		}
		catch(JDWPException e)
		{
			throw e.toJDIException();
		}
	}
}
