package mono.debugger;

import org.jetbrains.annotations.NotNull;

/**
 * @author VISTALL
 * @since 11.04.14
 */
public interface ValueVisitor
{
	public static class Adapter implements ValueVisitor
	{
		@Override
		public void visitObjectValue(@NotNull ObjectValueMirror value)
		{

		}

		@Override
		public void visitStringValue(@NotNull StringValueMirror value, @NotNull String mainValue)
		{

		}

		@Override
		public void visitNumberValue(@NotNull NumberValueMirror value, @NotNull Number mainValue)
		{

		}

		@Override
		public void visitNoObjectValue(@NotNull NoObjectValueMirror value)
		{

		}

		@Override
		public void visitArrayValue(@NotNull ArrayValueMirror value)
		{

		}

		@Override
		public void visitBooleanValue(@NotNull BooleanValueMirror value, @NotNull Boolean mainValue)
		{

		}

		@Override
		public void visitCharValue(@NotNull CharValueMirror valueMirror, @NotNull Character mainValue)
		{

		}
	}

	void visitObjectValue(@NotNull ObjectValueMirror value);

	void visitStringValue(@NotNull StringValueMirror value, @NotNull String mainValue);

	void visitNumberValue(@NotNull NumberValueMirror value, @NotNull Number mainValue);

	void visitNoObjectValue(@NotNull NoObjectValueMirror value);

	void visitArrayValue(@NotNull ArrayValueMirror value);

	void visitBooleanValue(@NotNull BooleanValueMirror value, @NotNull Boolean mainValue);

	void visitCharValue(@NotNull CharValueMirror valueMirror, @NotNull Character mainValue);
}
