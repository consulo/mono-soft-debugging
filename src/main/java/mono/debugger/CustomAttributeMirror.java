package mono.debugger;

/**
 * @author VISTALL
 * @since 23.07.2015
 */
public class CustomAttributeMirror extends MirrorImpl
{
	public static final CustomAttributeMirror[] EMPTY_ARRAY = new CustomAttributeMirror[0];

	private MethodMirror myConstructorMirror;
	private Value[] myValues;
	private NamedValue[] myNamedValues;

	public CustomAttributeMirror(VirtualMachine virtualMachine, MethodMirror constructorMirror, Value[] values, NamedValue[] namedValues)
	{
		super(virtualMachine);
		myConstructorMirror = constructorMirror;
		myValues = values;
		myNamedValues = namedValues;
	}

	public MethodMirror getConstructorMirror()
	{
		return myConstructorMirror;
	}

	public Value[] getValues()
	{
		return myValues;
	}

	public NamedValue[] getNamedValues()
	{
		return myNamedValues;
	}
}
