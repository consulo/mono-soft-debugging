package mono.debugger;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class ObjectValueMirror extends ValueImpl implements MirrorWithId
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
		return null;
	}
}
