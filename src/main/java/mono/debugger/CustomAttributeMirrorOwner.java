package mono.debugger;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 23.07.2015
 */
public abstract class CustomAttributeMirrorOwner extends MirrorWithIdAndName implements ModifierOwner
{
	private CustomAttributeMirror[] myCustomAttributeMirrors;

	public CustomAttributeMirrorOwner(@Nonnull VirtualMachine aVm, int id)
	{
		super(aVm, id);
	}

	protected abstract CustomAttributeMirror[] customAttributesImpl() throws JDWPException;

	@Nonnull
	@Override
	public CustomAttributeMirror[] findCustomAttributes(@Nonnull String vmQName)
	{
		CustomAttributeMirror[] customAttributeMirrors = customAttributes();
		if(customAttributeMirrors.length == 0)
		{
			return CustomAttributeMirror.EMPTY_ARRAY;
		}

		List<CustomAttributeMirror> list = new ArrayList<CustomAttributeMirror>(2);
		for(CustomAttributeMirror customAttributeMirror : customAttributeMirrors)
		{
			MethodMirror constructorMirror = customAttributeMirror.getConstructorMirror();

			TypeMirror typeMirror = constructorMirror.declaringType();

			String s = typeMirror.qualifiedName();
			if(vmQName.equals(s))
			{
				list.add(customAttributeMirror);
			}
		}
		return list.isEmpty() ? CustomAttributeMirror.EMPTY_ARRAY : list.toArray(new CustomAttributeMirror[list.size()]);
	}

	@Nonnull
	@Override
	public final CustomAttributeMirror[] customAttributes()
	{
		if(myCustomAttributeMirrors != null)
		{
			return myCustomAttributeMirrors;
		}
		try
		{
			return myCustomAttributeMirrors = customAttributesImpl();
		}
		catch(JDWPException e)
		{
			throw e.asUncheckedException();
		}
	}
}
