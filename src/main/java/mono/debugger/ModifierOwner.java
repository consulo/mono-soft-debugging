package mono.debugger;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 11.04.14
 */
public interface ModifierOwner
{
	@Nonnull
	CustomAttributeMirror[] customAttributes();

	@Nonnull
	CustomAttributeMirror[] findCustomAttributes(@Nonnull String vmQName);

	boolean isStatic();

	boolean isAbstract();
}
