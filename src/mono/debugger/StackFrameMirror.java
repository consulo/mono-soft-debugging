package mono.debugger;

import org.jetbrains.annotations.NotNull;
import mono.debugger.protocol.StackFrame_GetValues;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class StackFrameMirror extends MirrorImpl implements Locatable, MirrorWithId
{
	public enum StackFrameFlags
	{
		NONE,
		DEBUGGER_INVOKE,
		NATIVE_TRANSITION
	}

	private final ThreadMirror myThreadMirror;
	private final long myFrameID;
	private final Location myLocation;
	private final StackFrameFlags myFlags;

	public StackFrameMirror(VirtualMachine aVm, ThreadMirror threadMirror, long frameID, Location location, StackFrameFlags flags)
	{
		super(aVm);
		myThreadMirror = threadMirror;
		myFrameID = frameID;
		myLocation = location;
		myFlags = flags;
	}

	public StackFrameFlags flags()
	{
		return myFlags;
	}

	@NotNull
	@Override
	public Location location()
	{
		return myLocation;
	}

	@NotNull
	public ThreadMirror thread()
	{
		return myThreadMirror;
	}

	public ObjectReference thisObject()
	{
		return null;
	}

	@Override
	public long id()
	{
		return myFrameID;
	}

	public Value parameterValue(MethodParameterMirror parameter)
	{
		// native methods ill throw absent information
		if(flags() == StackFrameFlags.NATIVE_TRANSITION)
		{
			return null;
		}
		try
		{
			StackFrame_GetValues process = StackFrame_GetValues.process(vm, myThreadMirror, this, new int[]{-(int) (parameter.id() + 1)});
			return process.values[0];
		}
		catch(JDWPException e)
		{
			if(e.errorCode == JDWP.Error.ABSENT_INFORMATION)
			{
				return null;
			}
			throw e.toJDIException();
		}
	}
}
