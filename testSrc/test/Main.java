package test;

import java.util.List;
import java.util.Map;

import mono.debugger.ReferenceType;
import mono.debugger.SocketListeningConnector;
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

		Map<String,Connector.Argument> argumentMap = socketListeningConnector.defaultArguments();

		argumentMap.get(SocketListeningConnector.ARG_LOCALADDR).setValue("127.0.0.1");
		argumentMap.get(SocketListeningConnector.ARG_PORT).setValue("10110");

		VirtualMachine accept = socketListeningConnector.accept(argumentMap);

		List<ReferenceType> types = accept.findTypes("System.Int32", true);


		Thread.sleep(50000L);
	}
}
