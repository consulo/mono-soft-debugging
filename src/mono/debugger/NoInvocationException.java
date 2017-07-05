package mono.debugger;

/**
 * @author VISTALL
 * @since 06-Jul-17
 */
public class NoInvocationException extends RuntimeException
{
	public NoInvocationException()
	{
	}

	public NoInvocationException(String message)
	{
		super(message);
	}

	public NoInvocationException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public NoInvocationException(Throwable cause)
	{
		super(cause);
	}

	public NoInvocationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
