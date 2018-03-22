package mono.debugger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mono.debugger.protocol.StackFrame_GetThis;
import mono.debugger.protocol.StackFrame_GetValues;
import mono.debugger.protocol.StackFrame_SetValues;
import mono.debugger.util.ImmutablePair;

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
	private final int myFrameID;
	private final Location myLocation;
	private final StackFrameFlags myFlags;

	public StackFrameMirror(VirtualMachine aVm, ThreadMirror threadMirror, int frameID, Location location, StackFrameFlags flags)
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

	@Nonnull
	@Override
	public Location location()
	{
		return myLocation;
	}

	@Nonnull
	public ThreadMirror thread()
	{
		return myThreadMirror;
	}

	@Nonnull
	public Value thisObject()
	{
		try
		{
			return StackFrame_GetThis.process(vm, myThreadMirror, this).value;
		}
		catch(JDWPException e)
		{
			throw e.asUncheckedException();
		}
	}

	@Override
	public int id()
	{
		return myFrameID;
	}

	@Nullable
	public Value localOrParameterValue(LocalVariableOrParameterMirror mirror)
	{
		// native methods ill throw absent information
		if(flags() == StackFrameFlags.NATIVE_TRANSITION)
		{
			return null;
		}
		try
		{
			StackFrame_GetValues process = StackFrame_GetValues.process(vm, myThreadMirror, this, mirror);
			return process.values[0];
		}
		catch(JDWPException e)
		{
			if(e.errorCode == JDWP.Error.ABSENT_INFORMATION)
			{
				return null;
			}
			throw e.asUncheckedException();
		}
	}

	@Nullable
	public Value[] localOrParameterValues(LocalVariableOrParameterMirror... mirror)
	{
		// native methods ill throw absent information
		if(flags() == StackFrameFlags.NATIVE_TRANSITION)
		{
			return null;
		}
		try
		{
			StackFrame_GetValues process = StackFrame_GetValues.process(vm, myThreadMirror, this, mirror);
			return process.values;
		}
		catch(JDWPException e)
		{
			if(e.errorCode == JDWP.Error.ABSENT_INFORMATION)
			{
				return null;
			}
			throw e.asUncheckedException();
		}
	}

	public void setLocalOrParameterValues(@Nonnull ImmutablePair<LocalVariableOrParameterMirror, Value<?>>... pairs)
	{
		try
		{
			StackFrame_SetValues.process(vm, myThreadMirror, this, pairs);
		}
		catch(JDWPException e)
		{
			throw e.asUncheckedException();
		}
	}
}
