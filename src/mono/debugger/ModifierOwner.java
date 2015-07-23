package mono.debugger;

import org.jetbrains.annotations.NotNull;

/**
 * @author VISTALL
 * @since 11.04.14
 */
public interface ModifierOwner
{
	@NotNull
	CustomAttributeMirror[] customAttributes();

	@NotNull
	CustomAttributeMirror[] findCustomAttributes(@NotNull String vmQName);

	boolean isStatic();
}
