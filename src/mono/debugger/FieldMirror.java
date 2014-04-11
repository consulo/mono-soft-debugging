package mono.debugger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import edu.arizona.cs.mbel.signature.FieldAttributes;
import mono.debugger.protocol.Type_GetValues;

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
	private final TypeMirror myParent;
	private final int myAttributes;

	public FieldMirror(@NotNull VirtualMachine aVm, long id, @NotNull String name, @NotNull TypeMirror typeMirror, TypeMirror parent, int attributes)
	{
		super(aVm, id);
		myName = name;
		myTypeMirror = typeMirror;
		myParent = parent;
		myAttributes = attributes;
	}

	public Value<?> value(@Nullable ObjectValueMirror objectValueMirror)
	{
		if(isStatic() && objectValueMirror != null || !isStatic() && objectValueMirror == null)
		{
			throw new IllegalArgumentException();
		}

		if(objectValueMirror == null)
		{
			try
			{
				Type_GetValues process = Type_GetValues.process(vm, parent(), this);
				return process.values[0];
			}
			catch(JDWPException e)
			{
				throw e.toJDIException();
			}
		}
		else
		{
			return null;
		}
	}

	@NotNull
	public TypeMirror parent()
	{
		return myParent;
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
