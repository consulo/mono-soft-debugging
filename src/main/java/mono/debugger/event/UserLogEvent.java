package mono.debugger.event;

import mono.debugger.JDWP;
import mono.debugger.VirtualMachine;

/**
 * @author VISTALL
 * @since 24.04.2015
 */
public class UserLogEvent extends ThreadedEvent
{
	private final String myCategory;
	private final int myLevel;
	private final String myMessage;

	public UserLogEvent(VirtualMachine virtualMachine, JDWP.Event.Composite.Events.UserLog evt)
	{
		super(virtualMachine, evt, evt.requestID, evt.thread);
		myCategory = evt.category;
		myLevel = evt.level;
		myMessage = evt.message;
	}

	public String getCategory()
	{
		return myCategory;
	}

	public int getLevel()
	{
		return myLevel;
	}

	public String getMessage()
	{
		return myMessage;
	}

	@Override
	public String eventName()
	{
		return "user log";
	}
}
