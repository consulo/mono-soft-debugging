package mono.debugger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author VISTALL
 * @since 23.07.2015
 */
public class TypeValueMirror extends ValueImpl<TypeMirror>
{
	private TypeMirror myTypeMirror;

	public TypeValueMirror(VirtualMachine aVm, TypeMirror typeMirror)
	{
		super(aVm);
		myTypeMirror = typeMirror;
	}

	@Nullable
	@Override
	public TypeMirror type()
	{
		return vm.rootAppDomain().corlibAssembly().findTypeByQualifiedName("System.Type", false);
	}

	@NotNull
	@Override
	public TypeMirror value()
	{
		return myTypeMirror;
	}

	@Override
	public void accept(@NotNull ValueVisitor valueVisitor)
	{
		valueVisitor.visitTypeValue(this, value());
	}
}
