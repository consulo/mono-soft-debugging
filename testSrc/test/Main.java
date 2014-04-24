package test;

import java.util.List;
import java.util.Map;

import mono.debugger.LocationImpl;
import mono.debugger.MethodMirror;
import mono.debugger.SocketListeningConnector;
import mono.debugger.StackFrameMirror;
import mono.debugger.TypeMirror;
import mono.debugger.Value;
import mono.debugger.VirtualMachineImpl;
import mono.debugger.connect.Connector;
import mono.debugger.event.EventSet;
import mono.debugger.protocol.Method_GetDebugInfo;
import mono.debugger.request.BreakpointRequest;
import mono.debugger.request.EventRequestManager;

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

		VirtualMachineImpl accept = (VirtualMachineImpl) socketListeningConnector.accept(argumentMap);

		Thread.sleep(1000L);

		TypeMirror typeMirror = accept.findTypesByQualifiedName("Program", true)[0];

		int index = 0;
		MethodMirror m = null;
		l:
		for(MethodMirror methodMirror : typeMirror.methods())
		{
			if("Main".equals(methodMirror.name()))
			{
				for(Method_GetDebugInfo.Entry entry : methodMirror.debugInfo())
				{
					if(entry.line == 54)
					{
						m = methodMirror;
						index = entry.offset;
						break l;
					}
				}
			}
		}

		EventRequestManager eventRequestManager = accept.eventRequestManager();


		BreakpointRequest breakpointRequest = eventRequestManager.createBreakpointRequest(new LocationImpl(accept, m, index));
		breakpointRequest.enable();
		accept.resume();

		while(true)
		{
			EventSet eventSet = accept.eventQueue().remove();
			if(eventSet.suspendPolicy() == BreakpointRequest.SUSPEND_ALL)
			{
				List<StackFrameMirror> frames = eventSet.eventThread().frames();

				for(StackFrameMirror frame : frames)
				{
					System.out.println("frame: " + frame.location().method());
					Value value = frame.thisObject();

					TypeMirror type = value.type();

					if(type == null)
					{
						continue;
					}

					MethodMirror toString = typeMirror.findMethodByName("ToString", true);

					assert toString != null;

					Value<?> invoke = toString.invoke(frame.thread(), value);

					System.out.println("tst: " + invoke.value());
				}
			}


			Thread.sleep(100L);
		}
	}
}
