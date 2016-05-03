package mono.debugger;

/**
 * @author VISTALL
 * @since 23.07.2015
 */
public class NamedValue
{
	private Value<?> myValue;
	private int myFieldOrPropertyId;
	private boolean myProperty;

	public NamedValue(Value<?> value, int fieldOrPropertyId, boolean property)
	{
		myValue = value;
		myFieldOrPropertyId = fieldOrPropertyId;
		myProperty = property;
	}

	public Value<?> getValue()
	{
		return myValue;
	}

	public int getFieldOrPropertyId()
	{
		return myFieldOrPropertyId;
	}

	public boolean isProperty()
	{
		return myProperty;
	}
}
