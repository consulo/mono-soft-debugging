package test;

import java.util.List;
import java.util.Map;

import mono.debugger.FieldMirror;
import mono.debugger.LocationImpl;
import mono.debugger.MethodMirror;
import mono.debugger.SocketListeningConnector;
import mono.debugger.StackFrameMirror;
import mono.debugger.TypeMirror;
import mono.debugger.Value;
import mono.debugger.VirtualMachine;
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

		VirtualMachine accept = socketListeningConnector.accept(argumentMap);

		accept.resume();
		accept.suspend();

		TypeMirror typeMirror = accept.findTypes("MyClass", true)[0];

		FieldMirror[] fields = typeMirror.fields();
		for(FieldMirror field : fields)
		{
			System.out.println("field: " + field + " is static " + field.isStatic());
		}

		TypeMirror typeMirror2 = accept.findTypes("Program", true)[0];

		FieldMirror[] fields1 = typeMirror2.fields();
		for(FieldMirror field : fields1)
		{
			System.out.println("field: " + field + " is static " + field.isStatic());
		}

		int index = 0;
		MethodMirror m  = null;
		l:for(MethodMirror methodMirror : typeMirror.methods())
		{
			if("call".equals(methodMirror.name()))
			{
				Method_GetDebugInfo debugInfo = Method_GetDebugInfo.process((VirtualMachineImpl) accept, methodMirror);
				for(Method_GetDebugInfo.Entry entry : debugInfo.entries)
				{
					if(entry.line == 20)
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
				}
			}

			Thread.sleep(100L);
		}
	}
}
