package mono.debugger;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 19.04.14
 */
public interface GenericTarget<T extends Mirror>
{
	/**
	 * @return null is this is original mirror, if it a runtime copy with generic - ill return not null
	 */
	@Nullable
	T original();

	@Nonnull
	TypeMirror[] genericArguments();
}
