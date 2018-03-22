package mono.debugger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import consulo.internal.dotnet.asm.signature.FieldAttributes;
import mono.debugger.protocol.ObjectReference_GetValues;
import mono.debugger.protocol.ObjectReference_SetValues;
import mono.debugger.protocol.Type_GetFieldCustomAttributes;
import mono.debugger.protocol.Type_GetValues;
import mono.debugger.protocol.Type_SetValues;
import mono.debugger.util.ImmutablePair;

/**
 * @author VISTALL
 * @since 11.04.14
 */
public class FieldMirror extends FieldOrPropertyMirror
{
	@Nonnull
	private final TypeMirror myTypeMirror;

	public FieldMirror(
			@Nonnull VirtualMachine aVm, int id, @Nonnull String name, @Nonnull TypeMirror typeMirror, @Nonnull TypeMirror parent, int attributes)
	{
		super(aVm, id, parent, attributes, name);
		myTypeMirror = typeMirror;
	}

	@Override
	public Value<?> value(@Nullable ThreadMirror threadMirror, @Nullable ObjectValueMirror thisObjectValue)
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
			throw e.asUncheckedException();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setValue(@Nullable ThreadMirror threadMirror, @Nullable ObjectValueMirror thisObjectValue, @Nonnull Value<?> value)
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
			throw e.asUncheckedException();
		}
	}

	@Override
	@Nonnull
	public TypeMirror type()
	{
		return myTypeMirror;
	}

	@Nonnull
	@Override
	public CustomAttributeMirror[] customAttributesImpl() throws JDWPException
	{
		return Type_GetFieldCustomAttributes.process(vm, parent(), this).customAttributeMirrors;
	}

	@Override
	public boolean isStatic()
	{
		return (myAttributes & FieldAttributes.Static) == FieldAttributes.Static;
	}

	@Override
	public boolean isAbstract()
	{
		return false;
	}
}
