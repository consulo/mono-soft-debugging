package mono.debugger;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 11.04.14
 */
public interface ValueVisitor
{
	class Adapter implements ValueVisitor
	{
		@Override
		public void visitObjectValue(@Nonnull ObjectValueMirror value)
		{
		}

		@Override
		public void visitStringValue(@Nonnull StringValueMirror value, @Nonnull String mainValue)
		{
		}

		@Override
		public void visitNumberValue(@Nonnull NumberValueMirror value, @Nonnull Number mainValue)
		{
		}

		@Override
		public void visitNoObjectValue(@Nonnull NoObjectValueMirror value)
		{
		}

		@Override
		public void visitArrayValue(@Nonnull ArrayValueMirror value)
		{
		}

		@Override
		public void visitBooleanValue(@Nonnull BooleanValueMirror value, @Nonnull Boolean mainValue)
		{
		}

		@Override
		public void visitCharValue(@Nonnull CharValueMirror valueMirror, @Nonnull Character mainValue)
		{
		}

		@Override
		public void visitTypeValue(@Nonnull TypeValueMirror typeValueMirror, @Nonnull TypeMirror mainValue)
		{
		}

		@Override
		public void visitStructValue(@Nonnull StructValueMirror mirror)
		{
		}

		@Override
		public void visitEnumValue(@Nonnull EnumValueMirror mirror)
		{
		}
	}

	void visitObjectValue(@Nonnull ObjectValueMirror value);

	void visitStringValue(@Nonnull StringValueMirror value, @Nonnull String mainValue);

	void visitNumberValue(@Nonnull NumberValueMirror value, @Nonnull Number mainValue);

	void visitNoObjectValue(@Nonnull NoObjectValueMirror value);

	void visitArrayValue(@Nonnull ArrayValueMirror value);

	void visitBooleanValue(@Nonnull BooleanValueMirror value, @Nonnull Boolean mainValue);

	void visitCharValue(@Nonnull CharValueMirror valueMirror, @Nonnull Character mainValue);

	void visitTypeValue(@Nonnull TypeValueMirror typeValueMirror, @Nonnull TypeMirror mainValue);

	void visitStructValue(@Nonnull StructValueMirror mirror);

	void visitEnumValue(@Nonnull EnumValueMirror mirror);
}
