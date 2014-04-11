/*
 * Copyright (c) 1998, 2008, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package mono.debugger;

import java.io.ByteArrayOutputStream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import edu.arizona.cs.mbel.signature.SignatureConstants;

public class PacketStream
{
	public final VirtualMachineImpl vm;
	private int inCursor = 0;
	public final Packet pkt;
	private ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
	private boolean isCommitted = false;

	public PacketStream(VirtualMachineImpl vm, int cmdSet, int cmd)
	{
		this.vm = vm;
		this.pkt = new Packet();
		pkt.cmdSet = (short) cmdSet;
		pkt.cmd = (short) cmd;
	}

	public PacketStream(VirtualMachineImpl vm, Packet pkt)
	{
		this.vm = vm;
		this.pkt = pkt;
		this.isCommitted = true; /* read only stream */
	}

	int id()
	{
		return pkt.id;
	}

	public void send()
	{
		if(!isCommitted)
		{
			pkt.data = dataStream.toByteArray();
			vm.sendToTarget(pkt);
			isCommitted = true;
		}
	}

	public void waitForReply() throws JDWPException
	{
		if(!isCommitted)
		{
			throw new InternalException("waitForReply without send");
		}

		vm.waitForTargetReply(pkt);

		if(pkt.errorCode != Packet.ReplyNoError)
		{
			throw new JDWPException(pkt.errorCode);
		}
	}

	void writeBoolean(boolean data)
	{
		writeInt(data ? 1 : 0);
	}

	void writeByte(byte data)
	{
		dataStream.write(data);
	}

	void writeChar(char data)
	{
		dataStream.write((byte) ((data >>> 8) & 0xFF));
		dataStream.write((byte) ((data >>> 0) & 0xFF));
	}

	void writeShort(short data)
	{
		dataStream.write((byte) ((data >>> 8) & 0xFF));
		dataStream.write((byte) ((data >>> 0) & 0xFF));
	}

	public void writeInt(int data)
	{
		dataStream.write((byte) ((data >>> 24) & 0xFF));
		dataStream.write((byte) ((data >>> 16) & 0xFF));
		dataStream.write((byte) ((data >>> 8) & 0xFF));
		dataStream.write((byte) ((data >>> 0) & 0xFF));
	}

	void writeLong(long data)
	{
		dataStream.write((byte) ((data >>> 56) & 0xFF));
		dataStream.write((byte) ((data >>> 48) & 0xFF));
		dataStream.write((byte) ((data >>> 40) & 0xFF));
		dataStream.write((byte) ((data >>> 32) & 0xFF));

		dataStream.write((byte) ((data >>> 24) & 0xFF));
		dataStream.write((byte) ((data >>> 16) & 0xFF));
		dataStream.write((byte) ((data >>> 8) & 0xFF));
		dataStream.write((byte) ((data >>> 0) & 0xFF));
	}

	void writeFloat(float data)
	{
		writeInt(Float.floatToIntBits(data));
	}

	void writeDouble(double data)
	{
		writeLong(Double.doubleToLongBits(data));
	}

	public void writeId(MirrorWithId data)
	{
		writeInt((int) data.id());
	}

	void writeID(int size, long data)
	{
		switch(size)
		{
			case 8:
				writeLong(data);
				break;
			case 4:
				writeInt((int) data);
				break;
			case 2:
				writeShort((short) data);
				break;
			default:
				throw new UnsupportedOperationException("JDWP: ID size not supported: " + size);
		}
	}

	void writeNullObjectRef()
	{
		writeObjectRef(0);
	}

	@Deprecated
	void writeObjectRef(long data)
	{
		writeID(4, data);
	}

	void writeClassRef(long data)
	{
		writeID(4, data);
	}

	void writeMethodRef(long data)
	{
		writeID(4, data);
	}

	void writeFieldRef(long data)
	{
		writeID(4, data);
	}

	void writeFrameRef(long data)
	{
		writeID(4, data);
	}

	void writeByteArray(byte[] data)
	{
		dataStream.write(data, 0, data.length);
	}

	void writeString(String string)
	{
		try
		{
			byte[] stringBytes = string.getBytes("UTF8");
			writeInt(stringBytes.length);
			writeByteArray(stringBytes);
		}
		catch(java.io.UnsupportedEncodingException e)
		{
			throw new InternalException("Cannot convert string to UTF8 bytes");
		}
	}

	void writeLocation(Location location)
	{
		writeInt((int) location.method().id());
		writeLong(location.codeIndex());
	}

	/**
	 * Read byte represented as one bytes.
	 */
	public byte readByte()
	{
		byte ret = pkt.data[inCursor];
		inCursor += 1;
		return ret;
	}

	/**
	 * Read boolean represented as one byte.
	 */
	public boolean readBoolean()
	{
		int ret = readInt();
		return (ret != 0);
	}

	/**
	 * Read char represented as two bytes.
	 */
	char readChar()
	{
		int b1, b2;

		b1 = pkt.data[inCursor++] & 0xff;
		b2 = pkt.data[inCursor++] & 0xff;

		return (char) ((b1 << 8) + b2);
	}

	/**
	 * Read short represented as two bytes.
	 */
	short readShort()
	{
		int b1, b2;

		b1 = pkt.data[inCursor++] & 0xff;
		b2 = pkt.data[inCursor++] & 0xff;

		return (short) ((b1 << 8) + b2);
	}

	/**
	 * Read int represented as four bytes.
	 */
	public int readInt()
	{
		int b1, b2, b3, b4;

		b1 = pkt.data[inCursor++] & 0xff;
		b2 = pkt.data[inCursor++] & 0xff;
		b3 = pkt.data[inCursor++] & 0xff;
		b4 = pkt.data[inCursor++] & 0xff;

		return ((b1 << 24) + (b2 << 16) + (b3 << 8) + b4);
	}

	/**
	 * Read long represented as eight bytes.
	 */
	long readLong()
	{
		long b1, b2, b3, b4;
		long b5, b6, b7, b8;

		b1 = pkt.data[inCursor++] & 0xff;
		b2 = pkt.data[inCursor++] & 0xff;
		b3 = pkt.data[inCursor++] & 0xff;
		b4 = pkt.data[inCursor++] & 0xff;

		b5 = pkt.data[inCursor++] & 0xff;
		b6 = pkt.data[inCursor++] & 0xff;
		b7 = pkt.data[inCursor++] & 0xff;
		b8 = pkt.data[inCursor++] & 0xff;

		return ((b1 << 56) + (b2 << 48) + (b3 << 40) + (b4 << 32) + (b5 << 24) + (b6 << 16) + (b7 << 8) + b8);
	}

	/**
	 * Read float represented as four bytes.
	 */
	float readFloat()
	{
		return Float.intBitsToFloat(readInt());
	}

	/**
	 * Read double represented as eight bytes.
	 */
	double readDouble()
	{
		return Double.longBitsToDouble(readLong());
	}

	/**
	 * Read string represented as four byte length followed by
	 * characters of the string.
	 */
	public String readString()
	{
		String ret;
		int len = readInt();

		try
		{
			ret = new String(pkt.data, inCursor, len, "UTF8");
		}
		catch(java.io.UnsupportedEncodingException e)
		{
			System.err.println(e);
			ret = "Conversion error!";
		}
		inCursor += len;
		return ret;
	}

	private long readID(int size)
	{
		switch(size)
		{
			case 8:
				return readLong();
			case 4:
				return (long) readInt();
			case 2:
				return (long) readShort();
			default:
				throw new UnsupportedOperationException("JDWP: ID size not supported: " + size);
		}
	}

	/**
	 * Read object represented as vm specific byte sequence.
	 */
	long readObjectRef()
	{
		return readID(4);
	}

	public int readId()
	{
		return readInt();
	}

	@Deprecated
	int readClassRef()
	{
		return (int) readID(4);
	}

	@NotNull
	public ThreadMirror readThreadMirror()
	{
		long ref = readObjectRef();
		return new ThreadMirror(vm, ref); //FIXME [VISTALL] caching?
	}

	@Nullable
	public AssemblyMirror readAssemblyMirror()
	{
		long ref = readId();
		if(ref == 0)
		{
			return null;
		}
		return new AssemblyMirror(vm, ref);  //FIXME [VISTALL] caching?
	}

	@NotNull
	public AppDomainMirror readAppDomainMirror()
	{
		int ref = readId();
		return new AppDomainMirror(vm, ref); //FIXME [VISTALL] caching?
	}

	@Nullable
	public MethodMirror readMethodMirror()
	{
		int ref = readId();
		if(ref == 0)
		{
			return null;
		}
		return new MethodMirror(vm, ref); //FIXME [VISTALL] caching?
	}

	@Nullable
	public TypeMirror readTypeMirror()
	{
		int ref = readId();
		if(ref == 0)
		{
			return null;
		}
		return new TypeMirror(vm, ref); //FIXME [VISTALL] caching?
	}

	@NotNull
	public ObjectValueMirror readObjectMirror()
	{
		int ref = readId();
		return new ObjectValueMirror(vm, ref); //FIXME [VISTALL] caching?
	}

	@NotNull
	public Value readValue()
	{
		byte tag = readByte();
		switch(tag)
		{
			case SignatureConstants.ELEMENT_TYPE_I1:
				return new NumberValueMirror(vm, tag, readByte());
			case SignatureConstants.ELEMENT_TYPE_I2:
				return new NumberValueMirror(vm, tag, readShort());
			case SignatureConstants.ELEMENT_TYPE_I4:
				return new NumberValueMirror(vm, tag, readInt());
			case SignatureConstants.ELEMENT_TYPE_STRING:
				return new StringValueMirror(vm, readObjectMirror());
			case SignatureConstants.ELEMENT_TYPE_CLASS:
				return readObjectMirror();
			case SignatureConstants.ELEMENT_TYPE_SZARRAY:
				return new ArrayValueMirror(vm, tag, readObjectMirror());
			case (byte) 0xf0:
				return new NoObjectValue(vm);
			default:
				throw new IllegalArgumentException("Unsupported tag: " + tag);
		}
	}

	/**
	 * Read method reference represented as vm specific byte sequence.
	 */
	long readMethodRef()
	{
		return readID(4);
	}

	/**
	 * Read field reference represented as vm specific byte sequence.
	 */
	long readFieldRef()
	{
		return readID(4);
	}


	/**
	 * Read frame represented as vm specific byte sequence.
	 */
	long readFrameRef()
	{
		return readID(4);
	}

	/**
	 * Read location represented as vm specific byte sequence.
	 */
	public Location readLocation()
	{
		MethodMirror methodRef = readMethodMirror();
		int codeIndex = readInt();
		return new LocationImpl(vm, methodRef, codeIndex);
	}

	byte[] readByteArray(int length)
	{
		byte[] array = new byte[length];
		System.arraycopy(pkt.data, inCursor, array, 0, length);
		inCursor += length;
		return array;
	}

	int skipBytes(int n)
	{
		inCursor += n;
		return n;
	}

	byte command()
	{
		return (byte) pkt.cmd;
	}
}
