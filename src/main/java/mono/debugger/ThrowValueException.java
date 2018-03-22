package mono.debugger;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 20.09.14
 */
public class ThrowValueException extends RuntimeException
{
	private final Value<?> myThrowExceptionValue;

	public ThrowValueException(@Nonnull Value<?> throwExceptionValue)
	{
		myThrowExceptionValue = throwExceptionValue;
	}

	public Value<?> getThrowExceptionValue()
	{
		return myThrowExceptionValue;
	}
}
