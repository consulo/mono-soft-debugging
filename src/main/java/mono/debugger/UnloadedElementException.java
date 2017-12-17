package mono.debugger;

/**
 * @author VISTALL
 * @since 15.05.2015
 */
public class UnloadedElementException extends RuntimeException
{
	public UnloadedElementException()
	{
	}

	public UnloadedElementException(String message)
	{
		super(message);
	}

	public UnloadedElementException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public UnloadedElementException(Throwable cause)
	{
		super(cause);
	}
}
