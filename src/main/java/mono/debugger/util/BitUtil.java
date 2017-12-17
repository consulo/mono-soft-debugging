package mono.debugger.util;

/**
 * @author VISTALL
 * @since 06.06.14
 */
public class BitUtil
{
	public static boolean isSet(int val, int mask)
	{
		return isSet(val, mask, mask);
	}

	public static boolean isSet(int val, int mask, int to)
	{
		return (val & mask) == to;
	}
}
