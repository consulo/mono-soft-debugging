package mono.debugger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import mono.debugger.protocol.ObjectReference_GetType;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class ObjectValueMirror extends ValueImpl<Object> implements MirrorWithId
{
	private final long myId;

	public ObjectValueMirror(VirtualMachine aVm, long id)
	{
		super(aVm);
		myId = id;
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
