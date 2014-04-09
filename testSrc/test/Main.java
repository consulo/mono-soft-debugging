package test;

import java.util.List;
import java.util.Map;

import mono.debugger.SocketListeningConnector;
import mono.debugger.StackFrame;
import mono.debugger.ThreadMirror;
import mono.debugger.VirtualMachine;
import mono.debugger.connect.Connector;

/**
 * @author VISTALL
 * @since 07.04.14
 */
public class Main
{
	public static void main(String[] args) throws Exception
	{
		SocketListeningConnector socketListeningConnector = new SocketListeningConnector();

		Map<String, Connector.Argument> argumentMap = socketListeningConnector.defaultArguments();

		argumentMap.get(SocketListeningConnector.ARG_LOCALADDR).setValue("127.0.0.1");
		argumentMap.get(SocketListeningConnector.ARG_PORT).setValue("10110");

		VirtualMachine accept = socketListeningConnector.accept(argumentMap);


		accept.resume();


		System.out.println("wait 10 sec");

		Thread.sleep(10000L);
		accept.suspend();

		for(ThreadMirror threadMirror : accept.allThreads())
		{
			List<StackFrame> frames = threadMirror.frames();
			System.out.println("thread: '" + threadMirror.name());
			System.out.println("frames: ");
			for(StackFrame frame : frames)
			{
				System.out.println(" -- " + frame);
			}
		}

		accept.dispose();

		Thread.sleep(50000L);
	}
}
