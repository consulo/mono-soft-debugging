package mono.debugger;

/**
 * @author VISTALL
 * @since 11.04.14
 */
public enum InvokeFlags
{
	NONE(0),
	DISABLE_BREAKPOINTS(1),
	SINGLE_THREADED(2),
	OUT_THIS(4),
	OUT_ARGS(8),
	VIRTUAL(16);

	private int mask;

	InvokeFlags(int mask)
	{
		this.mask = mask;
	}

	public static int pack(InvokeFlags... flags)
	{
		int result = 0;

		for(InvokeFlags flag : flags)
		{
			result |= flag.mask;
		}

		return result;
	}
}
