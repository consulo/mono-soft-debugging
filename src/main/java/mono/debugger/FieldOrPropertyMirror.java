package mono.debugger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 11.04.14
 */
public abstract class FieldOrPropertyMirror extends CustomAttributeMirrorOwner implements ModifierOwner
{
	private final TypeMirror myParent;
	protected final int myAttributes;
	@Nonnull
	private final String myName;

	public FieldOrPropertyMirror(@Nonnull VirtualMachine aVm, int id, TypeMirror parent, int attributes, @Nonnull String name)
	{
		super(aVm, id);
		myParent = parent;
		myAttributes = attributes;
		myName = name;
	}

	public int attributes()
	{
		return myAttributes;
	}

	@Nonnull
	@Override
	protected String nameImpl() throws JDWPException
	{
		return myName;
	}

	@Nonnull
	public TypeMirror parent()
	{
		return myParent;
	}

	@Nonnull
	public abstract TypeMirror type();

	public abstract Value<?> value(@Nullable ThreadMirror threadMirror, @Nullable ObjectValueMirror thisObjectValue);

	public abstract void setValue(@Nullable ThreadMirror threadMirror, @Nullable ObjectValueMirror thisObjectValue, @Nonnull Value<?> value);
}
