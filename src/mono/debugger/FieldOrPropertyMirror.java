package mono.debugger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author VISTALL
 * @since 11.04.14
 */
public abstract class FieldOrPropertyMirror extends CustomAttributeMirrorOwner implements ModifierOwner
{
	private final TypeMirror myParent;
	protected final int myAttributes;
	@NotNull
	private final String myName;

	public FieldOrPropertyMirror(@NotNull VirtualMachine aVm, int id, TypeMirror parent, int attributes, @NotNull String name)
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

	public abstract Value<?> value(@Nullable ThreadMirror threadMirror, @Nullable ObjectValueMirror thisObjectValue);

	public abstract void setValue(@Nullable ThreadMirror threadMirror, @Nullable ObjectValueMirror thisObjectValue, @NotNull Value<?> value);
}
