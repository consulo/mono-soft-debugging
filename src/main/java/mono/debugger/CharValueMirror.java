package mono.debugger;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 25.04.14
 */
public class CharValueMirror extends ValueImpl<Character>
{
	private final Character myChar;

	public CharValueMirror(VirtualMachine aVm, Character character)
	{
		super(aVm);
		myChar = character;
	}

	@Nullable
	@Override
	public TypeMirror type()
	{
		return virtualMachine().rootAppDomain().corlibAssembly().findTypeByQualifiedName(TypeTag.Char.getType(), false);
	}

	@Nonnull
	@Override
	public Character value()
	{
		return myChar;
	}

	@Override
	public void accept(@Nonnull ValueVisitor valueVisitor)
	{
		valueVisitor.visitCharValue(this, value());
	}
}
