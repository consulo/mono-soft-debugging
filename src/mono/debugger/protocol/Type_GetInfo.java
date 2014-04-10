package mono.debugger.protocol;

import mono.debugger.JDWPException;
import mono.debugger.PacketStream;
import mono.debugger.TypeMirror;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class Type_GetInfo implements Type
{
	static final int COMMAND = 1;

	public static Type_GetInfo process(VirtualMachineImpl vm, TypeMirror typeMirror) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm, typeMirror);
		return waitForReply(vm, ps);
	}

	static PacketStream enqueueCommand(VirtualMachineImpl vm, TypeMirror typeMirror)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
		{
			vm.printTrace("Sending Command(id=" + ps.pkt.id + ") Type_GetInfo" + (ps.pkt.flags != 0 ? ", " + "FLAGS=" + ps.pkt.flags : ""));
		}
		if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
		{
			ps.vm.printTrace("Sending:                 type(TypeMirror): " + "ref=" + typeMirror.id());
		}
		ps.writeId(typeMirror);
		ps.send();
		return ps;
	}

	static Type_GetInfo waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
	{
		ps.waitForReply();
		return new Type_GetInfo(vm, ps);
	}

	public final String namespace;
	public final String name;
	public final String fullName;

	private Type_GetInfo(VirtualMachineImpl vm, PacketStream ps)
	{
		if(vm.traceReceives)
		{
			vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") Type_GetInfo" + (ps.pkt.flags != 0 ? ", " +
					"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
		}
		namespace = ps.readString();
		if(vm.traceReceives)
		{
			vm.printReceiveTrace(4, "namespace(String): " + namespace);
		}
		name = ps.readString();
		if(vm.traceReceives)
		{
			vm.printReceiveTrace(4, "name(String): " + name);
		}
		fullName = ps.readString();
		if(vm.traceReceives)
		{
			vm.printReceiveTrace(4, "fullName(String): " + fullName);
		}
	}
}
