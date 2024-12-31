package mono.debugger;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import mono.debugger.protocol.ObjectReference_GetAddress;
import mono.debugger.protocol.ObjectReference_GetType;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class ObjectValueMirror extends ValueImpl<Object> implements MirrorWithId
{
	private final int myId;
	private long myAddress = -1;

	public ObjectValueMirror(VirtualMachine aVm, int id)
	{
		super(aVm);
		myId = id;
	}

	public long address()
	{
		if(myAddress == -1)
		{
			try
			{
				myAddress = ObjectReference_GetAddress.process(vm, this).address;
			}
			catch(JDWPException e)
			{
				throw e.asUncheckedException();
			}
		}
		return myAddress;
	}

	@Override
	public int id()
	{
		return myId;
	}

	@Override
	public TypeMirror type()
	{
		try
		{
			return ObjectReference_GetType.process(vm, this).type;
		}
		catch(JDWPException e)
		{
			throw e.asUncheckedException();
		}
	}

	@Nullable
	@Override
	public Object value()
	{
		return "object";
	}

	@Override
	public void accept(@Nonnull ValueVisitor valueVisitor)
	{
		valueVisitor.visitObjectValue(this);
	}
}
