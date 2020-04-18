package mono.debugger;

/**
 * @author VISTALL
 * @since 11.04.14
 */
public enum InvokeFlags
{
	NONE,
	DISABLE_BREAKPOINTS,
	SINGLE_THREADED,
	OUT_THIS,
	OUT_ARGS,
	VIRTUAL;

	private int mask;

	InvokeFlags()
	{
		mask = 1 << ordinal();
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
