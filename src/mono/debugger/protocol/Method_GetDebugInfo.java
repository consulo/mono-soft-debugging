package mono.debugger.protocol;

import mono.debugger.JDWPException;
import mono.debugger.MethodMirror;
import mono.debugger.PacketStream;
import mono.debugger.VirtualMachineImpl;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class Method_GetDebugInfo implements Method
{
	static final int COMMAND = 3;

	public static class Entry
	{
		public int offset;
		public int line;
		public int column;
		public SourceFile sourceFile;
	}

	public static class SourceFile
	{
		public String name;
		public byte[] hash = new byte[16];
	}

	public static Method_GetDebugInfo process(VirtualMachineImpl vm, MethodMirror methodMirror) throws JDWPException
	{
		PacketStream ps = enqueueCommand(vm, methodMirror);
		return waitForReply(vm, ps);
	}

	static PacketStream enqueueCommand(VirtualMachineImpl vm, MethodMirror methodMirror)
	{
		PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
		ps.writeId(methodMirror);
		ps.send();
		return ps;
	}

	static Method_GetDebugInfo waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
	{
		ps.waitForReply();
		return new Method_GetDebugInfo(vm, ps);
	}

	public final int maxIndex;
	public final Entry[] entries;

	private Method_GetDebugInfo(VirtualMachineImpl vm, PacketStream ps)
	{
		maxIndex = ps.readInt();
		final SourceFile[] sourceFiles;
		if(vm.isAtLeastVersion(2, 13))
		{
			int count = ps.readId();
			sourceFiles = new SourceFile[count];
			for(int i = 0; i < count; i++)
			{
				sourceFiles[i] = new SourceFile();
				sourceFiles[i].name = ps.readString();
				if(vm.isAtLeastVersion(2, 14))
				{
					for(int j = 0; j < sourceFiles[i].hash.length; j++)
					{
						sourceFiles[i].hash[j] = ps.readByte();
					}
				}
			}
		}
		else
		{
			sourceFiles = new SourceFile[1];
			sourceFiles[0].name = ps.readString();
		}

		int count = ps.readInt();
		entries = new Entry[count];
		for(int i = 0; i < count; i++)
		{
			entries[i] = new Entry();
			entries[i].offset = ps.readInt();
			entries[i].line = ps.readInt();
			if(vm.isAtLeastVersion(2, 13))
			{
				int idx = ps.readInt();
				entries[i].sourceFile = idx >= 0 ? sourceFiles[idx] : null;
			}
			else
			{
				entries[i].sourceFile = sourceFiles[0];
			}

			if(vm.isAtLeastVersion(2, 19))
			{
				entries[i].column = ps.readInt();
			}
		}
	}
}