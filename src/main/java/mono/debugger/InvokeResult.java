package mono.debugger;

/**
 * @author VISTALL
 * @since 2020-04-18
 */
public final class InvokeResult
{
	private boolean myError;

	private Value<?> myValue;

	private Value<?> myOutThis;

	private Value[] myOutArgs;

	public InvokeResult(boolean error, Value<?> value, Value<?> outThis, Value[] outArgs)
	{
		myError = error;
		myValue = value;
		myOutThis = outThis;
		myOutArgs = outArgs;
	}

	public boolean isError()
	{
		return myError;
	}

	public Value[] getOutArgs()
	{
		return myOutArgs;
	}

	public Value<?> getOutThis()
	{
		return myOutThis;
	}

	public Value<?> getValue()
	{
		return myValue;
	}
}
