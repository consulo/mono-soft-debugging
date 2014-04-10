package test;

import java.util.List;
import java.util.Map;

import mono.debugger.Location;
import mono.debugger.MethodMirror;
import mono.debugger.MethodParameterMirror;
import mono.debugger.SocketListeningConnector;
import mono.debugger.StackFrameMirror;
import mono.debugger.StackFrameOld;
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
			List<StackFrameOld> frames = threadMirror.frames();
			System.out.println("thread: '" + threadMirror.name());
			System.out.println("frames: ");
			for(StackFrameOld frame : frames)
			{
				StackFrameMirror stackFrameMirror = (StackFrameMirror) frame;


				System.out.println(" -- frame: " + stackFrameMirror.id());
				System.out.println(" --- flags: " + stackFrameMirror.flags());
				Location location = stackFrameMirror.location();
				MethodMirror method = location.method();
				System.out.println(" --- method: " + method);
				for(MethodParameterMirror parameter : method.parameters())
				{
					System.out.println(" ---- parameter: " + parameter + " value: " + stackFrameMirror.parameterValue(parameter));
				}
				System.out.println(" --- class: " + method.declaringType());
				System.out.println(" --- codeIndex: " + location.codeIndex());
			}
		}

		accept.dispose();

		Thread.sleep(50000L);
	}
}
