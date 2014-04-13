package mono.debugger.util;

/**
 * @author VISTALL
 * @since 13.04.14
 */
public class ImmutablePair<A, B>
{
	private final A myLeft;
	private final B myRight;

	public ImmutablePair(A left, B right)
	{
		myLeft = left;
		myRight = right;
	}

	public B getRight()
	{
		return myRight;
	}

	public A getLeft()
	{
		return myLeft;
	}
}
