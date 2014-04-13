package mono.debugger.protocol;

import edu.arizona.cs.mbel.signature.SignatureConstants;
import mono.debugger.AppDomainMirror;
import mono.debugger.JDWPException;
import mono.debugger.ObjectValueMirror;
import mono.debugger.PacketStream;
import mono.debugger.TypeMirror;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 13.04.14
 */
public class AppDomain_CreateBoxValue implements AppDomain
{
	static final int COMMAND = 7;

	public static AppDomain_CreateBoxValue process(VirtualMachineImpl vm, AppDomainMirror domainMirror, int tag, Object unboxed) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm, domainMirror, tag, unboxed);
		return waitForReply(vm, ps);
	}

	static PacketStream enqueueCommand(VirtualMachineImpl vm, AppDomainMirror domainMirror, int tag, Object unboxed)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		ps.writeId(domainMirror);
		TypeMirror t = null;
		switch(tag)
		{
			case SignatureConstants.ELEMENT_TYPE_I1:
				t = vm.findTypes("System.SByte", false)[0];
				break;
			case SignatureConstants.ELEMENT_TYPE_I2:
				t = vm.findTypes("System.Short", false)[0];
				break;
			case SignatureConstants.ELEMENT_TYPE_I4:
				t = vm.findTypes("System.Int32", false)[0];
				break;
			default:
				throw new IllegalArgumentException("Wrong type tag: 0x" + Integer.toHexString(tag));
		}
		ps.writeId(t);
		ps.writeByte((byte) tag);
		switch(tag)
		{
			case SignatureConstants.ELEMENT_TYPE_I1:
				ps.writeByte((Byte) unboxed);
				break;
			case SignatureConstants.ELEMENT_TYPE_I2:
				ps.writeShort((Short) unboxed);
				break;
			case SignatureConstants.ELEMENT_TYPE_I4:
				ps.writeInt((Integer) unboxed);
				break;
		}
		ps.send();
		return ps;
	}

	static AppDomain_CreateBoxValue waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
	{
		ps.waitForReply();
		return new AppDomain_CreateBoxValue(vm, ps);
	}

	public ObjectValueMirror value;

	private AppDomain_CreateBoxValue(VirtualMachineImpl vm, PacketStream ps)
	{
		value = ps.readObjectMirror();
	}
}
