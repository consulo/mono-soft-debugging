package test;

import java.util.Map;

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

		//accept.resume();
	   /*
		System.out.println(accept.description());


		System.out.println("waiting 10 sec and suspend");

		Thread.sleep(10000L);

		System.out.println("try to pause");

		accept.suspend();  */

		Thread.sleep(50000L);
	}
}
