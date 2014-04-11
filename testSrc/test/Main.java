package test;

import java.util.List;
import java.util.Map;

import mono.debugger.*;
import mono.debugger.connect.Connector;
import mono.debugger.event.EventSet;
import mono.debugger.protocol.Method_GetDebugInfo;
import mono.debugger.protocol.VirtualMachine_InvokeMethod;
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

		accept.resume();
		accept.suspend();

		TypeMirror typeMirror = accept.findTypes("MyClass", true)[0];

		int index = 0;
		MethodMirror m  = null;
		l:for(MethodMirror methodMirror : typeMirror.methods())
		{
			if("call".equals(methodMirror.name()))
			{
				Method_GetDebugInfo debugInfo = Method_GetDebugInfo.process(accept, methodMirror);
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

					TypeMirror type = value.type();

					if(type == null)
					{
						continue;
					}

					TypeMirror object = accept.findTypes("System.Object", true)[0];

					MethodMirror toString = object.findMethodByName("ToString");

					VirtualMachine_InvokeMethod process = VirtualMachine_InvokeMethod.process(accept, frame.thread(), InvokeFlags.DISABLE_BREAKPOINTS, toString,
							(ObjectValueMirror) value);
					System.out.println("b");
				}
			}


			Thread.sleep(100L);
		}
	}
}
