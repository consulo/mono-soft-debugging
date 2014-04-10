package mono.debugger;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class ObjectMirror extends MirrorImpl implements MirrorWithId
{
	private final long myId;

	public ObjectMirror(VirtualMachine aVm, long id)
	{
		super(aVm);
		myId = id;
	}

	@Override
	public long id()
	{
		return myId;
	}
}
