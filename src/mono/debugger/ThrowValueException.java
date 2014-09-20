package mono.debugger;

import org.jetbrains.annotations.NotNull;

/**
 * @author VISTALL
 * @since 20.09.14
 */
public class ThrowValueException extends RuntimeException
{
	private final Value<?> myThrowExceptionValue;

	public ThrowValueException(@NotNull Value<?> throwExceptionValue)
	{
		myThrowExceptionValue = throwExceptionValue;
	}

	public Value<?> getThrowExceptionValue()
	{
		return myThrowExceptionValue;
	}
}
