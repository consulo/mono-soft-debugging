package mono.debugger;

import org.jetbrains.annotations.NotNull;
import edu.arizona.cs.mbel.signature.FieldAttributes;

/**
 * @author VISTALL
 * @since 11.04.14
 */
public class FieldMirror extends MirrorWithIdAndName
{
	@NotNull
	private final String myName;
	@NotNull
	private final TypeMirror myTypeMirror;
	private final int myAttributes;

	public FieldMirror(@NotNull VirtualMachine aVm, long id, @NotNull String name, @NotNull TypeMirror typeMirror, int attributes)
	{
		super(aVm, id);
		myName = name;
		myTypeMirror = typeMirror;
		myAttributes = attributes;
	}

	@NotNull
	public TypeMirror type()
	{
		return myTypeMirror;
	}

	@NotNull
	@Override
	protected String nameImpl() throws JDWPException
	{
		return myName;
	}

	public boolean isStatic()
	{
		return (myAttributes & FieldAttributes.Static) == FieldAttributes.Static;
	}
}
