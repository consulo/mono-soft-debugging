package mono.debugger.event;

import mono.debugger.EventSetImpl;
import mono.debugger.JDWP;
import mono.debugger.ThreadMirror;
import mono.debugger.VirtualMachine;

public abstract class ThreadedEvent extends EventSetImpl.EventImpl
{
	private ThreadMirror thread;

	public ThreadedEvent(VirtualMachine virtualMachine, JDWP.Event.Composite.Events.EventsCommon evt, int requestID, ThreadMirror thread)
	{
		super(virtualMachine, evt, requestID);
		this.thread = thread;
	}

	public ThreadMirror thread()
	{
		return thread;
	}

	@Override
	public String toString()
	{
		return eventName() + " in thread " + thread.name();
	}
}
