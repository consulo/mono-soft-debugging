package mono.debugger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import mono.debugger.protocol.ObjectReference_GetValues;
import mono.debugger.protocol.Type_GetValues;

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

	public Value<?> value(@Nullable ObjectValueMirror objectValueMirror)
	{
		if(isStatic() && objectValueMirror != null || !isStatic() && objectValueMirror == null)
		{
			throw new IllegalArgumentException();
		}

		try
		{
			if(objectValueMirror == null)
			{
				Type_GetValues process = Type_GetValues.process(vm, parent(), this);
				return process.values[0];
			}
			else
			{
				ObjectReference_GetValues process = ObjectReference_GetValues.process(vm, objectValueMirror, this);
				return process.values[0];
			}
		}
		catch(JDWPException e)
		{
			throw e.toJDIException();
		}
	}
}
