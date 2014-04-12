package mono.debugger;

/**
 * @author VISTALL
 * @since 12.04.14
 */
public class LocalVariableMirror extends LocalVariableOrParameterMirror
{
	private final int myLiveStartRange;
	private final int myLiveEndRange;

	public LocalVariableMirror(VirtualMachineImpl vm, int i, TypeMirror type, String name, int liveStartRange, int liveEndRange)
	{
		super(vm, i, type, name);
		myLiveStartRange = liveStartRange;
		myLiveEndRange = liveEndRange;
	}

	public int liveRangeStart()
	{
		return myLiveStartRange;
	}

	public int liveRangeEnd()
	{
		return myLiveEndRange;
	}

	@Override
	public int idForStackFrame()
	{
		return (int) id();
	}
}
