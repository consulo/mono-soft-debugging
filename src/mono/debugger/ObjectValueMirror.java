package mono.debugger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import mono.debugger.protocol.ObjectReference_GetAddress;
import mono.debugger.protocol.ObjectReference_GetType;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class ObjectValueMirror extends ValueImpl<Object> implements MirrorWithId
{
	private final long myId;
	private long myAddress = -1;

	public ObjectValueMirror(VirtualMachine aVm, long id)
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
				throw e.toJDIException();
			}
		}
		return myAddress;
	}

	@Override
	public long id()
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
			throw e.toJDIException();
		}
	}

	@Nullable
	@Override
	public Object value()
	{
		return "object";
	}

	@Override
	public void accept(@NotNull ValueVisitor valueVisitor)
	{
		valueVisitor.visitObjectValue(this);
	}
}
