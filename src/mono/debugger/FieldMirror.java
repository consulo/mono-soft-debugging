package mono.debugger;

import org.jetbrains.annotations.NotNull;
import edu.arizona.cs.mbel.signature.FieldAttributes;

/**
 * @author VISTALL
 * @since 11.04.14
 */
public class FieldMirror extends FieldOrPropertyMirror
{
	@NotNull
	private final TypeMirror myTypeMirror;

	public FieldMirror(
			@NotNull VirtualMachine aVm, long id, @NotNull String name, @NotNull TypeMirror typeMirror, @NotNull TypeMirror parent, int attributes)
	{
		super(aVm, id, parent, attributes, name);
		myTypeMirror = typeMirror;
	}

	@Override
	@NotNull
	public TypeMirror type()
	{
		return myTypeMirror;
	}

	@Override
	public boolean isStatic()
	{
		return (myAttributes & FieldAttributes.Static) == FieldAttributes.Static;
	}
}
