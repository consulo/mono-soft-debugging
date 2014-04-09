package mono.debugger;

import java.util.List;


public class JDWP
{

	public static class VirtualMachine
	{
		static final int COMMAND_SET = 1;

		private VirtualMachine()
		{
		}  // hide constructor

		/**
		 * Returns the JDWP version implemented by the target VM.
		 * The version string format is implementation dependent.
		 */
		static class Version
		{
			static final int COMMAND = 1;

			static Version process(VirtualMachineImpl vm) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(VirtualMachineImpl vm)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.VirtualMachine.Version" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				ps.send();
				return ps;
			}

			static Version waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new Version(vm, ps);
			}


			/**
			 * Text information on the VM version
			 */
			final String description;

			/**
			 * Major JDWP Version number
			 */
			final int jdwpMajor;

			/**
			 * Minor JDWP Version number
			 */
			final int jdwpMinor;

			private Version(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.VirtualMachine.Version" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				description = ps.readString();
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "description(String): " + description);
				}
				jdwpMajor = ps.readInt();
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "jdwpMajor(int): " + jdwpMajor);
				}
				jdwpMinor = ps.readInt();
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "jdwpMinor(int): " + jdwpMinor);
				}
			}
		}

		/**
		 * Returns reference types for all the classes loaded by the target VM
		 * which match the given signature.
		 * Multple reference types will be returned if two or more class
		 * loaders have loaded a class of the same name.
		 * The search is confined to loaded classes only; no attempt is made
		 * to load a class of the given signature.
		 */
		public static class GetTypes
		{
			static final int COMMAND = 12;

			public static GetTypes process(VirtualMachineImpl vm, String signature, boolean ignoreCase) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, signature, ignoreCase);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(VirtualMachineImpl vm, String signature, boolean ignoreCase)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.VirtualMachine.GetTypes" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 signature(String): " + signature);
				}
				ps.writeString(signature);
				ps.writeBoolean(ignoreCase);
				ps.send();
				return ps;
			}

			static GetTypes waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new GetTypes(vm, ps);
			}

			static class ClassInfo
			{
				/**
				 * Matching loaded reference type
				 */
				final int typeID;


				private ClassInfo(VirtualMachineImpl vm, PacketStream ps)
				{
					typeID = ps.readClassRef();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "typeID(int): " + "ref=" + typeID);
					}
				}
			}


			/**
			 * Number of reference types that follow.
			 */
			final ClassInfo[] classes;

			private GetTypes(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.VirtualMachine.GetTypes" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "classes(ClassInfo[]): " + "");
				}
				int classesCount = ps.readInt();
				classes = new ClassInfo[classesCount];
				for(int i = 0; i < classesCount; i++)
				{
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "classes[i](ClassInfo): " + "");
					}
					classes[i] = new ClassInfo(vm, ps);
				}
			}
		}

		/**
		 * Returns all threads currently running in the target VM .
		 * The returned list contains threads created through
		 * java.lang.Thread, all native threads attached to
		 * the target VM through JNI, and system threads created
		 * by the target VM. Threads that have not yet been started
		 * and threads that have completed their execution are not
		 * included in the returned list.
		 */
		static class AllThreads
		{
			static final int COMMAND = 2;

			static AllThreads process(VirtualMachineImpl vm) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(VirtualMachineImpl vm)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.VirtualMachine.AllThreads" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				ps.send();
				return ps;
			}

			static AllThreads waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new AllThreads(vm, ps);
			}


			/**
			 * Number of threads that follow.
			 */
			final ThreadReferenceImpl[] threads;

			private AllThreads(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.VirtualMachine.AllThreads" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "threads(ThreadReferenceImpl[]): " + "");
				}
				int threadsCount = ps.readInt();
				threads = new ThreadReferenceImpl[threadsCount];
				for(int i = 0; i < threadsCount; i++)
				{
					threads[i] = ps.readThreadReference();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "threads[i](ThreadReferenceImpl): " + (threads[i] == null ? "NULL" : "ref=" + threads[i].ref()));
					}
				}
			}
		}

		/**
		 * Invalidates this virtual machine mirror.
		 * The communication channel to the target VM is closed, and
		 * the target VM prepares to accept another subsequent connection
		 * from this debugger or another debugger, including the
		 * following tasks:
		 * <ul>
		 * <li>All event requests are cancelled.
		 * <li>All threads suspended by the thread-level
		 * <a href="#JDWP_ThreadReference_Resume">resume</a> command
		 * or the VM-level
		 * <a href="#JDWP_VirtualMachine_Resume">resume</a> command
		 * are resumed as many times as necessary for them to run.
		 * <li>Garbage collection is re-enabled in all cases where it was
		 * <a href="#JDWP_ObjectReference_DisableCollection">disabled</a>
		 * </ul>
		 * Any current method invocations executing in the target VM
		 * are continued after the disconnection. Upon completion of any such
		 * method invocation, the invoking thread continues from the
		 * location where it was originally stopped.
		 * <p/>
		 * Resources originating in
		 * this VirtualMachine (ObjectReferences, ReferenceTypes, etc.)
		 * will become invalid.
		 */
		static class Dispose
		{
			static final int COMMAND = 6;

			static Dispose process(VirtualMachineImpl vm) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(VirtualMachineImpl vm)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.VirtualMachine.Dispose" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				ps.send();
				return ps;
			}

			static Dispose waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new Dispose(vm, ps);
			}


			private Dispose(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.VirtualMachine.Dispose" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
			}
		}

		/**
		 * Suspends the execution of the application running in the target
		 * VM. All Java threads currently running will be suspended.
		 * <p/>
		 * Unlike java.lang.Thread.suspend,
		 * suspends of both the virtual machine and individual threads are
		 * counted. Before a thread will run again, it must be resumed through
		 * the <a href="#JDWP_VirtualMachine_Resume">VM-level resume</a> command
		 * or the <a href="#JDWP_ThreadReference_Resume">thread-level resume</a> command
		 * the same number of times it has been suspended.
		 */
		static class Suspend
		{
			static final int COMMAND = 3;

			static Suspend process(VirtualMachineImpl vm) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(VirtualMachineImpl vm)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.VirtualMachine.Suspend" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				ps.send();
				return ps;
			}

			static Suspend waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new Suspend(vm, ps);
			}


			private Suspend(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.VirtualMachine.Suspend" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
			}
		}

		/**
		 * Resumes execution of the application after the suspend
		 * command or an event has stopped it.
		 * Suspensions of the Virtual Machine and individual threads are
		 * counted. If a particular thread is suspended n times, it must
		 * resumed n times before it will continue.
		 */
		static class Resume
		{
			static final int COMMAND = 4;

			static Resume process(VirtualMachineImpl vm) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(VirtualMachineImpl vm)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.VirtualMachine.Resume" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				ps.send();
				return ps;
			}

			static Resume waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new Resume(vm, ps);
			}


			private Resume(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.VirtualMachine.Resume" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
			}
		}

		/**
		 * Terminates the target VM with the given exit code.
		 * On some platforms, the exit code might be truncated, for
		 * example, to the low order 8 bits.
		 * All ids previously returned from the target VM become invalid.
		 * Threads running in the VM are abruptly terminated.
		 * A thread death exception is not thrown and
		 * finally blocks are not run.
		 */
		static class Exit
		{
			static final int COMMAND = 5;

			static Exit process(
					VirtualMachineImpl vm, int exitCode) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, exitCode);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, int exitCode)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.VirtualMachine.Exit" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 exitCode(int): " + exitCode);
				}
				ps.writeInt(exitCode);
				ps.send();
				return ps;
			}

			static Exit waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new Exit(vm, ps);
			}


			private Exit(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.VirtualMachine.Exit" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
			}
		}
	}

	static class ReferenceType
	{
		static final int COMMAND_SET = 23;

		private ReferenceType()
		{
		}  // hide constructor

		static class Info
		{
			static final int COMMAND = 1;

			static Info process(VirtualMachineImpl vm, ReferenceTypeImpl refType) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, refType);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(VirtualMachineImpl vm, ReferenceTypeImpl refType)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.ReferenceType.Info" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:    refType(ReferenceTypeImpl): " + (refType == null ? "NULL" : "ref=" + refType.ref()));
				}
				ps.writeClassRef(refType.ref());
				ps.send();
				return ps;
			}

			static Info waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new Info(vm, ps);
			}


			final String namespace;
			final String name;
			final String fullName;
			final int assemblyId;

			private Info(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.ReferenceType.Info" + (ps.pkt.flags != 0 ? ", " +
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
				assemblyId = ps.readInt();
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "fullName(assemblyId): " + assemblyId);
				}
			}
		}


		/**
		 * Returns the JNI signature of a reference type.
		 * JNI signature formats are described in the
		 * <a href="http://java.sun.com/products/jdk/1.2/docs/guide/jni/index.html">Java Native Inteface Specification</a>
		 * <p/>
		 * For primitive classes
		 * the returned signature is the signature of the corresponding primitive
		 * type; for example, "I" is returned as the signature of the class
		 * represented by java.lang.Integer.TYPE.
		 */
		static class Signature
		{
			static final int COMMAND = 1;

			static Signature process(
					VirtualMachineImpl vm, ReferenceTypeImpl refType) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, refType);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ReferenceTypeImpl refType)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.ReferenceType.Signature" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 refType(ReferenceTypeImpl): " + (refType == null ? "NULL" : "ref=" + refType.ref()));
				}
				ps.writeClassRef(refType.ref());
				ps.send();
				return ps;
			}

			static Signature waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new Signature(vm, ps);
			}


			/**
			 * The JNI signature for the reference type.
			 */
			final String signature;

			private Signature(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.ReferenceType.Signature" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				signature = ps.readString();
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "signature(String): " + signature);
				}
			}
		}


		/**
		 * Returns the modifiers (also known as access flags) for a reference type.
		 * The returned bit mask contains information on the declaration
		 * of the reference type. If the reference type is an array or
		 * a primitive class (for example, java.lang.Integer.TYPE), the
		 * value of the returned bit mask is undefined.
		 */
		static class Modifiers
		{
			static final int COMMAND = 3;

			static Modifiers process(
					VirtualMachineImpl vm, ReferenceTypeImpl refType) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, refType);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ReferenceTypeImpl refType)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.ReferenceType.Modifiers" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 refType(ReferenceTypeImpl): " + (refType == null ? "NULL" : "ref=" + refType.ref()));
				}
				ps.writeClassRef(refType.ref());
				ps.send();
				return ps;
			}

			static Modifiers waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new Modifiers(vm, ps);
			}


			/**
			 * Modifier bits as defined in Chapter 4 of
			 * <cite>The Java&trade; Virtual Machine Specification</cite>
			 */
			final int modBits;

			private Modifiers(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.ReferenceType.Modifiers" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				modBits = ps.readInt();
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "modBits(int): " + modBits);
				}
			}
		}

		/**
		 * Returns information for each field in a reference type.
		 * Inherited fields are not included.
		 * The field list will include any synthetic fields created
		 * by the compiler.
		 * Fields are returned in the order they occur in the class file.
		 */
		static class Fields
		{
			static final int COMMAND = 4;

			static Fields process(
					VirtualMachineImpl vm, ReferenceTypeImpl refType) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, refType);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ReferenceTypeImpl refType)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.ReferenceType.Fields" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 refType(ReferenceTypeImpl): " + (refType == null ? "NULL" : "ref=" + refType.ref()));
				}
				ps.writeClassRef(refType.ref());
				ps.send();
				return ps;
			}

			static Fields waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new Fields(vm, ps);
			}

			static class FieldInfo
			{

				/**
				 * Field ID.
				 */
				final long fieldID;

				/**
				 * Name of field.
				 */
				final String name;

				/**
				 * JNI Signature of field.
				 */
				final String signature;

				/**
				 * The modifier bit flags (also known as access flags)
				 * which provide additional information on the
				 * field declaration. Individual flag values are
				 * defined in Chapter 4 of
				 * <cite>The Java&trade; Virtual Machine Specification</cite>.
				 * In addition, The <code>0xf0000000</code> bit identifies
				 * the field as synthetic, if the synthetic attribute
				 * <a href="#JDWP_VirtualMachine_Capabilities">capability</a> is available.
				 */
				final int modBits;

				private FieldInfo(VirtualMachineImpl vm, PacketStream ps)
				{
					fieldID = ps.readFieldRef();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "fieldID(long): " + fieldID);
					}
					name = ps.readString();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "name(String): " + name);
					}
					signature = ps.readString();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "signature(String): " + signature);
					}
					modBits = ps.readInt();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "modBits(int): " + modBits);
					}
				}
			}


			/**
			 * Number of declared fields.
			 */
			final FieldInfo[] declared;

			private Fields(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.ReferenceType.Fields" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "declared(FieldInfo[]): " + "");
				}
				int declaredCount = ps.readInt();
				declared = new FieldInfo[declaredCount];
				for(int i = 0; i < declaredCount; i++)
				{
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "declared[i](FieldInfo): " + "");
					}
					declared[i] = new FieldInfo(vm, ps);
				}
			}
		}

		/**
		 * Returns information for each method in a reference type.
		 * Inherited methods are not included. The list of methods will
		 * include constructors (identified with the name "&lt;init&gt;"),
		 * the initialization method (identified with the name "&lt;clinit&gt;")
		 * if present, and any synthetic methods created by the compiler.
		 * Methods are returned in the order they occur in the class file.
		 */
		static class Methods
		{
			static final int COMMAND = 5;

			static Methods process(
					VirtualMachineImpl vm, ReferenceTypeImpl refType) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, refType);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ReferenceTypeImpl refType)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.ReferenceType.Methods" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 refType(ReferenceTypeImpl): " + (refType == null ? "NULL" : "ref=" + refType.ref()));
				}
				ps.writeClassRef(refType.ref());
				ps.send();
				return ps;
			}

			static Methods waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new Methods(vm, ps);
			}

			static class MethodInfo
			{

				/**
				 * Method ID.
				 */
				final long methodID;

				/**
				 * Name of method.
				 */
				final String name;

				/**
				 * JNI signature of method.
				 */
				final String signature;

				/**
				 * The modifier bit flags (also known as access flags)
				 * which provide additional information on the
				 * method declaration. Individual flag values are
				 * defined in Chapter 4 of
				 * <cite>The Java&trade; Virtual Machine Specification</cite>.
				 * In addition, The <code>0xf0000000</code> bit identifies
				 * the method as synthetic, if the synthetic attribute
				 * <a href="#JDWP_VirtualMachine_Capabilities">capability</a> is available.
				 */
				final int modBits;

				private MethodInfo(VirtualMachineImpl vm, PacketStream ps)
				{
					methodID = ps.readMethodRef();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "methodID(long): " + methodID);
					}
					name = ps.readString();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "name(String): " + name);
					}
					signature = ps.readString();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "signature(String): " + signature);
					}
					modBits = ps.readInt();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "modBits(int): " + modBits);
					}
				}
			}


			/**
			 * Number of declared methods.
			 */
			final MethodInfo[] declared;

			private Methods(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.ReferenceType.Methods" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "declared(MethodInfo[]): " + "");
				}
				int declaredCount = ps.readInt();
				declared = new MethodInfo[declaredCount];
				for(int i = 0; i < declaredCount; i++)
				{
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "declared[i](MethodInfo): " + "");
					}
					declared[i] = new MethodInfo(vm, ps);
				}
			}
		}

		/**
		 * Returns the value of one or more static fields of the
		 * reference type. Each field must be member of the reference type
		 * or one of its superclasses, superinterfaces, or implemented interfaces.
		 * Access control is not enforced; for example, the values of private
		 * fields can be obtained.
		 */
		static class GetValues
		{
			static final int COMMAND = 6;

			static class Field
			{

				/**
				 * A field to get
				 */
				final long fieldID;

				Field(long fieldID)
				{
					this.fieldID = fieldID;
				}

				private void write(PacketStream ps)
				{
					if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
					{
						ps.vm.printTrace("Sending:                     fieldID(long): " + fieldID);
					}
					ps.writeFieldRef(fieldID);
				}
			}

			static GetValues process(
					VirtualMachineImpl vm, ReferenceTypeImpl refType, Field[] fields) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, refType, fields);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ReferenceTypeImpl refType, Field[] fields)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.ReferenceType.GetValues" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 refType(ReferenceTypeImpl): " + (refType == null ? "NULL" : "ref=" + refType.ref()));
				}
				ps.writeClassRef(refType.ref());
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 fields(Field[]): " + "");
				}
				ps.writeInt(fields.length);
				for(int i = 0; i < fields.length; i++)
				{
					if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
					{
						ps.vm.printTrace("Sending:                     fields[i](Field): " + "");
					}
					fields[i].write(ps);
				}
				ps.send();
				return ps;
			}

			static GetValues waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new GetValues(vm, ps);
			}


			/**
			 * The number of values returned, always equal to fields,
			 * the number of values to get.
			 */
			final ValueImpl[] values;

			private GetValues(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.ReferenceType.GetValues" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "values(ValueImpl[]): " + "");
				}
				int valuesCount = ps.readInt();
				values = new ValueImpl[valuesCount];
				for(int i = 0; i < valuesCount; i++)
				{
					values[i] = ps.readValue();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "values[i](ValueImpl): " + values[i]);
					}
				}
			}
		}

		/**
		 * Returns the name of source file in which a reference type was
		 * declared.
		 */
		static class SourceFile
		{
			static final int COMMAND = 7;

			static SourceFile process(
					VirtualMachineImpl vm, ReferenceTypeImpl refType) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, refType);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ReferenceTypeImpl refType)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.ReferenceType.SourceFile" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 refType(ReferenceTypeImpl): " + (refType == null ? "NULL" : "ref=" + refType.ref()));
				}
				ps.writeClassRef(refType.ref());
				ps.send();
				return ps;
			}

			static SourceFile waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new SourceFile(vm, ps);
			}


			/**
			 * The source file name. No path information
			 * for the file is included
			 */
			final String sourceFile;

			private SourceFile(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.ReferenceType.SourceFile" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				sourceFile = ps.readString();
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "sourceFile(String): " + sourceFile);
				}
			}
		}

		/**
		 * Returns the classes and interfaces directly nested within this type.
		 * Types further nested within those types are not included.
		 */
		static class NestedTypes
		{
			static final int COMMAND = 8;

			static NestedTypes process(
					VirtualMachineImpl vm, ReferenceTypeImpl refType) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, refType);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ReferenceTypeImpl refType)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.ReferenceType.NestedTypes" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 refType(ReferenceTypeImpl): " + (refType == null ? "NULL" : "ref=" + refType.ref()));
				}
				ps.writeClassRef(refType.ref());
				ps.send();
				return ps;
			}

			static NestedTypes waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new NestedTypes(vm, ps);
			}

			static class TypeInfo
			{

				/**
				 * <a href="#JDWP_TypeTag">Kind</a>
				 * of following reference type.
				 */
				final byte refTypeTag;

				/**
				 * The nested class or interface ID.
				 */
				final long typeID;

				private TypeInfo(VirtualMachineImpl vm, PacketStream ps)
				{
					refTypeTag = ps.readByte();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "refTypeTag(byte): " + refTypeTag);
					}
					typeID = ps.readClassRef();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "typeID(long): " + "ref=" + typeID);
					}
				}
			}


			/**
			 * The number of nested classes and interfaces
			 */
			final TypeInfo[] classes;

			private NestedTypes(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.ReferenceType.NestedTypes" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "classes(TypeInfo[]): " + "");
				}
				int classesCount = ps.readInt();
				classes = new TypeInfo[classesCount];
				for(int i = 0; i < classesCount; i++)
				{
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "classes[i](TypeInfo): " + "");
					}
					classes[i] = new TypeInfo(vm, ps);
				}
			}
		}

		/**
		 * Returns the current state of the reference type. The state
		 * indicates the extent to which the reference type has been
		 * initialized, as described in section 2.1.6 of
		 * <cite>The Java&trade; Virtual Machine Specification</cite>.
		 * If the class is linked the PREPARED and VERIFIED bits in the returned state bits
		 * will be set. If the class is initialized the INITIALIZED bit in the returned
		 * state bits will be set. If an error occured during initialization then the
		 * ERROR bit in the returned state bits will be set.
		 * The returned state bits are undefined for array types and for
		 * primitive classes (such as java.lang.Integer.TYPE).
		 */
		static class Status
		{
			static final int COMMAND = 9;

			static Status process(
					VirtualMachineImpl vm, ReferenceTypeImpl refType) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, refType);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ReferenceTypeImpl refType)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.ReferenceType.Status" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 refType(ReferenceTypeImpl): " + (refType == null ? "NULL" : "ref=" + refType.ref()));
				}
				ps.writeClassRef(refType.ref());
				ps.send();
				return ps;
			}

			static Status waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new Status(vm, ps);
			}


			/**
			 * <a href="#JDWP_ClassStatus">Status</a> bits:
			 * See <a href="#JDWP_ClassStatus">JDWP.ClassStatus</a>
			 */
			final int status;

			private Status(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.ReferenceType.Status" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				status = ps.readInt();
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "state(int): " + status);
				}
			}
		}

		/**
		 * Returns the class object corresponding to this type.
		 */
		static class ClassObject
		{
			static final int COMMAND = 11;

			static ClassObject process(
					VirtualMachineImpl vm, ReferenceTypeImpl refType) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, refType);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ReferenceTypeImpl refType)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.ReferenceType.ClassObject" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 refType(ReferenceTypeImpl): " + (refType == null ? "NULL" : "ref=" + refType.ref()));
				}
				ps.writeClassRef(refType.ref());
				ps.send();
				return ps;
			}

			static ClassObject waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new ClassObject(vm, ps);
			}


			/**
			 * class object.
			 */
			final ClassObjectReferenceImpl classObject;

			private ClassObject(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.ReferenceType.ClassObject" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				classObject = ps.readClassObjectReference();
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "classObject(ClassObjectReferenceImpl): " + (classObject == null ? "NULL" : "ref=" + classObject.ref()));
				}
			}
		}

		/**
		 * Returns the value of the SourceDebugExtension attribute.
		 * Since JDWP version 1.4. Requires canGetSourceDebugExtension capability - see
		 * <a href="#JDWP_VirtualMachine_CapabilitiesNew">CapabilitiesNew</a>.
		 */
		static class SourceDebugExtension
		{
			static final int COMMAND = 12;

			static SourceDebugExtension process(
					VirtualMachineImpl vm, ReferenceTypeImpl refType) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, refType);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ReferenceTypeImpl refType)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.ReferenceType.SourceDebugExtension" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 refType(ReferenceTypeImpl): " + (refType == null ? "NULL" : "ref=" + refType.ref()));
				}
				ps.writeClassRef(refType.ref());
				ps.send();
				return ps;
			}

			static SourceDebugExtension waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new SourceDebugExtension(vm, ps);
			}


			/**
			 * extension attribute
			 */
			final String extension;

			private SourceDebugExtension(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.ReferenceType.SourceDebugExtension" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				extension = ps.readString();
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "extension(String): " + extension);
				}
			}
		}

		/**
		 * Returns the JNI signature of a reference type along with the
		 * generic signature if there is one.
		 * Generic signatures are described in the signature attribute
		 * section in
		 * <cite>The Java&trade; Virtual Machine Specification</cite>.
		 * Since JDWP version 1.5.
		 * <p/>
		 */
		static class SignatureWithGeneric
		{
			static final int COMMAND = 13;

			static SignatureWithGeneric process(
					VirtualMachineImpl vm, ReferenceTypeImpl refType) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, refType);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ReferenceTypeImpl refType)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.ReferenceType.SignatureWithGeneric" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 refType(ReferenceTypeImpl): " + (refType == null ? "NULL" : "ref=" + refType.ref()));
				}
				ps.writeClassRef(refType.ref());
				ps.send();
				return ps;
			}

			static SignatureWithGeneric waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new SignatureWithGeneric(vm, ps);
			}


			/**
			 * The JNI signature for the reference type.
			 */
			final String signature;

			/**
			 * The generic signature for the reference type or an empty
			 * string if there is none.
			 */
			final String genericSignature;

			private SignatureWithGeneric(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.ReferenceType.SignatureWithGeneric" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				signature = ps.readString();
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "signature(String): " + signature);
				}
				genericSignature = ps.readString();
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "genericSignature(String): " + genericSignature);
				}
			}
		}

		/**
		 * Returns information, including the generic signature if any,
		 * for each field in a reference type.
		 * Inherited fields are not included.
		 * The field list will include any synthetic fields created
		 * by the compiler.
		 * Fields are returned in the order they occur in the class file.
		 * Generic signatures are described in the signature attribute
		 * section in
		 * <cite>The Java&trade; Virtual Machine Specification</cite>.
		 * Since JDWP version 1.5.
		 */
		static class FieldsWithGeneric
		{
			static final int COMMAND = 14;

			static FieldsWithGeneric process(
					VirtualMachineImpl vm, ReferenceTypeImpl refType) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, refType);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ReferenceTypeImpl refType)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.ReferenceType.FieldsWithGeneric" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 refType(ReferenceTypeImpl): " + (refType == null ? "NULL" : "ref=" + refType.ref()));
				}
				ps.writeClassRef(refType.ref());
				ps.send();
				return ps;
			}

			static FieldsWithGeneric waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new FieldsWithGeneric(vm, ps);
			}

			static class FieldInfo
			{

				/**
				 * Field ID.
				 */
				final long fieldID;

				/**
				 * The name of the field.
				 */
				final String name;

				/**
				 * The JNI signature of the field.
				 */
				final String signature;

				/**
				 * The generic signature of the
				 * field, or an empty string if there is none.
				 */
				final String genericSignature;

				/**
				 * The modifier bit flags (also known as access flags)
				 * which provide additional information on the
				 * field declaration. Individual flag values are
				 * defined in Chapter 4 of
				 * <cite>The Java&trade; Virtual Machine Specification</cite>.
				 * In addition, The <code>0xf0000000</code> bit identifies
				 * the field as synthetic, if the synthetic attribute
				 * <a href="#JDWP_VirtualMachine_Capabilities">capability</a> is available.
				 */
				final int modBits;

				private FieldInfo(VirtualMachineImpl vm, PacketStream ps)
				{
					fieldID = ps.readFieldRef();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "fieldID(long): " + fieldID);
					}
					name = ps.readString();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "name(String): " + name);
					}
					signature = ps.readString();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "signature(String): " + signature);
					}
					genericSignature = ps.readString();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "genericSignature(String): " + genericSignature);
					}
					modBits = ps.readInt();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "modBits(int): " + modBits);
					}
				}
			}


			/**
			 * Number of declared fields.
			 */
			final FieldInfo[] declared;

			private FieldsWithGeneric(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.ReferenceType.FieldsWithGeneric" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "declared(FieldInfo[]): " + "");
				}
				int declaredCount = ps.readInt();
				declared = new FieldInfo[declaredCount];
				for(int i = 0; i < declaredCount; i++)
				{
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "declared[i](FieldInfo): " + "");
					}
					declared[i] = new FieldInfo(vm, ps);
				}
			}
		}

		/**
		 * Returns information, including the generic signature if any,
		 * for each method in a reference type.
		 * Inherited methodss are not included. The list of methods will
		 * include constructors (identified with the name "&lt;init&gt;"),
		 * the initialization method (identified with the name "&lt;clinit&gt;")
		 * if present, and any synthetic methods created by the compiler.
		 * Methods are returned in the order they occur in the class file.
		 * Generic signatures are described in the signature attribute
		 * section in
		 * <cite>The Java&trade; Virtual Machine Specification</cite>.
		 * Since JDWP version 1.5.
		 */
		static class MethodsWithGeneric
		{
			static final int COMMAND = 15;

			static MethodsWithGeneric process(
					VirtualMachineImpl vm, ReferenceTypeImpl refType) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, refType);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ReferenceTypeImpl refType)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.ReferenceType.MethodsWithGeneric" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 refType(ReferenceTypeImpl): " + (refType == null ? "NULL" : "ref=" + refType.ref()));
				}
				ps.writeClassRef(refType.ref());
				ps.send();
				return ps;
			}

			static MethodsWithGeneric waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new MethodsWithGeneric(vm, ps);
			}

			static class MethodInfo
			{

				/**
				 * Method ID.
				 */
				final long methodID;

				/**
				 * The name of the method.
				 */
				final String name;

				/**
				 * The JNI signature of the method.
				 */
				final String signature;

				/**
				 * The generic signature of the method, or
				 * an empty string if there is none.
				 */
				final String genericSignature;

				/**
				 * The modifier bit flags (also known as access flags)
				 * which provide additional information on the
				 * method declaration. Individual flag values are
				 * defined in Chapter 4 of
				 * <cite>The Java&trade; Virtual Machine Specification</cite>.
				 * In addition, The <code>0xf0000000</code> bit identifies
				 * the method as synthetic, if the synthetic attribute
				 * <a href="#JDWP_VirtualMachine_Capabilities">capability</a> is available.
				 */
				final int modBits;

				private MethodInfo(VirtualMachineImpl vm, PacketStream ps)
				{
					methodID = ps.readMethodRef();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "methodID(long): " + methodID);
					}
					name = ps.readString();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "name(String): " + name);
					}
					signature = ps.readString();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "signature(String): " + signature);
					}
					genericSignature = ps.readString();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "genericSignature(String): " + genericSignature);
					}
					modBits = ps.readInt();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "modBits(int): " + modBits);
					}
				}
			}


			/**
			 * Number of declared methods.
			 */
			final MethodInfo[] declared;

			private MethodsWithGeneric(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.ReferenceType.MethodsWithGeneric" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "declared(MethodInfo[]): " + "");
				}
				int declaredCount = ps.readInt();
				declared = new MethodInfo[declaredCount];
				for(int i = 0; i < declaredCount; i++)
				{
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "declared[i](MethodInfo): " + "");
					}
					declared[i] = new MethodInfo(vm, ps);
				}
			}
		}

		/**
		 * Returns instances of this reference type.
		 * Only instances that are reachable for the purposes of
		 * garbage collection are returned.
		 * <p>Since JDWP version 1.6. Requires canGetInstanceInfo capability - see
		 * <a href="#JDWP_VirtualMachine_CapabilitiesNew">CapabilitiesNew</a>.
		 */
		static class Instances
		{
			static final int COMMAND = 16;

			static Instances process(
					VirtualMachineImpl vm, ReferenceTypeImpl refType, int maxInstances) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, refType, maxInstances);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ReferenceTypeImpl refType, int maxInstances)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.ReferenceType.Instances" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 refType(ReferenceTypeImpl): " + (refType == null ? "NULL" : "ref=" + refType.ref()));
				}
				ps.writeClassRef(refType.ref());
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 maxInstances(int): " + maxInstances);
				}
				ps.writeInt(maxInstances);
				ps.send();
				return ps;
			}

			static Instances waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new Instances(vm, ps);
			}


			/**
			 * The number of instances that follow.
			 */
			final ObjectReferenceImpl[] instances;

			private Instances(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.ReferenceType.Instances" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "instances(ObjectReferenceImpl[]): " + "");
				}
				int instancesCount = ps.readInt();
				instances = new ObjectReferenceImpl[instancesCount];
				for(int i = 0; i < instancesCount; i++)
				{
					instances[i] = ps.readTaggedObjectReference();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "instances[i](ObjectReferenceImpl): " + (instances[i] == null ? "NULL" : "ref=" + instances[i].ref
								()));
					}
				}
			}
		}

		/**
		 * Returns the class file major and minor version numbers, as defined in the class
		 * file format of the Java Virtual Machine specification.
		 * <p>Since JDWP version 1.6.
		 */
		static class ClassFileVersion
		{
			static final int COMMAND = 17;

			static ClassFileVersion process(
					VirtualMachineImpl vm, ReferenceTypeImpl refType) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, refType);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ReferenceTypeImpl refType)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.ReferenceType.ClassFileVersion" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 refType(ReferenceTypeImpl): " + (refType == null ? "NULL" : "ref=" + refType.ref()));
				}
				ps.writeClassRef(refType.ref());
				ps.send();
				return ps;
			}

			static ClassFileVersion waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new ClassFileVersion(vm, ps);
			}


			/**
			 * Major version number
			 */
			final int majorVersion;

			/**
			 * Minor version number
			 */
			final int minorVersion;

			private ClassFileVersion(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.ReferenceType.ClassFileVersion" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				majorVersion = ps.readInt();
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "majorVersion(int): " + majorVersion);
				}
				minorVersion = ps.readInt();
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "minorVersion(int): " + minorVersion);
				}
			}
		}

		/**
		 * Return the raw bytes of the constant pool in the format of the
		 * constant_pool item of the Class File Format in
		 * <cite>The Java&trade; Virtual Machine Specification</cite>.
		 * <p>Since JDWP version 1.6. Requires canGetConstantPool capability - see
		 * <a href="#JDWP_VirtualMachine_CapabilitiesNew">CapabilitiesNew</a>.
		 */
		static class ConstantPool
		{
			static final int COMMAND = 18;

			static ConstantPool process(
					VirtualMachineImpl vm, ReferenceTypeImpl refType) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, refType);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ReferenceTypeImpl refType)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.ReferenceType.ConstantPool" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 refType(ReferenceTypeImpl): " + (refType == null ? "NULL" : "ref=" + refType.ref()));
				}
				ps.writeClassRef(refType.ref());
				ps.send();
				return ps;
			}

			static ConstantPool waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new ConstantPool(vm, ps);
			}


			/**
			 * Total number of constant pool entries plus one. This
			 * corresponds to the constant_pool_count item of the
			 * Class File Format in
			 * <cite>The Java&trade; Virtual Machine Specification</cite>.
			 */
			final int count;

			final byte[] bytes;

			private ConstantPool(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.ReferenceType.ConstantPool" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				count = ps.readInt();
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "count(int): " + count);
				}
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "bytes(byte[]): " + "");
				}
				int bytesCount = ps.readInt();
				bytes = new byte[bytesCount];
				for(int i = 0; i < bytesCount; i++)
				{
					bytes[i] = ps.readByte();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "bytes[i](byte): " + bytes[i]);
					}
				}
			}
		}
	}

	static class ClassType
	{
		static final int COMMAND_SET = 3;

		private ClassType()
		{
		}  // hide constructor

		/**
		 * Returns the immediate superclass of a class.
		 */
		static class Superclass
		{
			static final int COMMAND = 1;

			static Superclass process(
					VirtualMachineImpl vm, ClassTypeImpl clazz) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, clazz);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ClassTypeImpl clazz)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.ClassType.Superclass" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 clazz(ClassTypeImpl): " + (clazz == null ? "NULL" : "ref=" + clazz.ref()));
				}
				ps.writeClassRef(clazz.ref());
				ps.send();
				return ps;
			}

			static Superclass waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new Superclass(vm, ps);
			}


			/**
			 * The superclass (null if the class ID for java.lang.Object is specified).
			 */
			final ClassTypeImpl superclass;

			private Superclass(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.ClassType.Superclass" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				superclass = vm.classType(ps.readClassRef());
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "superclass(ClassTypeImpl): " + (superclass == null ? "NULL" : "ref=" + superclass.ref()));
				}
			}
		}

		/**
		 * Sets the value of one or more static fields.
		 * Each field must be member of the class type
		 * or one of its superclasses, superinterfaces, or implemented interfaces.
		 * Access control is not enforced; for example, the values of private
		 * fields can be set. Final fields cannot be set.
		 * For primitive values, the value's type must match the
		 * field's type exactly. For object values, there must exist a
		 * widening reference conversion from the value's type to the
		 * field's type and the field's type must be loaded.
		 */
		static class SetValues
		{
			static final int COMMAND = 2;

			/**
			 * A Field/Value pair.
			 */
			static class FieldValue
			{

				/**
				 * Field to set.
				 */
				final long fieldID;

				/**
				 * Value to put in the field.
				 */
				final ValueImpl value;

				FieldValue(long fieldID, ValueImpl value)
				{
					this.fieldID = fieldID;
					this.value = value;
				}

				private void write(PacketStream ps)
				{
					if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
					{
						ps.vm.printTrace("Sending:                     fieldID(long): " + fieldID);
					}
					ps.writeFieldRef(fieldID);
					if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
					{
						ps.vm.printTrace("Sending:                     value(ValueImpl): " + value);
					}
					ps.writeUntaggedValue(value);
				}
			}

			static SetValues process(
					VirtualMachineImpl vm, ClassTypeImpl clazz, FieldValue[] values) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, clazz, values);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ClassTypeImpl clazz, FieldValue[] values)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.ClassType.SetValues" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 clazz(ClassTypeImpl): " + (clazz == null ? "NULL" : "ref=" + clazz.ref()));
				}
				ps.writeClassRef(clazz.ref());
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 values(FieldValue[]): " + "");
				}
				ps.writeInt(values.length);
				for(int i = 0; i < values.length; i++)
				{
					if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
					{
						ps.vm.printTrace("Sending:                     values[i](FieldValue): " + "");
					}
					values[i].write(ps);
				}
				ps.send();
				return ps;
			}

			static SetValues waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new SetValues(vm, ps);
			}


			private SetValues(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.ClassType.SetValues" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
			}
		}

		/**
		 * Invokes a static method.
		 * The method must be member of the class type
		 * or one of its superclasses, superinterfaces, or implemented interfaces.
		 * Access control is not enforced; for example, private
		 * methods can be invoked.
		 * <p/>
		 * The method invocation will occur in the specified thread.
		 * Method invocation can occur only if the specified thread
		 * has been suspended by an event.
		 * Method invocation is not supported
		 * when the target VM has been suspended by the front-end.
		 * <p/>
		 * The specified method is invoked with the arguments in the specified
		 * argument list.
		 * The method invocation is synchronous; the reply packet is not
		 * sent until the invoked method returns in the target VM.
		 * The return value (possibly the void value) is
		 * included in the reply packet.
		 * If the invoked method throws an exception, the
		 * exception object ID is set in the reply packet; otherwise, the
		 * exception object ID is null.
		 * <p/>
		 * For primitive arguments, the argument value's type must match the
		 * argument's type exactly. For object arguments, there must exist a
		 * widening reference conversion from the argument value's type to the
		 * argument's type and the argument's type must be loaded.
		 * <p/>
		 * By default, all threads in the target VM are resumed while
		 * the method is being invoked if they were previously
		 * suspended by an event or by command.
		 * This is done to prevent the deadlocks
		 * that will occur if any of the threads own monitors
		 * that will be needed by the invoked method. It is possible that
		 * breakpoints or other events might occur during the invocation.
		 * Note, however, that this implicit resume acts exactly like
		 * the ThreadReference resume command, so if the thread's suspend
		 * count is greater than 1, it will remain in a suspended state
		 * during the invocation. By default, when the invocation completes,
		 * all threads in the target VM are suspended, regardless their state
		 * before the invocation.
		 * <p/>
		 * The resumption of other threads during the invoke can be prevented
		 * by specifying the INVOKE_SINGLE_THREADED
		 * bit flag in the <code>options</code> field; however,
		 * there is no protection against or recovery from the deadlocks
		 * described above, so this option should be used with great caution.
		 * Only the specified thread will be resumed (as described for all
		 * threads above). Upon completion of a single threaded invoke, the invoking thread
		 * will be suspended once again. Note that any threads started during
		 * the single threaded invocation will not be suspended when the
		 * invocation completes.
		 * <p/>
		 * If the target VM is disconnected during the invoke (for example, through
		 * the VirtualMachine dispose command) the method invocation continues.
		 */
		static class InvokeMethod
		{
			static final int COMMAND = 3;

			static InvokeMethod process(
					VirtualMachineImpl vm,
					ClassTypeImpl clazz,
					ThreadReferenceImpl thread,
					long methodID,
					ValueImpl[] arguments,
					int options) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, clazz, thread, methodID, arguments, options);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ClassTypeImpl clazz, ThreadReferenceImpl thread, long methodID, ValueImpl[] arguments, int options)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.ClassType.InvokeMethod" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 clazz(ClassTypeImpl): " + (clazz == null ? "NULL" : "ref=" + clazz.ref()));
				}
				ps.writeClassRef(clazz.ref());
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 thread(ThreadReferenceImpl): " + (thread == null ? "NULL" : "ref=" + thread.ref()));
				}
				ps.writeObjectRef(thread.ref());
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 methodID(long): " + methodID);
				}
				ps.writeMethodRef(methodID);
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 arguments(ValueImpl[]): " + "");
				}
				ps.writeInt(arguments.length);
				for(int i = 0; i < arguments.length; i++)
				{
					if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
					{
						ps.vm.printTrace("Sending:                     arguments[i](ValueImpl): " + arguments[i]);
					}
					ps.writeValue(arguments[i]);
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 options(int): " + options);
				}
				ps.writeInt(options);
				ps.send();
				return ps;
			}

			static InvokeMethod waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new InvokeMethod(vm, ps);
			}


			/**
			 * The returned value.
			 */
			final ValueImpl returnValue;

			/**
			 * The thrown exception.
			 */
			final ObjectReferenceImpl exception;

			private InvokeMethod(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.ClassType.InvokeMethod" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				returnValue = ps.readValue();
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "returnValue(ValueImpl): " + returnValue);
				}
				exception = ps.readTaggedObjectReference();
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "exception(ObjectReferenceImpl): " + (exception == null ? "NULL" : "ref=" + exception.ref()));
				}
			}
		}

		/**
		 * Creates a new object of this type, invoking the specified
		 * constructor. The constructor method ID must be a member of
		 * the class type.
		 * <p/>
		 * Instance creation will occur in the specified thread.
		 * Instance creation can occur only if the specified thread
		 * has been suspended by an event.
		 * Method invocation is not supported
		 * when the target VM has been suspended by the front-end.
		 * <p/>
		 * The specified constructor is invoked with the arguments in the specified
		 * argument list.
		 * The constructor invocation is synchronous; the reply packet is not
		 * sent until the invoked method returns in the target VM.
		 * The return value (possibly the void value) is
		 * included in the reply packet.
		 * If the constructor throws an exception, the
		 * exception object ID is set in the reply packet; otherwise, the
		 * exception object ID is null.
		 * <p/>
		 * For primitive arguments, the argument value's type must match the
		 * argument's type exactly. For object arguments, there must exist a
		 * widening reference conversion from the argument value's type to the
		 * argument's type and the argument's type must be loaded.
		 * <p/>
		 * By default, all threads in the target VM are resumed while
		 * the method is being invoked if they were previously
		 * suspended by an event or by command.
		 * This is done to prevent the deadlocks
		 * that will occur if any of the threads own monitors
		 * that will be needed by the invoked method. It is possible that
		 * breakpoints or other events might occur during the invocation.
		 * Note, however, that this implicit resume acts exactly like
		 * the ThreadReference resume command, so if the thread's suspend
		 * count is greater than 1, it will remain in a suspended state
		 * during the invocation. By default, when the invocation completes,
		 * all threads in the target VM are suspended, regardless their state
		 * before the invocation.
		 * <p/>
		 * The resumption of other threads during the invoke can be prevented
		 * by specifying the INVOKE_SINGLE_THREADED
		 * bit flag in the <code>options</code> field; however,
		 * there is no protection against or recovery from the deadlocks
		 * described above, so this option should be used with great caution.
		 * Only the specified thread will be resumed (as described for all
		 * threads above). Upon completion of a single threaded invoke, the invoking thread
		 * will be suspended once again. Note that any threads started during
		 * the single threaded invocation will not be suspended when the
		 * invocation completes.
		 * <p/>
		 * If the target VM is disconnected during the invoke (for example, through
		 * the VirtualMachine dispose command) the method invocation continues.
		 */
		static class NewInstance
		{
			static final int COMMAND = 4;

			static NewInstance process(
					VirtualMachineImpl vm,
					ClassTypeImpl clazz,
					ThreadReferenceImpl thread,
					long methodID,
					ValueImpl[] arguments,
					int options) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, clazz, thread, methodID, arguments, options);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ClassTypeImpl clazz, ThreadReferenceImpl thread, long methodID, ValueImpl[] arguments, int options)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.ClassType.NewInstance" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 clazz(ClassTypeImpl): " + (clazz == null ? "NULL" : "ref=" + clazz.ref()));
				}
				ps.writeClassRef(clazz.ref());
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 thread(ThreadReferenceImpl): " + (thread == null ? "NULL" : "ref=" + thread.ref()));
				}
				ps.writeObjectRef(thread.ref());
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 methodID(long): " + methodID);
				}
				ps.writeMethodRef(methodID);
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 arguments(ValueImpl[]): " + "");
				}
				ps.writeInt(arguments.length);
				for(int i = 0; i < arguments.length; i++)
				{
					if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
					{
						ps.vm.printTrace("Sending:                     arguments[i](ValueImpl): " + arguments[i]);
					}
					ps.writeValue(arguments[i]);
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 options(int): " + options);
				}
				ps.writeInt(options);
				ps.send();
				return ps;
			}

			static NewInstance waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new NewInstance(vm, ps);
			}


			/**
			 * The newly created object, or null
			 * if the constructor threw an exception.
			 */
			final ObjectReferenceImpl newObject;

			/**
			 * The thrown exception, if any; otherwise, null.
			 */
			final ObjectReferenceImpl exception;

			private NewInstance(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.ClassType.NewInstance" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				newObject = ps.readTaggedObjectReference();
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "newObject(ObjectReferenceImpl): " + (newObject == null ? "NULL" : "ref=" + newObject.ref()));
				}
				exception = ps.readTaggedObjectReference();
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "exception(ObjectReferenceImpl): " + (exception == null ? "NULL" : "ref=" + exception.ref()));
				}
			}
		}
	}

	static class ArrayType
	{
		static final int COMMAND_SET = 4;

		private ArrayType()
		{
		}  // hide constructor

		/**
		 * Creates a new array object of this type with a given length.
		 */
		static class NewInstance
		{
			static final int COMMAND = 1;

			static NewInstance process(
					VirtualMachineImpl vm, ArrayTypeImpl arrType, int length) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, arrType, length);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ArrayTypeImpl arrType, int length)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.ArrayType.NewInstance" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 arrType(ArrayTypeImpl): " + (arrType == null ? "NULL" : "ref=" + arrType.ref()));
				}
				ps.writeClassRef(arrType.ref());
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 length(int): " + length);
				}
				ps.writeInt(length);
				ps.send();
				return ps;
			}

			static NewInstance waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new NewInstance(vm, ps);
			}


			/**
			 * The newly created array object.
			 */
			final ObjectReferenceImpl newArray;

			private NewInstance(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.ArrayType.NewInstance" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				newArray = ps.readTaggedObjectReference();
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "newArray(ObjectReferenceImpl): " + (newArray == null ? "NULL" : "ref=" + newArray.ref()));
				}
			}
		}
	}

	static class Method
	{
		static final int COMMAND_SET = 6;

		private Method()
		{
		}  // hide constructor

		/**
		 * Returns line number information for the method, if present.
		 * The line table maps source line numbers to the initial code index
		 * of the line. The line table
		 * is ordered by code index (from lowest to highest). The line number
		 * information is constant unless a new class definition is installed
		 * using <a href="#JDWP_VirtualMachine_RedefineClasses">RedefineClasses</a>.
		 */
		static class LineTable
		{
			static final int COMMAND = 1;

			static LineTable process(
					VirtualMachineImpl vm, ReferenceTypeImpl refType, long methodID) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, refType, methodID);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ReferenceTypeImpl refType, long methodID)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.Method.LineTable" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 refType(ReferenceTypeImpl): " + (refType == null ? "NULL" : "ref=" + refType.ref()));
				}
				ps.writeClassRef(refType.ref());
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 methodID(long): " + methodID);
				}
				ps.writeMethodRef(methodID);
				ps.send();
				return ps;
			}

			static LineTable waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new LineTable(vm, ps);
			}

			static class LineInfo
			{

				/**
				 * Initial code index of the line,
				 * start <= lineCodeIndex < end
				 */
				final long lineCodeIndex;

				/**
				 * Line number.
				 */
				final int lineNumber;

				private LineInfo(VirtualMachineImpl vm, PacketStream ps)
				{
					lineCodeIndex = ps.readLong();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "lineCodeIndex(long): " + lineCodeIndex);
					}
					lineNumber = ps.readInt();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "lineNumber(int): " + lineNumber);
					}
				}
			}


			/**
			 * Lowest valid code index for the method, >=0, or -1 if the method is native
			 */
			final long start;

			/**
			 * Highest valid code index for the method, >=0, or -1 if the method is native
			 */
			final long end;

			/**
			 * The number of entries in the line table for this method.
			 */
			final LineInfo[] lines;

			private LineTable(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.Method.LineTable" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				start = ps.readLong();
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "start(long): " + start);
				}
				end = ps.readLong();
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "end(long): " + end);
				}
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "lines(LineInfo[]): " + "");
				}
				int linesCount = ps.readInt();
				lines = new LineInfo[linesCount];
				for(int i = 0; i < linesCount; i++)
				{
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "lines[i](LineInfo): " + "");
					}
					lines[i] = new LineInfo(vm, ps);
				}
			}
		}

		/**
		 * Returns variable information for the method. The variable table
		 * includes arguments and locals declared within the method. For
		 * instance methods, the "this" reference is included in the
		 * table. Also, synthetic variables may be present.
		 */
		static class VariableTable
		{
			static final int COMMAND = 2;

			static VariableTable process(
					VirtualMachineImpl vm, ReferenceTypeImpl refType, long methodID) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, refType, methodID);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ReferenceTypeImpl refType, long methodID)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.Method.VariableTable" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 refType(ReferenceTypeImpl): " + (refType == null ? "NULL" : "ref=" + refType.ref()));
				}
				ps.writeClassRef(refType.ref());
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 methodID(long): " + methodID);
				}
				ps.writeMethodRef(methodID);
				ps.send();
				return ps;
			}

			static VariableTable waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new VariableTable(vm, ps);
			}

			/**
			 * Information about the variable.
			 */
			static class SlotInfo
			{

				/**
				 * First code index at which the variable is visible (unsigned).
				 * Used in conjunction with <code>length</code>.
				 * The variable can be get or set only when the current
				 * <code>codeIndex</code> <= current frame code index < <code>codeIndex + length</code>
				 */
				final long codeIndex;

				/**
				 * The variable's name.
				 */
				final String name;

				/**
				 * The variable type's JNI signature.
				 */
				final String signature;

				/**
				 * Unsigned value used in conjunction with <code>codeIndex</code>.
				 * The variable can be get or set only when the current
				 * <code>codeIndex</code> <= current frame code index < <code>code index + length</code>
				 */
				final int length;

				/**
				 * The local variable's index in its frame
				 */
				final int slot;

				private SlotInfo(VirtualMachineImpl vm, PacketStream ps)
				{
					codeIndex = ps.readLong();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "codeIndex(long): " + codeIndex);
					}
					name = ps.readString();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "name(String): " + name);
					}
					signature = ps.readString();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "signature(String): " + signature);
					}
					length = ps.readInt();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "length(int): " + length);
					}
					slot = ps.readInt();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "slot(int): " + slot);
					}
				}
			}


			/**
			 * The number of words in the frame used by arguments.
			 * Eight-byte arguments use two words; all others use one.
			 */
			final int argCnt;

			/**
			 * The number of variables.
			 */
			final SlotInfo[] slots;

			private VariableTable(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.Method.VariableTable" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				argCnt = ps.readInt();
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "argCnt(int): " + argCnt);
				}
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "slots(SlotInfo[]): " + "");
				}
				int slotsCount = ps.readInt();
				slots = new SlotInfo[slotsCount];
				for(int i = 0; i < slotsCount; i++)
				{
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "slots[i](SlotInfo): " + "");
					}
					slots[i] = new SlotInfo(vm, ps);
				}
			}
		}

		/**
		 * Retrieve the method's bytecodes as defined in
		 * <cite>The Java&trade; Virtual Machine Specification</cite>.
		 * Requires canGetBytecodes capability - see
		 * <a href="#JDWP_VirtualMachine_CapabilitiesNew">CapabilitiesNew</a>.
		 */
		static class Bytecodes
		{
			static final int COMMAND = 3;

			static Bytecodes process(
					VirtualMachineImpl vm, ReferenceTypeImpl refType, long methodID) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, refType, methodID);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ReferenceTypeImpl refType, long methodID)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.Method.Bytecodes" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 refType(ReferenceTypeImpl): " + (refType == null ? "NULL" : "ref=" + refType.ref()));
				}
				ps.writeClassRef(refType.ref());
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 methodID(long): " + methodID);
				}
				ps.writeMethodRef(methodID);
				ps.send();
				return ps;
			}

			static Bytecodes waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new Bytecodes(vm, ps);
			}


			final byte[] bytes;

			private Bytecodes(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.Method.Bytecodes" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "bytes(byte[]): " + "");
				}
				int bytesCount = ps.readInt();
				bytes = new byte[bytesCount];
				for(int i = 0; i < bytesCount; i++)
				{
					bytes[i] = ps.readByte();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "bytes[i](byte): " + bytes[i]);
					}
				}
			}
		}

		/**
		 * Determine if this method is obsolete. A method is obsolete if it has been replaced
		 * by a non-equivalent method using the
		 * <a href="#JDWP_VirtualMachine_RedefineClasses">RedefineClasses</a> command.
		 * The original and redefined methods are considered equivalent if their bytecodes are
		 * the same except for indices into the constant pool and the referenced constants are
		 * equal.
		 */
		static class IsObsolete
		{
			static final int COMMAND = 4;

			static IsObsolete process(
					VirtualMachineImpl vm, ReferenceTypeImpl refType, long methodID) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, refType, methodID);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ReferenceTypeImpl refType, long methodID)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.Method.IsObsolete" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 refType(ReferenceTypeImpl): " + (refType == null ? "NULL" : "ref=" + refType.ref()));
				}
				ps.writeClassRef(refType.ref());
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 methodID(long): " + methodID);
				}
				ps.writeMethodRef(methodID);
				ps.send();
				return ps;
			}

			static IsObsolete waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new IsObsolete(vm, ps);
			}


			/**
			 * true if this method has been replaced
			 * by a non-equivalent method using
			 * the RedefineClasses command.
			 */
			final boolean isObsolete;

			private IsObsolete(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.Method.IsObsolete" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				isObsolete = ps.readBoolean();
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "isObsolete(boolean): " + isObsolete);
				}
			}
		}

		/**
		 * Returns variable information for the method, including
		 * generic signatures for the variables. The variable table
		 * includes arguments and locals declared within the method. For
		 * instance methods, the "this" reference is included in the
		 * table. Also, synthetic variables may be present.
		 * Generic signatures are described in the signature attribute
		 * section in
		 * <cite>The Java&trade; Virtual Machine Specification</cite>.
		 * Since JDWP version 1.5.
		 */
		static class VariableTableWithGeneric
		{
			static final int COMMAND = 5;

			static VariableTableWithGeneric process(
					VirtualMachineImpl vm, ReferenceTypeImpl refType, long methodID) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, refType, methodID);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ReferenceTypeImpl refType, long methodID)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.Method.VariableTableWithGeneric" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 refType(ReferenceTypeImpl): " + (refType == null ? "NULL" : "ref=" + refType.ref()));
				}
				ps.writeClassRef(refType.ref());
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 methodID(long): " + methodID);
				}
				ps.writeMethodRef(methodID);
				ps.send();
				return ps;
			}

			static VariableTableWithGeneric waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new VariableTableWithGeneric(vm, ps);
			}

			/**
			 * Information about the variable.
			 */
			static class SlotInfo
			{

				/**
				 * First code index at which the variable is visible (unsigned).
				 * Used in conjunction with <code>length</code>.
				 * The variable can be get or set only when the current
				 * <code>codeIndex</code> <= current frame code index < <code>codeIndex + length</code>
				 */
				final long codeIndex;

				/**
				 * The variable's name.
				 */
				final String name;

				/**
				 * The variable type's JNI signature.
				 */
				final String signature;

				/**
				 * The variable type's generic
				 * signature or an empty string if there is none.
				 */
				final String genericSignature;

				/**
				 * Unsigned value used in conjunction with <code>codeIndex</code>.
				 * The variable can be get or set only when the current
				 * <code>codeIndex</code> <= current frame code index < <code>code index + length</code>
				 */
				final int length;

				/**
				 * The local variable's index in its frame
				 */
				final int slot;

				private SlotInfo(VirtualMachineImpl vm, PacketStream ps)
				{
					codeIndex = ps.readLong();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "codeIndex(long): " + codeIndex);
					}
					name = ps.readString();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "name(String): " + name);
					}
					signature = ps.readString();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "signature(String): " + signature);
					}
					genericSignature = ps.readString();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "genericSignature(String): " + genericSignature);
					}
					length = ps.readInt();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "length(int): " + length);
					}
					slot = ps.readInt();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "slot(int): " + slot);
					}
				}
			}


			/**
			 * The number of words in the frame used by arguments.
			 * Eight-byte arguments use two words; all others use one.
			 */
			final int argCnt;

			/**
			 * The number of variables.
			 */
			final SlotInfo[] slots;

			private VariableTableWithGeneric(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.Method.VariableTableWithGeneric" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				argCnt = ps.readInt();
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "argCnt(int): " + argCnt);
				}
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "slots(SlotInfo[]): " + "");
				}
				int slotsCount = ps.readInt();
				slots = new SlotInfo[slotsCount];
				for(int i = 0; i < slotsCount; i++)
				{
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "slots[i](SlotInfo): " + "");
					}
					slots[i] = new SlotInfo(vm, ps);
				}
			}
		}
	}

	static class Field
	{
		static final int COMMAND_SET = 8;

		private Field()
		{
		}  // hide constructor
	}

	static class ObjectReference
	{
		static final int COMMAND_SET = 9;

		private ObjectReference()
		{
		}  // hide constructor

		/**
		 * Returns the runtime type of the object.
		 * The runtime type will be a class or an array.
		 */
		static class ReferenceType
		{
			static final int COMMAND = 1;

			static ReferenceType process(
					VirtualMachineImpl vm, ObjectReferenceImpl object) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, object);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ObjectReferenceImpl object)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.ObjectReference.ReferenceType" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 object(ObjectReferenceImpl): " + (object == null ? "NULL" : "ref=" + object.ref()));
				}
				ps.writeObjectRef(object.ref());
				ps.send();
				return ps;
			}

			static ReferenceType waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new ReferenceType(vm, ps);
			}


			/**
			 * <a href="#JDWP_TypeTag">Kind</a>
			 * of following reference type.
			 */
			final byte refTypeTag;

			/**
			 * The runtime reference type.
			 */
			final long typeID;

			private ReferenceType(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.ObjectReference.ReferenceType" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				refTypeTag = ps.readByte();
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "refTypeTag(byte): " + refTypeTag);
				}
				typeID = ps.readClassRef();
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "typeID(long): " + "ref=" + typeID);
				}
			}
		}

		/**
		 * Returns the value of one or more instance fields.
		 * Each field must be member of the object's type
		 * or one of its superclasses, superinterfaces, or implemented interfaces.
		 * Access control is not enforced; for example, the values of private
		 * fields can be obtained.
		 */
		static class GetValues
		{
			static final int COMMAND = 2;

			static class Field
			{

				/**
				 * Field to get.
				 */
				final long fieldID;

				Field(long fieldID)
				{
					this.fieldID = fieldID;
				}

				private void write(PacketStream ps)
				{
					if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
					{
						ps.vm.printTrace("Sending:                     fieldID(long): " + fieldID);
					}
					ps.writeFieldRef(fieldID);
				}
			}

			static GetValues process(
					VirtualMachineImpl vm, ObjectReferenceImpl object, Field[] fields) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, object, fields);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ObjectReferenceImpl object, Field[] fields)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.ObjectReference.GetValues" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 object(ObjectReferenceImpl): " + (object == null ? "NULL" : "ref=" + object.ref()));
				}
				ps.writeObjectRef(object.ref());
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 fields(Field[]): " + "");
				}
				ps.writeInt(fields.length);
				for(int i = 0; i < fields.length; i++)
				{
					if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
					{
						ps.vm.printTrace("Sending:                     fields[i](Field): " + "");
					}
					fields[i].write(ps);
				}
				ps.send();
				return ps;
			}

			static GetValues waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new GetValues(vm, ps);
			}


			/**
			 * The number of values returned, always equal to 'fields',
			 * the number of values to get. Field values are ordered
			 * in the reply in the same order as corresponding fieldIDs
			 * in the command.
			 */
			final ValueImpl[] values;

			private GetValues(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.ObjectReference.GetValues" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "values(ValueImpl[]): " + "");
				}
				int valuesCount = ps.readInt();
				values = new ValueImpl[valuesCount];
				for(int i = 0; i < valuesCount; i++)
				{
					values[i] = ps.readValue();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "values[i](ValueImpl): " + values[i]);
					}
				}
			}
		}

		/**
		 * Sets the value of one or more instance fields.
		 * Each field must be member of the object's type
		 * or one of its superclasses, superinterfaces, or implemented interfaces.
		 * Access control is not enforced; for example, the values of private
		 * fields can be set.
		 * For primitive values, the value's type must match the
		 * field's type exactly. For object values, there must be a
		 * widening reference conversion from the value's type to the
		 * field's type and the field's type must be loaded.
		 */
		static class SetValues
		{
			static final int COMMAND = 3;

			/**
			 * A Field/Value pair.
			 */
			static class FieldValue
			{

				/**
				 * Field to set.
				 */
				final long fieldID;

				/**
				 * Value to put in the field.
				 */
				final ValueImpl value;

				FieldValue(long fieldID, ValueImpl value)
				{
					this.fieldID = fieldID;
					this.value = value;
				}

				private void write(PacketStream ps)
				{
					if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
					{
						ps.vm.printTrace("Sending:                     fieldID(long): " + fieldID);
					}
					ps.writeFieldRef(fieldID);
					if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
					{
						ps.vm.printTrace("Sending:                     value(ValueImpl): " + value);
					}
					ps.writeUntaggedValue(value);
				}
			}

			static SetValues process(
					VirtualMachineImpl vm, ObjectReferenceImpl object, FieldValue[] values) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, object, values);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ObjectReferenceImpl object, FieldValue[] values)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.ObjectReference.SetValues" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 object(ObjectReferenceImpl): " + (object == null ? "NULL" : "ref=" + object.ref()));
				}
				ps.writeObjectRef(object.ref());
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 values(FieldValue[]): " + "");
				}
				ps.writeInt(values.length);
				for(int i = 0; i < values.length; i++)
				{
					if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
					{
						ps.vm.printTrace("Sending:                     values[i](FieldValue): " + "");
					}
					values[i].write(ps);
				}
				ps.send();
				return ps;
			}

			static SetValues waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new SetValues(vm, ps);
			}


			private SetValues(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.ObjectReference.SetValues" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
			}
		}


		/**
		 * Invokes a instance method.
		 * The method must be member of the object's type
		 * or one of its superclasses, superinterfaces, or implemented interfaces.
		 * Access control is not enforced; for example, private
		 * methods can be invoked.
		 * <p/>
		 * The method invocation will occur in the specified thread.
		 * Method invocation can occur only if the specified thread
		 * has been suspended by an event.
		 * Method invocation is not supported
		 * when the target VM has been suspended by the front-end.
		 * <p/>
		 * The specified method is invoked with the arguments in the specified
		 * argument list.
		 * The method invocation is synchronous; the reply packet is not
		 * sent until the invoked method returns in the target VM.
		 * The return value (possibly the void value) is
		 * included in the reply packet.
		 * If the invoked method throws an exception, the
		 * exception object ID is set in the reply packet; otherwise, the
		 * exception object ID is null.
		 * <p/>
		 * For primitive arguments, the argument value's type must match the
		 * argument's type exactly. For object arguments, there must be a
		 * widening reference conversion from the argument value's type to the
		 * argument's type and the argument's type must be loaded.
		 * <p/>
		 * By default, all threads in the target VM are resumed while
		 * the method is being invoked if they were previously
		 * suspended by an event or by command.
		 * This is done to prevent the deadlocks
		 * that will occur if any of the threads own monitors
		 * that will be needed by the invoked method. It is possible that
		 * breakpoints or other events might occur during the invocation.
		 * Note, however, that this implicit resume acts exactly like
		 * the ThreadReference resume command, so if the thread's suspend
		 * count is greater than 1, it will remain in a suspended state
		 * during the invocation. By default, when the invocation completes,
		 * all threads in the target VM are suspended, regardless their state
		 * before the invocation.
		 * <p/>
		 * The resumption of other threads during the invoke can be prevented
		 * by specifying the INVOKE_SINGLE_THREADED
		 * bit flag in the <code>options</code> field; however,
		 * there is no protection against or recovery from the deadlocks
		 * described above, so this option should be used with great caution.
		 * Only the specified thread will be resumed (as described for all
		 * threads above). Upon completion of a single threaded invoke, the invoking thread
		 * will be suspended once again. Note that any threads started during
		 * the single threaded invocation will not be suspended when the
		 * invocation completes.
		 * <p/>
		 * If the target VM is disconnected during the invoke (for example, through
		 * the VirtualMachine dispose command) the method invocation continues.
		 */
		static class InvokeMethod
		{
			static final int COMMAND = 6;

			static InvokeMethod process(
					VirtualMachineImpl vm,
					ObjectReferenceImpl object,
					ThreadReferenceImpl thread,
					ClassTypeImpl clazz,
					long methodID,
					ValueImpl[] arguments,
					int options) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, object, thread, clazz, methodID, arguments, options);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm,
					ObjectReferenceImpl object,
					ThreadReferenceImpl thread,
					ClassTypeImpl clazz,
					long methodID,
					ValueImpl[] arguments,
					int options)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.ObjectReference.InvokeMethod" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 object(ObjectReferenceImpl): " + (object == null ? "NULL" : "ref=" + object.ref()));
				}
				ps.writeObjectRef(object.ref());
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 thread(ThreadReferenceImpl): " + (thread == null ? "NULL" : "ref=" + thread.ref()));
				}
				ps.writeObjectRef(thread.ref());
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 clazz(ClassTypeImpl): " + (clazz == null ? "NULL" : "ref=" + clazz.ref()));
				}
				ps.writeClassRef(clazz.ref());
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 methodID(long): " + methodID);
				}
				ps.writeMethodRef(methodID);
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 arguments(ValueImpl[]): " + "");
				}
				ps.writeInt(arguments.length);
				for(int i = 0; i < arguments.length; i++)
				{
					if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
					{
						ps.vm.printTrace("Sending:                     arguments[i](ValueImpl): " + arguments[i]);
					}
					ps.writeValue(arguments[i]);
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 options(int): " + options);
				}
				ps.writeInt(options);
				ps.send();
				return ps;
			}

			static InvokeMethod waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new InvokeMethod(vm, ps);
			}


			/**
			 * The returned value, or null if an exception is thrown.
			 */
			final ValueImpl returnValue;

			/**
			 * The thrown exception, if any.
			 */
			final ObjectReferenceImpl exception;

			private InvokeMethod(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.ObjectReference.InvokeMethod" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				returnValue = ps.readValue();
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "returnValue(ValueImpl): " + returnValue);
				}
				exception = ps.readTaggedObjectReference();
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "exception(ObjectReferenceImpl): " + (exception == null ? "NULL" : "ref=" + exception.ref()));
				}
			}
		}

		/**
		 * Prevents garbage collection for the given object. By
		 * default all objects in back-end replies may be
		 * collected at any time the target VM is running. A call to
		 * this command guarantees that the object will not be
		 * collected. The
		 * <a href="#JDWP_ObjectReference_EnableCollection">EnableCollection</a>
		 * command can be used to
		 * allow collection once again.
		 * <p/>
		 * Note that while the target VM is suspended, no garbage
		 * collection will occur because all threads are suspended.
		 * The typical examination of variables, fields, and arrays
		 * during the suspension is safe without explicitly disabling
		 * garbage collection.
		 * <p/>
		 * This method should be used sparingly, as it alters the
		 * pattern of garbage collection in the target VM and,
		 * consequently, may result in application behavior under the
		 * debugger that differs from its non-debugged behavior.
		 */
		static class DisableCollection
		{
			static final int COMMAND = 7;

			static DisableCollection process(
					VirtualMachineImpl vm, ObjectReferenceImpl object) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, object);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ObjectReferenceImpl object)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.ObjectReference.DisableCollection" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 object(ObjectReferenceImpl): " + (object == null ? "NULL" : "ref=" + object.ref()));
				}
				ps.writeObjectRef(object.ref());
				ps.send();
				return ps;
			}

			static DisableCollection waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new DisableCollection(vm, ps);
			}


			private DisableCollection(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.ObjectReference.DisableCollection" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
			}
		}

		/**
		 * Permits garbage collection for this object. By default all
		 * objects returned by JDWP may become unreachable in the target VM,
		 * and hence may be garbage collected. A call to this command is
		 * necessary only if garbage collection was previously disabled with
		 * the <a href="#JDWP_ObjectReference_DisableCollection">DisableCollection</a>
		 * command.
		 */
		static class EnableCollection
		{
			static final int COMMAND = 8;

			static EnableCollection process(
					VirtualMachineImpl vm, ObjectReferenceImpl object) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, object);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ObjectReferenceImpl object)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.ObjectReference.EnableCollection" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 object(ObjectReferenceImpl): " + (object == null ? "NULL" : "ref=" + object.ref()));
				}
				ps.writeObjectRef(object.ref());
				ps.send();
				return ps;
			}

			static EnableCollection waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new EnableCollection(vm, ps);
			}


			private EnableCollection(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.ObjectReference.EnableCollection" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
			}
		}

		/**
		 * Determines whether an object has been garbage collected in the
		 * target VM.
		 */
		static class IsCollected
		{
			static final int COMMAND = 9;

			static IsCollected process(
					VirtualMachineImpl vm, ObjectReferenceImpl object) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, object);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ObjectReferenceImpl object)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.ObjectReference.IsCollected" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 object(ObjectReferenceImpl): " + (object == null ? "NULL" : "ref=" + object.ref()));
				}
				ps.writeObjectRef(object.ref());
				ps.send();
				return ps;
			}

			static IsCollected waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new IsCollected(vm, ps);
			}


			/**
			 * true if the object has been collected; false otherwise
			 */
			final boolean isCollected;

			private IsCollected(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.ObjectReference.IsCollected" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				isCollected = ps.readBoolean();
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "isCollected(boolean): " + isCollected);
				}
			}
		}

		/**
		 * Returns objects that directly reference this object.
		 * Only objects that are reachable for the purposes
		 * of garbage collection are returned.
		 * Note that an object can also be referenced in other ways,
		 * such as from a local variable in a stack frame, or from a JNI global
		 * reference.  Such non-object referrers are not returned by this command.
		 * <p>Since JDWP version 1.6. Requires canGetInstanceInfo capability - see
		 * <a href="#JDWP_VirtualMachine_CapabilitiesNew">CapabilitiesNew</a>.
		 */
		static class ReferringObjects
		{
			static final int COMMAND = 10;

			static ReferringObjects process(
					VirtualMachineImpl vm, ObjectReferenceImpl object, int maxReferrers) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, object, maxReferrers);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ObjectReferenceImpl object, int maxReferrers)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.ObjectReference.ReferringObjects" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 object(ObjectReferenceImpl): " + (object == null ? "NULL" : "ref=" + object.ref()));
				}
				ps.writeObjectRef(object.ref());
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 maxReferrers(int): " + maxReferrers);
				}
				ps.writeInt(maxReferrers);
				ps.send();
				return ps;
			}

			static ReferringObjects waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new ReferringObjects(vm, ps);
			}


			/**
			 * The number of objects that follow.
			 */
			final ObjectReferenceImpl[] referringObjects;

			private ReferringObjects(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.ObjectReference.ReferringObjects" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "referringObjects(ObjectReferenceImpl[]): " + "");
				}
				int referringObjectsCount = ps.readInt();
				referringObjects = new ObjectReferenceImpl[referringObjectsCount];
				for(int i = 0; i < referringObjectsCount; i++)
				{
					referringObjects[i] = ps.readTaggedObjectReference();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "referringObjects[i](ObjectReferenceImpl): " + (referringObjects[i] == null ? "NULL" : "ref=" +
								referringObjects[i].ref()));
					}
				}
			}
		}
	}

	static class StringReference
	{
		static final int COMMAND_SET = 10;

		private StringReference()
		{
		}  // hide constructor

		/**
		 * Returns the characters contained in the string.
		 */
		static class Value
		{
			static final int COMMAND = 1;

			static Value process(
					VirtualMachineImpl vm, ObjectReferenceImpl stringObject) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, stringObject);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ObjectReferenceImpl stringObject)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.StringReference.Value" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 stringObject(ObjectReferenceImpl): " + (stringObject == null ? "NULL" : "ref=" +
							stringObject.ref()));
				}
				ps.writeObjectRef(stringObject.ref());
				ps.send();
				return ps;
			}

			static Value waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new Value(vm, ps);
			}


			/**
			 * UTF-8 representation of the string value.
			 */
			final String stringValue;

			private Value(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.StringReference.Value" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				stringValue = ps.readString();
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "stringValue(String): " + stringValue);
				}
			}
		}
	}

	static class ThreadReference
	{
		static final int COMMAND_SET = 11;

		private ThreadReference()
		{
		}  // hide constructor



		/**
		 * Returns the current call stack of a suspended thread.
		 * The sequence of frames starts with
		 * the currently executing frame, followed by its caller,
		 * and so on. The thread must be suspended, and the returned
		 * frameID is valid only while the thread is suspended.
		 */
		static class Frames
		{
			static final int COMMAND = 6;

			static Frames process(
					VirtualMachineImpl vm, ThreadReferenceImpl thread, int startFrame, int length) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, thread, startFrame, length);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ThreadReferenceImpl thread, int startFrame, int length)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.ThreadReference.Frames" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 thread(ThreadReferenceImpl): " + (thread == null ? "NULL" : "ref=" + thread.ref()));
				}
				ps.writeObjectRef(thread.ref());
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 startFrame(int): " + startFrame);
				}
				ps.writeInt(startFrame);
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 length(int): " + length);
				}
				ps.writeInt(length);
				ps.send();
				return ps;
			}

			static Frames waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new Frames(vm, ps);
			}

			static class Frame
			{

				/**
				 * The ID of this frame.
				 */
				final long frameID;

				/**
				 * The current location of this frame
				 */
				final Location location;

				private Frame(VirtualMachineImpl vm, PacketStream ps)
				{
					frameID = ps.readFrameRef();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "frameID(long): " + frameID);
					}
					location = ps.readLocation();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "location(Location): " + location);
					}
				}
			}


			/**
			 * The number of frames retreived
			 */
			final Frame[] frames;

			private Frames(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.ThreadReference.Frames" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "frames(Frame[]): " + "");
				}
				int framesCount = ps.readInt();
				frames = new Frame[framesCount];
				for(int i = 0; i < framesCount; i++)
				{
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "frames[i](Frame): " + "");
					}
					frames[i] = new Frame(vm, ps);
				}
			}
		}

		/**
		 * Returns the count of frames on this thread's stack.
		 * The thread must be suspended, and the returned
		 * count is valid only while the thread is suspended.
		 * Returns JDWP.Error.errorThreadNotSuspended if not suspended.
		 */
		static class FrameCount
		{
			static final int COMMAND = 7;

			static FrameCount process(
					VirtualMachineImpl vm, ThreadReferenceImpl thread) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, thread);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ThreadReferenceImpl thread)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.ThreadReference.FrameCount" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 thread(ThreadReferenceImpl): " + (thread == null ? "NULL" : "ref=" + thread.ref()));
				}
				ps.writeObjectRef(thread.ref());
				ps.send();
				return ps;
			}

			static FrameCount waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new FrameCount(vm, ps);
			}


			/**
			 * The count of frames on this thread's stack.
			 */
			final int frameCount;

			private FrameCount(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.ThreadReference.FrameCount" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				frameCount = ps.readInt();
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "frameCount(int): " + frameCount);
				}
			}
		}
	}

	static class ArrayReference
	{
		static final int COMMAND_SET = 13;

		private ArrayReference()
		{
		}  // hide constructor

		/**
		 * Returns the number of components in a given array.
		 */
		static class Length
		{
			static final int COMMAND = 1;

			static Length process(
					VirtualMachineImpl vm, ArrayReferenceImpl arrayObject) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, arrayObject);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ArrayReferenceImpl arrayObject)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.ArrayReference.Length" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 arrayObject(ArrayReferenceImpl): " + (arrayObject == null ? "NULL" : "ref=" +
							arrayObject.ref()));
				}
				ps.writeObjectRef(arrayObject.ref());
				ps.send();
				return ps;
			}

			static Length waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new Length(vm, ps);
			}


			/**
			 * The length of the array.
			 */
			final int arrayLength;

			private Length(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.ArrayReference.Length" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				arrayLength = ps.readInt();
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "arrayLength(int): " + arrayLength);
				}
			}
		}

		/**
		 * Returns a range of array components. The specified range must
		 * be within the bounds of the array.
		 */
		static class GetValues
		{
			static final int COMMAND = 2;

			static GetValues process(
					VirtualMachineImpl vm, ArrayReferenceImpl arrayObject, int firstIndex, int length) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, arrayObject, firstIndex, length);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ArrayReferenceImpl arrayObject, int firstIndex, int length)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.ArrayReference.GetValues" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 arrayObject(ArrayReferenceImpl): " + (arrayObject == null ? "NULL" : "ref=" +
							arrayObject.ref()));
				}
				ps.writeObjectRef(arrayObject.ref());
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 firstIndex(int): " + firstIndex);
				}
				ps.writeInt(firstIndex);
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 length(int): " + length);
				}
				ps.writeInt(length);
				ps.send();
				return ps;
			}

			static GetValues waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new GetValues(vm, ps);
			}


			/**
			 * The retrieved values. If the values
			 * are objects, they are tagged-values;
			 * otherwise, they are untagged-values
			 */
			final List values;

			private GetValues(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.ArrayReference.GetValues" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				values = ps.readArrayRegion();
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "values(List): " + values);
				}
			}
		}

		/**
		 * Sets a range of array components. The specified range must
		 * be within the bounds of the array.
		 * For primitive values, each value's type must match the
		 * array component type exactly. For object values, there must be a
		 * widening reference conversion from the value's type to the
		 * array component type and the array component type must be loaded.
		 */
		static class SetValues
		{
			static final int COMMAND = 3;

			static SetValues process(
					VirtualMachineImpl vm, ArrayReferenceImpl arrayObject, int firstIndex, ValueImpl[] values) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, arrayObject, firstIndex, values);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ArrayReferenceImpl arrayObject, int firstIndex, ValueImpl[] values)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.ArrayReference.SetValues" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 arrayObject(ArrayReferenceImpl): " + (arrayObject == null ? "NULL" : "ref=" +
							arrayObject.ref()));
				}
				ps.writeObjectRef(arrayObject.ref());
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 firstIndex(int): " + firstIndex);
				}
				ps.writeInt(firstIndex);
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 values(ValueImpl[]): " + "");
				}
				ps.writeInt(values.length);
				for(int i = 0; i < values.length; i++)
				{
					if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
					{
						ps.vm.printTrace("Sending:                     values[i](ValueImpl): " + values[i]);
					}
					ps.writeUntaggedValue(values[i]);
				}
				ps.send();
				return ps;
			}

			static SetValues waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new SetValues(vm, ps);
			}


			private SetValues(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.ArrayReference.SetValues" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
			}
		}
	}

	static class EventRequest
	{
		static final int COMMAND_SET = 15;

		private EventRequest()
		{
		}  // hide constructor

		/**
		 * Set an event request. When the event described by this request
		 * occurs, an <a href="#JDWP_Event">event</a> is sent from the
		 * target VM. If an event occurs that has not been requested then it is not sent
		 * from the target VM. The two exceptions to this are the VM Start Event and
		 * the VM Death Event which are automatically generated events - see
		 * <a href="#JDWP_Event_Composite">Composite Command</a> for further details.
		 */
		static class Set
		{
			static final int COMMAND = 1;

			static class Modifier
			{
				abstract static class ModifierCommon
				{
					abstract void write(PacketStream ps);
				}

				/**
				 * Modifier kind
				 */
				final byte modKind;
				ModifierCommon aModifierCommon;

				Modifier(byte modKind, ModifierCommon aModifierCommon)
				{
					this.modKind = modKind;
					this.aModifierCommon = aModifierCommon;
				}

				private void write(PacketStream ps)
				{
					if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
					{
						ps.vm.printTrace("Sending:                     modKind(byte): " + modKind);
					}
					ps.writeByte(modKind);
					aModifierCommon.write(ps);
				}

				/**
				 * Limit the requested event to be reported at most once after a
				 * given number of occurrences.  The event is not reported
				 * the first <code>count - 1</code> times this filter is reached.
				 * To request a one-off event, call this method with a count of 1.
				 * <p/>
				 * Once the count reaches 0, any subsequent filters in this request
				 * are applied. If none of those filters cause the event to be
				 * suppressed, the event is reported. Otherwise, the event is not
				 * reported. In either case subsequent events are never reported for
				 * this request.
				 * This modifier can be used with any event kind.
				 */
				static class Count extends ModifierCommon
				{
					static final byte ALT_ID = 1;

					static Modifier create(int count)
					{
						return new Modifier(ALT_ID, new Count(count));
					}

					/**
					 * Count before event. One for one-off.
					 */
					final int count;

					Count(int count)
					{
						this.count = count;
					}

					@Override
					void write(PacketStream ps)
					{
						if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
						{
							ps.vm.printTrace("Sending:                         count(int): " + count);
						}
						ps.writeInt(count);
					}
				}

				/**
				 * Conditional on expression
				 */
				static class Conditional extends ModifierCommon
				{
					static final byte ALT_ID = 2;

					static Modifier create(int exprID)
					{
						return new Modifier(ALT_ID, new Conditional(exprID));
					}

					/**
					 * For the future
					 */
					final int exprID;

					Conditional(int exprID)
					{
						this.exprID = exprID;
					}

					@Override
					void write(PacketStream ps)
					{
						if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
						{
							ps.vm.printTrace("Sending:                         exprID(int): " + exprID);
						}
						ps.writeInt(exprID);
					}
				}

				/**
				 * Restricts reported events to
				 * those in the given thread.
				 * This modifier can be used with any event kind
				 * except for class unload.
				 */
				static class ThreadOnly extends ModifierCommon
				{
					static final byte ALT_ID = 3;

					static Modifier create(ThreadReferenceImpl thread)
					{
						return new Modifier(ALT_ID, new ThreadOnly(thread));
					}

					/**
					 * Required thread
					 */
					final ThreadReferenceImpl thread;

					ThreadOnly(ThreadReferenceImpl thread)
					{
						this.thread = thread;
					}

					@Override
					void write(PacketStream ps)
					{
						if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
						{
							ps.vm.printTrace("Sending:                         thread(ThreadReferenceImpl): " + (thread == null ? "NULL" : "ref=" +
									thread.ref()));
						}
						ps.writeObjectRef(thread.ref());
					}
				}

				/**
				 * For class prepare events, restricts the events
				 * generated by this request to be the
				 * preparation of the given reference type and any subtypes.
				 * For monitor wait and waited events, restricts the events
				 * generated by this request to those whose monitor object
				 * is of the given reference type or any of its subtypes.
				 * For other events, restricts the events generated
				 * by this request to those
				 * whose location is in the given reference type or any of its subtypes.
				 * An event will be generated for any location in a reference type that can
				 * be safely cast to the given reference type.
				 * This modifier can be used with any event kind except
				 * class unload, thread start, and thread end.
				 */
				static class ClassOnly extends ModifierCommon
				{
					static final byte ALT_ID = 4;

					static Modifier create(ReferenceTypeImpl clazz)
					{
						return new Modifier(ALT_ID, new ClassOnly(clazz));
					}

					/**
					 * Required class
					 */
					final ReferenceTypeImpl clazz;

					ClassOnly(ReferenceTypeImpl clazz)
					{
						this.clazz = clazz;
					}

					@Override
					void write(PacketStream ps)
					{
						if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
						{
							ps.vm.printTrace("Sending:                         clazz(ReferenceTypeImpl): " + (clazz == null ? "NULL" : "ref=" +
									clazz.ref()));
						}
						ps.writeClassRef(clazz.ref());
					}
				}

				/**
				 * Restricts reported events to those for classes whose name
				 * matches the given restricted regular expression.
				 * For class prepare events, the prepared class name
				 * is matched. For class unload events, the
				 * unloaded class name is matched. For monitor wait
				 * and waited events, the name of the class of the
				 * monitor object is matched. For other events,
				 * the class name of the event's location is matched.
				 * This modifier can be used with any event kind except
				 * thread start and thread end.
				 */
				static class ClassMatch extends ModifierCommon
				{
					static final byte ALT_ID = 5;

					static Modifier create(String classPattern)
					{
						return new Modifier(ALT_ID, new ClassMatch(classPattern));
					}

					/**
					 * Required class pattern.
					 * Matches are limited to exact matches of the
					 * given class pattern and matches of patterns that
					 * begin or end with '*'; for example,
					 * "*.Foo" or "java.*".
					 */
					final String classPattern;

					ClassMatch(String classPattern)
					{
						this.classPattern = classPattern;
					}

					@Override
					void write(PacketStream ps)
					{
						if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
						{
							ps.vm.printTrace("Sending:                         classPattern(String): " + classPattern);
						}
						ps.writeString(classPattern);
					}
				}

				/**
				 * Restricts reported events to those for classes whose name
				 * does not match the given restricted regular expression.
				 * For class prepare events, the prepared class name
				 * is matched. For class unload events, the
				 * unloaded class name is matched. For monitor wait and
				 * waited events, the name of the class of the monitor
				 * object is matched. For other events,
				 * the class name of the event's location is matched.
				 * This modifier can be used with any event kind except
				 * thread start and thread end.
				 */
				static class ClassExclude extends ModifierCommon
				{
					static final byte ALT_ID = 6;

					static Modifier create(String classPattern)
					{
						return new Modifier(ALT_ID, new ClassExclude(classPattern));
					}

					/**
					 * Disallowed class pattern.
					 * Matches are limited to exact matches of the
					 * given class pattern and matches of patterns that
					 * begin or end with '*'; for example,
					 * "*.Foo" or "java.*".
					 */
					final String classPattern;

					ClassExclude(String classPattern)
					{
						this.classPattern = classPattern;
					}

					@Override
					void write(PacketStream ps)
					{
						if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
						{
							ps.vm.printTrace("Sending:                         classPattern(String): " + classPattern);
						}
						ps.writeString(classPattern);
					}
				}

				/**
				 * Restricts reported events to those that occur at
				 * the given location.
				 * This modifier can be used with
				 * breakpoint, field access, field modification,
				 * step, and exception event kinds.
				 */
				static class LocationOnly extends ModifierCommon
				{
					static final byte ALT_ID = 7;

					static Modifier create(Location loc)
					{
						return new Modifier(ALT_ID, new LocationOnly(loc));
					}

					/**
					 * Required location
					 */
					final Location loc;

					LocationOnly(Location loc)
					{
						this.loc = loc;
					}

					@Override
					void write(PacketStream ps)
					{
						if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
						{
							ps.vm.printTrace("Sending:                         loc(Location): " + loc);
						}
						ps.writeLocation(loc);
					}
				}

				/**
				 * Restricts reported exceptions by their class and
				 * whether they are caught or uncaught.
				 * This modifier can be used with
				 * exception event kinds only.
				 */
				static class ExceptionOnly extends ModifierCommon
				{
					static final byte ALT_ID = 8;

					static Modifier create(ReferenceTypeImpl exceptionOrNull, boolean caught, boolean uncaught)
					{
						return new Modifier(ALT_ID, new ExceptionOnly(exceptionOrNull, caught, uncaught));
					}

					/**
					 * Exception to report. Null (0) means report
					 * exceptions of all types.
					 * A non-null type restricts the reported exception
					 * events to exceptions of the given type or
					 * any of its subtypes.
					 */
					final ReferenceTypeImpl exceptionOrNull;

					/**
					 * Report caught exceptions
					 */
					final boolean caught;

					/**
					 * Report uncaught exceptions.
					 * Note that it
					 * is not always possible to determine whether an
					 * exception is caught or uncaught at the time it is
					 * thrown. See the exception event catch location under
					 * <a href="#JDWP_Event_Composite">composite events</a>
					 * for more information.
					 */
					final boolean uncaught;

					ExceptionOnly(ReferenceTypeImpl exceptionOrNull, boolean caught, boolean uncaught)
					{
						this.exceptionOrNull = exceptionOrNull;
						this.caught = caught;
						this.uncaught = uncaught;
					}

					@Override
					void write(PacketStream ps)
					{
						if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
						{
							ps.vm.printTrace("Sending:                         exceptionOrNull(ReferenceTypeImpl): " + (exceptionOrNull == null ?
									"NULL" : "ref=" + exceptionOrNull.ref()));
						}
						ps.writeClassRef(exceptionOrNull.ref());
						if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
						{
							ps.vm.printTrace("Sending:                         caught(boolean): " + caught);
						}
						ps.writeBoolean(caught);
						if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
						{
							ps.vm.printTrace("Sending:                         uncaught(boolean): " + uncaught);
						}
						ps.writeBoolean(uncaught);
					}
				}

				/**
				 * Restricts reported events to those that occur for
				 * a given field.
				 * This modifier can be used with
				 * field access and field modification event kinds only.
				 */
				static class FieldOnly extends ModifierCommon
				{
					static final byte ALT_ID = 9;

					static Modifier create(ReferenceTypeImpl declaring, long fieldID)
					{
						return new Modifier(ALT_ID, new FieldOnly(declaring, fieldID));
					}

					/**
					 * Type in which field is declared.
					 */
					final ReferenceTypeImpl declaring;

					/**
					 * Required field
					 */
					final long fieldID;

					FieldOnly(ReferenceTypeImpl declaring, long fieldID)
					{
						this.declaring = declaring;
						this.fieldID = fieldID;
					}

					@Override
					void write(PacketStream ps)
					{
						if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
						{
							ps.vm.printTrace("Sending:                         declaring(ReferenceTypeImpl): " + (declaring == null ? "NULL" :
									"ref=" + declaring.ref()));
						}
						ps.writeClassRef(declaring.ref());
						if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
						{
							ps.vm.printTrace("Sending:                         fieldID(long): " + fieldID);
						}
						ps.writeFieldRef(fieldID);
					}
				}

				/**
				 * Restricts reported step events
				 * to those which satisfy
				 * depth and size constraints.
				 * This modifier can be used with
				 * step event kinds only.
				 */
				static class Step extends ModifierCommon
				{
					static final byte ALT_ID = 10;

					static Modifier create(ThreadReferenceImpl thread, int size, int depth)
					{
						return new Modifier(ALT_ID, new Step(thread, size, depth));
					}

					/**
					 * Thread in which to step
					 */
					final ThreadReferenceImpl thread;

					/**
					 * size of each step.
					 * See <a href="#JDWP_StepSize">JDWP.StepSize</a>
					 */
					final int size;

					/**
					 * relative call stack limit.
					 * See <a href="#JDWP_StepDepth">JDWP.StepDepth</a>
					 */
					final int depth;

					Step(ThreadReferenceImpl thread, int size, int depth)
					{
						this.thread = thread;
						this.size = size;
						this.depth = depth;
					}

					@Override
					void write(PacketStream ps)
					{
						if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
						{
							ps.vm.printTrace("Sending:                         thread(ThreadReferenceImpl): " + (thread == null ? "NULL" : "ref=" +
									thread.ref()));
						}
						ps.writeObjectRef(thread.ref());
						if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
						{
							ps.vm.printTrace("Sending:                         size(int): " + size);
						}
						ps.writeInt(size);
						if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
						{
							ps.vm.printTrace("Sending:                         depth(int): " + depth);
						}
						ps.writeInt(depth);
					}
				}

				/**
				 * Restricts reported events to those whose
				 * active 'this' object is the given object.
				 * Match value is the null object for static methods.
				 * This modifier can be used with any event kind
				 * except class prepare, class unload, thread start,
				 * and thread end. Introduced in JDWP version 1.4.
				 */
				static class InstanceOnly extends ModifierCommon
				{
					static final byte ALT_ID = 11;

					static Modifier create(ObjectReferenceImpl instance)
					{
						return new Modifier(ALT_ID, new InstanceOnly(instance));
					}

					/**
					 * Required 'this' object
					 */
					final ObjectReferenceImpl instance;

					InstanceOnly(ObjectReferenceImpl instance)
					{
						this.instance = instance;
					}

					@Override
					void write(PacketStream ps)
					{
						if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
						{
							ps.vm.printTrace("Sending:                         instance(ObjectReferenceImpl): " + (instance == null ? "NULL" :
									"ref=" + instance.ref()));
						}
						ps.writeObjectRef(instance.ref());
					}
				}

				/**
				 * Restricts reported class prepare events to those
				 * for reference types which have a source name
				 * which matches the given restricted regular expression.
				 * The source names are determined by the reference type's
				 * <a href="#JDWP_ReferenceType_SourceDebugExtension">
				 * SourceDebugExtension</a>.
				 * This modifier can only be used with class prepare
				 * events.
				 * Since JDWP version 1.6. Requires the canUseSourceNameFilters
				 * capability - see
				 * <a href="#JDWP_VirtualMachine_CapabilitiesNew">CapabilitiesNew</a>.
				 */
				static class SourceNameMatch extends ModifierCommon
				{
					static final byte ALT_ID = 12;

					static Modifier create(String sourceNamePattern)
					{
						return new Modifier(ALT_ID, new SourceNameMatch(sourceNamePattern));
					}

					/**
					 * Required source name pattern.
					 * Matches are limited to exact matches of the
					 * given pattern and matches of patterns that
					 * begin or end with '*'; for example,
					 * "*.Foo" or "java.*".
					 */
					final String sourceNamePattern;

					SourceNameMatch(String sourceNamePattern)
					{
						this.sourceNamePattern = sourceNamePattern;
					}

					@Override
					void write(PacketStream ps)
					{
						if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
						{
							ps.vm.printTrace("Sending:                         sourceNamePattern(String): " + sourceNamePattern);
						}
						ps.writeString(sourceNamePattern);
					}
				}
			}

			static Set process(
					VirtualMachineImpl vm, byte eventKind, byte suspendPolicy, Modifier[] modifiers) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, eventKind, suspendPolicy, modifiers);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, byte eventKind, byte suspendPolicy, Modifier[] modifiers)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.EventRequest.Set" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 eventKind(byte): " + eventKind);
				}
				ps.writeByte(eventKind);
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 suspendPolicy(byte): " + suspendPolicy);
				}
				ps.writeByte(suspendPolicy);
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 modifiers(Modifier[]): " + "");
				}
				ps.writeByte((byte) modifiers.length);
				for(int i = 0; i < modifiers.length; i++)
				{
					if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
					{
						ps.vm.printTrace("Sending:                     modifiers[i](Modifier): " + "");
					}
					modifiers[i].write(ps);
				}
				ps.send();
				return ps;
			}

			static Set waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new Set(vm, ps);
			}


			/**
			 * ID of created request
			 */
			final int requestID;

			private Set(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.EventRequest.Set" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				requestID = ps.readInt();
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "requestID(int): " + requestID);
				}
			}
		}

		/**
		 * Clear an event request. See <a href="#JDWP_EventKind">JDWP.EventKind</a>
		 * for a complete list of events that can be cleared. Only the event request matching
		 * the specified event kind and requestID is cleared. If there isn't a matching event
		 * request the command is a no-op and does not result in an error. Automatically
		 * generated events do not have a corresponding event request and may not be cleared
		 * using this command.
		 */
		static class Clear
		{
			static final int COMMAND = 2;

			static Clear process(
					VirtualMachineImpl vm, byte eventKind, int requestID) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, eventKind, requestID);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, byte eventKind, int requestID)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.EventRequest.Clear" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 eventKind(byte): " + eventKind);
				}
				ps.writeByte(eventKind);
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 requestID(int): " + requestID);
				}
				ps.writeInt(requestID);
				ps.send();
				return ps;
			}

			static Clear waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new Clear(vm, ps);
			}


			private Clear(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.EventRequest.Clear" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
			}
		}

		/**
		 * Removes all set breakpoints, a no-op if there are no breakpoints set.
		 */
		static class ClearAllBreakpoints
		{
			static final int COMMAND = 3;

			static ClearAllBreakpoints process(VirtualMachineImpl vm) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(VirtualMachineImpl vm)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.EventRequest.ClearAllBreakpoints" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				ps.send();
				return ps;
			}

			static ClearAllBreakpoints waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new ClearAllBreakpoints(vm, ps);
			}


			private ClearAllBreakpoints(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.EventRequest.ClearAllBreakpoints" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
			}
		}
	}

	static class StackFrame
	{
		static final int COMMAND_SET = 16;

		private StackFrame()
		{
		}  // hide constructor

		/**
		 * Returns the value of one or more local variables in a
		 * given frame. Each variable must be visible at the frame's code index.
		 * Even if local variable information is not available, values can
		 * be retrieved if the front-end is able to
		 * determine the correct local variable index. (Typically, this
		 * index can be determined for method arguments from the method
		 * signature without access to the local variable table information.)
		 */
		static class GetValues
		{
			static final int COMMAND = 1;

			static class SlotInfo
			{

				/**
				 * The local variable's index in the frame.
				 */
				final int slot;

				/**
				 * A <a href="#JDWP_Tag">tag</a>
				 * identifying the type of the variable
				 */
				final byte sigbyte;

				SlotInfo(int slot, byte sigbyte)
				{
					this.slot = slot;
					this.sigbyte = sigbyte;
				}

				private void write(PacketStream ps)
				{
					if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
					{
						ps.vm.printTrace("Sending:                     slot(int): " + slot);
					}
					ps.writeInt(slot);
					if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
					{
						ps.vm.printTrace("Sending:                     sigbyte(byte): " + sigbyte);
					}
					ps.writeByte(sigbyte);
				}
			}

			static GetValues process(
					VirtualMachineImpl vm, ThreadReferenceImpl thread, long frame, SlotInfo[] slots) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, thread, frame, slots);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ThreadReferenceImpl thread, long frame, SlotInfo[] slots)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.StackFrame.GetValues" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 thread(ThreadReferenceImpl): " + (thread == null ? "NULL" : "ref=" + thread.ref()));
				}
				ps.writeObjectRef(thread.ref());
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 frame(long): " + frame);
				}
				ps.writeFrameRef(frame);
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 slots(SlotInfo[]): " + "");
				}
				ps.writeInt(slots.length);
				for(int i = 0; i < slots.length; i++)
				{
					if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
					{
						ps.vm.printTrace("Sending:                     slots[i](SlotInfo): " + "");
					}
					slots[i].write(ps);
				}
				ps.send();
				return ps;
			}

			static GetValues waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new GetValues(vm, ps);
			}


			/**
			 * The number of values retrieved, always equal to slots,
			 * the number of values to get.
			 */
			final ValueImpl[] values;

			private GetValues(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.StackFrame.GetValues" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "values(ValueImpl[]): " + "");
				}
				int valuesCount = ps.readInt();
				values = new ValueImpl[valuesCount];
				for(int i = 0; i < valuesCount; i++)
				{
					values[i] = ps.readValue();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "values[i](ValueImpl): " + values[i]);
					}
				}
			}
		}

		/**
		 * Sets the value of one or more local variables.
		 * Each variable must be visible at the current frame code index.
		 * For primitive values, the value's type must match the
		 * variable's type exactly. For object values, there must be a
		 * widening reference conversion from the value's type to the
		 * variable's type and the variable's type must be loaded.
		 * <p/>
		 * Even if local variable information is not available, values can
		 * be set, if the front-end is able to
		 * determine the correct local variable index. (Typically, this
		 * index can be determined for method arguments from the method
		 * signature without access to the local variable table information.)
		 */
		static class SetValues
		{
			static final int COMMAND = 2;

			static class SlotInfo
			{

				/**
				 * The slot ID.
				 */
				final int slot;

				/**
				 * The value to set.
				 */
				final ValueImpl slotValue;

				SlotInfo(int slot, ValueImpl slotValue)
				{
					this.slot = slot;
					this.slotValue = slotValue;
				}

				private void write(PacketStream ps)
				{
					if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
					{
						ps.vm.printTrace("Sending:                     slot(int): " + slot);
					}
					ps.writeInt(slot);
					if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
					{
						ps.vm.printTrace("Sending:                     slotValue(ValueImpl): " + slotValue);
					}
					ps.writeValue(slotValue);
				}
			}

			static SetValues process(
					VirtualMachineImpl vm, ThreadReferenceImpl thread, long frame, SlotInfo[] slotValues) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, thread, frame, slotValues);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ThreadReferenceImpl thread, long frame, SlotInfo[] slotValues)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.StackFrame.SetValues" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 thread(ThreadReferenceImpl): " + (thread == null ? "NULL" : "ref=" + thread.ref()));
				}
				ps.writeObjectRef(thread.ref());
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 frame(long): " + frame);
				}
				ps.writeFrameRef(frame);
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 slotValues(SlotInfo[]): " + "");
				}
				ps.writeInt(slotValues.length);
				for(int i = 0; i < slotValues.length; i++)
				{
					if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
					{
						ps.vm.printTrace("Sending:                     slotValues[i](SlotInfo): " + "");
					}
					slotValues[i].write(ps);
				}
				ps.send();
				return ps;
			}

			static SetValues waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new SetValues(vm, ps);
			}


			private SetValues(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.StackFrame.SetValues" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
			}
		}

		/**
		 * Returns the value of the 'this' reference for this frame.
		 * If the frame's method is static or native, the reply
		 * will contain the null object reference.
		 */
		static class ThisObject
		{
			static final int COMMAND = 3;

			static ThisObject process(
					VirtualMachineImpl vm, ThreadReferenceImpl thread, long frame) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, thread, frame);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ThreadReferenceImpl thread, long frame)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.StackFrame.ThisObject" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 thread(ThreadReferenceImpl): " + (thread == null ? "NULL" : "ref=" + thread.ref()));
				}
				ps.writeObjectRef(thread.ref());
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 frame(long): " + frame);
				}
				ps.writeFrameRef(frame);
				ps.send();
				return ps;
			}

			static ThisObject waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new ThisObject(vm, ps);
			}


			/**
			 * The 'this' object for this frame.
			 */
			final ObjectReferenceImpl objectThis;

			private ThisObject(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.StackFrame.ThisObject" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				objectThis = ps.readTaggedObjectReference();
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "objectThis(ObjectReferenceImpl): " + (objectThis == null ? "NULL" : "ref=" + objectThis.ref()));
				}
			}
		}

		/**
		 * Pop the top-most stack frames of the thread stack, up to, and including 'frame'.
		 * The thread must be suspended to perform this command.
		 * The top-most stack frames are discarded and the stack frame previous to 'frame'
		 * becomes the current frame. The operand stack is restored -- the argument values
		 * are added back and if the invoke was not <code>invokestatic</code>,
		 * <code>objectref</code> is added back as well. The Java virtual machine
		 * program counter is restored to the opcode of the invoke instruction.
		 * <p/>
		 * Since JDWP version 1.4. Requires canPopFrames capability - see
		 * <a href="#JDWP_VirtualMachine_CapabilitiesNew">CapabilitiesNew</a>.
		 */
		static class PopFrames
		{
			static final int COMMAND = 4;

			static PopFrames process(
					VirtualMachineImpl vm, ThreadReferenceImpl thread, long frame) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, thread, frame);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ThreadReferenceImpl thread, long frame)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.StackFrame.PopFrames" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 thread(ThreadReferenceImpl): " + (thread == null ? "NULL" : "ref=" + thread.ref()));
				}
				ps.writeObjectRef(thread.ref());
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 frame(long): " + frame);
				}
				ps.writeFrameRef(frame);
				ps.send();
				return ps;
			}

			static PopFrames waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new PopFrames(vm, ps);
			}


			private PopFrames(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.StackFrame.PopFrames" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
			}
		}
	}

	static class ClassObjectReference
	{
		static final int COMMAND_SET = 17;

		private ClassObjectReference()
		{
		}  // hide constructor

		/**
		 * Returns the reference type reflected by this class object.
		 */
		static class ReflectedType
		{
			static final int COMMAND = 1;

			static ReflectedType process(
					VirtualMachineImpl vm, ClassObjectReferenceImpl classObject) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, classObject);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, ClassObjectReferenceImpl classObject)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				if((vm.traceFlags & mono.debugger.VirtualMachine.TRACE_SENDS) != 0)
				{
					vm.printTrace("Sending Command(id=" + ps.pkt.id + ") JDWP.ClassObjectReference.ReflectedType" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : ""));
				}
				if((ps.vm.traceFlags & VirtualMachineImpl.TRACE_SENDS) != 0)
				{
					ps.vm.printTrace("Sending:                 classObject(ClassObjectReferenceImpl): " + (classObject == null ? "NULL" : "ref=" +
							classObject.ref()));
				}
				ps.writeObjectRef(classObject.ref());
				ps.send();
				return ps;
			}

			static ReflectedType waitForReply(VirtualMachineImpl vm, PacketStream ps) throws JDWPException
			{
				ps.waitForReply();
				return new ReflectedType(vm, ps);
			}


			/**
			 * <a href="#JDWP_TypeTag">Kind</a>
			 * of following reference type.
			 */
			final byte refTypeTag;

			/**
			 * reflected reference type
			 */
			final long typeID;

			private ReflectedType(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.ClassObjectReference.ReflectedType" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				refTypeTag = ps.readByte();
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "refTypeTag(byte): " + refTypeTag);
				}
				typeID = ps.readClassRef();
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "typeID(long): " + "ref=" + typeID);
				}
			}
		}
	}

	public static class Event
	{
		static final int COMMAND_SET = 64;

		private Event()
		{
		}  // hide constructor

		/**
		 * Several events may occur at a given time in the target VM.
		 * For example, there may be more than one breakpoint request
		 * for a given location
		 * or you might single step to the same location as a
		 * breakpoint request.  These events are delivered
		 * together as a composite event.  For uniformity, a
		 * composite event is always used
		 * to deliver events, even if there is only one event to report.
		 * <p/>
		 * The events that are grouped in a composite event are restricted in the
		 * following ways:
		 * <p/>
		 * <UL>
		 * <LI>Only with other thread start events for the same thread:
		 * <UL>
		 * <LI>Thread Start Event
		 * </UL>
		 * <LI>Only with other thread death events for the same thread:
		 * <UL>
		 * <LI>Thread Death Event
		 * </UL>
		 * <LI>Only with other class prepare events for the same class:
		 * <UL>
		 * <LI>Class Prepare Event
		 * </UL>
		 * <LI>Only with other class unload events for the same class:
		 * <UL>
		 * <LI>Class Unload Event
		 * </UL>
		 * <LI>Only with other access watchpoint events for the same field access:
		 * <UL>
		 * <LI>Access Watchpoint Event
		 * </UL>
		 * <LI>Only with other modification watchpoint events for the same field
		 * modification:
		 * <UL>
		 * <LI>Modification Watchpoint Event
		 * </UL>
		 * <LI>Only with other Monitor contended enter events for the same monitor object:
		 * <UL>
		 * <LI>Monitor Contended Enter Event
		 * </UL>
		 * <LI>Only with other Monitor contended entered events for the same monitor object:
		 * <UL>
		 * <LI>Monitor Contended Entered Event
		 * </UL>
		 * <LI>Only with other Monitor wait events for the same monitor object:
		 * <UL>
		 * <LI>Monitor Wait Event
		 * </UL>
		 * <LI>Only with other Monitor waited events for the same monitor object:
		 * <UL>
		 * <LI>Monitor Waited Event
		 * </UL>
		 * <LI>Only with other ExceptionEvents for the same exception occurrance:
		 * <UL>
		 * <LI>ExceptionEvent
		 * </UL>
		 * <LI>Only with other members of this group, at the same location
		 * and in the same thread:
		 * <UL>
		 * <LI>Breakpoint Event
		 * <LI>Step Event
		 * <LI>Method Entry Event
		 * <LI>Method Exit Event
		 * </UL>
		 * </UL>
		 * <p/>
		 * The VM Start Event and VM Death Event are automatically generated events.
		 * This means they do not need to be requested using the
		 * <a href="#JDWP_EventRequest_Set">EventRequest.Set</a> command.
		 * The VM Start event signals the completion of VM initialization. The VM Death
		 * event signals the termination of the VM.
		 * If there is a debugger connected at the time when an automatically generated
		 * event occurs it is sent from the target VM. Automatically generated events may
		 * also be requested using the EventRequest.Set command and thus multiple events
		 * of the same event kind will be sent from the target VM when an event occurs.
		 * Automatically generated events are sent with the requestID field
		 * in the Event Data set to 0. The value of the suspendPolicy field in the
		 * Event Data depends on the event. For the automatically generated VM Start
		 * Event the value of suspendPolicy is not defined and is therefore implementation
		 * or configuration specific. In the Sun implementation, for example, the
		 * suspendPolicy is specified as an option to the JDWP agent at launch-time.
		 * The automatically generated VM Death Event will have the suspendPolicy set to
		 * NONE.
		 */
		public static class Composite
		{
			static final int COMMAND = 100;

			public static class Events
			{
				public abstract static class EventsCommon
				{
					abstract byte eventKind();
				}

				/**
				 * Event kind selector
				 */
				final byte eventKind;
				EventsCommon aEventsCommon;

				Events(VirtualMachineImpl vm, PacketStream ps)
				{
					eventKind = ps.readByte();
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "eventKind(byte): " + eventKind);
					}
					switch(eventKind)
					{
						case JDWP.EventKind.VM_START:
							aEventsCommon = new VMStart(vm, ps);
							break;
						case JDWP.EventKind.SINGLE_STEP:
							aEventsCommon = new SingleStep(vm, ps);
							break;
						case JDWP.EventKind.BREAKPOINT:
							aEventsCommon = new Breakpoint(vm, ps);
							break;
						case JDWP.EventKind.METHOD_ENTRY:
							aEventsCommon = new MethodEntry(vm, ps);
							break;
						case JDWP.EventKind.METHOD_EXIT:
							aEventsCommon = new MethodExit(vm, ps);
							break;
						case JDWP.EventKind.METHOD_EXIT_WITH_RETURN_VALUE:
							aEventsCommon = new MethodExitWithReturnValue(vm, ps);
							break;
						case JDWP.EventKind.EXCEPTION:
							aEventsCommon = new Exception(vm, ps);
							break;
						case JDWP.EventKind.THREAD_START:
							aEventsCommon = new ThreadStart(vm, ps);
							break;
						case JDWP.EventKind.THREAD_DEATH:
							aEventsCommon = new ThreadDeath(vm, ps);
							break;
						case JDWP.EventKind.ASSEMBLY_LOAD:
							aEventsCommon = new AssemblyLoad(vm, ps);
							break;
						case JDWP.EventKind.ASSEMBLY_UNLOAD:
							aEventsCommon = new AssemblyUnLoad(vm, ps);
							break;
						case JDWP.EventKind.VM_DEATH:
							aEventsCommon = new VMDeath(vm, ps);
							break;
						default:
							System.out.println("Unknown EventKind: " + eventKind);
							break;
					}
				}

				/**
				 * Notification of initialization of a target VM.  This event is
				 * received before the main thread is started and before any
				 * application code has been executed. Before this event occurs
				 * a significant amount of system code has executed and a number
				 * of system classes have been loaded.
				 * This event is always generated by the target VM, even
				 * if not explicitly requested.
				 */
				public static class VMStart extends EventsCommon
				{
					static final byte ALT_ID = JDWP.EventKind.VM_START;

					@Override
					byte eventKind()
					{
						return ALT_ID;
					}

					/**
					 * Request that generated event (or 0 if this
					 * event is automatically generated.
					 */
					public final int requestID;

					/**
					 * Initial thread
					 */
					public final ThreadReferenceImpl thread;

					VMStart(VirtualMachineImpl vm, PacketStream ps)
					{
						requestID = ps.readInt();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "requestID(int): " + requestID);
						}
						thread = ps.readThreadReference();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "thread(ThreadReferenceImpl): " + (thread == null ? "NULL" : "ref=" + thread.ref()));
						}
					}
				}

				/**
				 * Notification of step completion in the target VM. The step event
				 * is generated before the code at its location is executed.
				 */
				static class SingleStep extends EventsCommon
				{
					static final byte ALT_ID = JDWP.EventKind.SINGLE_STEP;

					@Override
					byte eventKind()
					{
						return ALT_ID;
					}

					/**
					 * Request that generated event
					 */
					final int requestID;

					/**
					 * Stepped thread
					 */
					final ThreadReferenceImpl thread;

					/**
					 * Location stepped to
					 */
					final Location location;

					SingleStep(VirtualMachineImpl vm, PacketStream ps)
					{
						requestID = ps.readInt();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "requestID(int): " + requestID);
						}
						thread = ps.readThreadReference();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "thread(ThreadReferenceImpl): " + (thread == null ? "NULL" : "ref=" + thread.ref()));
						}
						location = ps.readLocation();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "location(Location): " + location);
						}
					}
				}

				/**
				 * Notification of a breakpoint in the target VM. The breakpoint event
				 * is generated before the code at its location is executed.
				 */
				static class Breakpoint extends EventsCommon
				{
					static final byte ALT_ID = JDWP.EventKind.BREAKPOINT;

					@Override
					byte eventKind()
					{
						return ALT_ID;
					}

					/**
					 * Request that generated event
					 */
					final int requestID;

					/**
					 * Thread which hit breakpoint
					 */
					final ThreadReferenceImpl thread;

					/**
					 * Location hit
					 */
					final Location location;

					Breakpoint(VirtualMachineImpl vm, PacketStream ps)
					{
						requestID = ps.readInt();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "requestID(int): " + requestID);
						}
						thread = ps.readThreadReference();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "thread(ThreadReferenceImpl): " + (thread == null ? "NULL" : "ref=" + thread.ref()));
						}
						location = ps.readLocation();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "location(Location): " + location);
						}
					}
				}

				/**
				 * Notification of a method invocation in the target VM. This event
				 * is generated before any code in the invoked method has executed.
				 * Method entry events are generated for both native and non-native
				 * methods.
				 * <p/>
				 * In some VMs method entry events can occur for a particular thread
				 * before its thread start event occurs if methods are called
				 * as part of the thread's initialization.
				 */
				static class MethodEntry extends EventsCommon
				{
					static final byte ALT_ID = JDWP.EventKind.METHOD_ENTRY;

					@Override
					byte eventKind()
					{
						return ALT_ID;
					}

					/**
					 * Request that generated event
					 */
					final int requestID;

					/**
					 * Thread which entered method
					 */
					final ThreadReferenceImpl thread;

					/**
					 * The initial executable location in the method.
					 */
					final Location location;

					MethodEntry(VirtualMachineImpl vm, PacketStream ps)
					{
						requestID = ps.readInt();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "requestID(int): " + requestID);
						}
						thread = ps.readThreadReference();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "thread(ThreadReferenceImpl): " + (thread == null ? "NULL" : "ref=" + thread.ref()));
						}
						location = ps.readLocation();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "location(Location): " + location);
						}
					}
				}

				/**
				 * Notification of a method return in the target VM. This event
				 * is generated after all code in the method has executed, but the
				 * location of this event is the last executed location in the method.
				 * Method exit events are generated for both native and non-native
				 * methods. Method exit events are not generated if the method terminates
				 * with a thrown exception.
				 */
				static class MethodExit extends EventsCommon
				{
					static final byte ALT_ID = JDWP.EventKind.METHOD_EXIT;

					@Override
					byte eventKind()
					{
						return ALT_ID;
					}

					/**
					 * Request that generated event
					 */
					final int requestID;

					/**
					 * Thread which exited method
					 */
					final ThreadReferenceImpl thread;

					/**
					 * Location of exit
					 */
					final Location location;

					MethodExit(VirtualMachineImpl vm, PacketStream ps)
					{
						requestID = ps.readInt();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "requestID(int): " + requestID);
						}
						thread = ps.readThreadReference();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "thread(ThreadReferenceImpl): " + (thread == null ? "NULL" : "ref=" + thread.ref()));
						}
						location = ps.readLocation();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "location(Location): " + location);
						}
					}
				}

				/**
				 * Notification of a method return in the target VM. This event
				 * is generated after all code in the method has executed, but the
				 * location of this event is the last executed location in the method.
				 * Method exit events are generated for both native and non-native
				 * methods. Method exit events are not generated if the method terminates
				 * with a thrown exception. <p>Since JDWP version 1.6.
				 */
				static class MethodExitWithReturnValue extends EventsCommon
				{
					static final byte ALT_ID = JDWP.EventKind.METHOD_EXIT_WITH_RETURN_VALUE;

					@Override
					byte eventKind()
					{
						return ALT_ID;
					}

					/**
					 * Request that generated event
					 */
					final int requestID;

					/**
					 * Thread which exited method
					 */
					final ThreadReferenceImpl thread;

					/**
					 * Location of exit
					 */
					final Location location;

					/**
					 * Value that will be returned by the method
					 */
					final ValueImpl value;

					MethodExitWithReturnValue(VirtualMachineImpl vm, PacketStream ps)
					{
						requestID = ps.readInt();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "requestID(int): " + requestID);
						}
						thread = ps.readThreadReference();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "thread(ThreadReferenceImpl): " + (thread == null ? "NULL" : "ref=" + thread.ref()));
						}
						location = ps.readLocation();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "location(Location): " + location);
						}
						value = ps.readValue();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "value(ValueImpl): " + value);
						}
					}
				}


				/**
				 * Notification of an exception in the target VM.
				 * If the exception is thrown from a non-native method,
				 * the exception event is generated at the location where the
				 * exception is thrown.
				 * If the exception is thrown from a native method, the exception event
				 * is generated at the first non-native location reached after the exception
				 * is thrown.
				 */
				static class Exception extends EventsCommon
				{
					static final byte ALT_ID = JDWP.EventKind.EXCEPTION;

					@Override
					byte eventKind()
					{
						return ALT_ID;
					}

					/**
					 * Request that generated event
					 */
					final int requestID;

					/**
					 * Thread with exception
					 */
					final ThreadReferenceImpl thread;

					/**
					 * Location of exception throw
					 * (or first non-native location after throw if thrown from a native method)
					 */
					final Location location;

					/**
					 * Thrown exception
					 */
					final ObjectReferenceImpl exception;

					/**
					 * Location of catch, or 0 if not caught. An exception
					 * is considered to be caught if, at the point of the throw, the
					 * current location is dynamically enclosed in a try statement that
					 * handles the exception. (See the JVM specification for details).
					 * If there is such a try statement, the catch location is the
					 * first location in the appropriate catch clause.
					 * <p/>
					 * If there are native methods in the call stack at the time of the
					 * exception, there are important restrictions to note about the
					 * returned catch location. In such cases,
					 * it is not possible to predict whether an exception will be handled
					 * by some native method on the call stack.
					 * Thus, it is possible that exceptions considered uncaught
					 * here will, in fact, be handled by a native method and not cause
					 * termination of the target VM. Furthermore, it cannot be assumed that the
					 * catch location returned here will ever be reached by the throwing
					 * thread. If there is
					 * a native frame between the current location and the catch location,
					 * the exception might be handled and cleared in that native method
					 * instead.
					 * <p/>
					 * Note that compilers can generate try-catch blocks in some cases
					 * where they are not explicit in the source code; for example,
					 * the code generated for <code>synchronized</code> and
					 * <code>finally</code> blocks can contain implicit try-catch blocks.
					 * If such an implicitly generated try-catch is
					 * present on the call stack at the time of the throw, the exception
					 * will be considered caught even though it appears to be uncaught from
					 * examination of the source code.
					 */
					final Location catchLocation;

					Exception(VirtualMachineImpl vm, PacketStream ps)
					{
						requestID = ps.readInt();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "requestID(int): " + requestID);
						}
						thread = ps.readThreadReference();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "thread(ThreadReferenceImpl): " + (thread == null ? "NULL" : "ref=" + thread.ref()));
						}
						location = ps.readLocation();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "location(Location): " + location);
						}
						exception = ps.readTaggedObjectReference();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "exception(ObjectReferenceImpl): " + (exception == null ? "NULL" : "ref=" + exception.ref()));
						}
						catchLocation = ps.readLocation();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "catchLocation(Location): " + catchLocation);
						}
					}
				}

				/**
				 * Notification of a new running thread in the target VM.
				 * The new thread can be the result of a call to
				 * <code>java.lang.Thread.start</code> or the result of
				 * attaching a new thread to the VM though JNI. The
				 * notification is generated by the new thread some time before
				 * its execution starts.
				 * Because of this timing, it is possible to receive other events
				 * for the thread before this event is received. (Notably,
				 * Method Entry Events and Method Exit Events might occur
				 * during thread initialization.
				 * It is also possible for the
				 * <a href="#JDWP_VirtualMachine_AllThreads">VirtualMachine AllThreads</a>
				 * command to return
				 * a thread before its thread start event is received.
				 * <p/>
				 * Note that this event gives no information
				 * about the creation of the thread object which may have happened
				 * much earlier, depending on the VM being debugged.
				 */
				static class ThreadStart extends EventsCommon
				{
					static final byte ALT_ID = JDWP.EventKind.THREAD_START;

					@Override
					byte eventKind()
					{
						return ALT_ID;
					}

					/**
					 * Request that generated event
					 */
					final int requestID;

					/**
					 * Started thread
					 */
					final ThreadReferenceImpl thread;

					ThreadStart(VirtualMachineImpl vm, PacketStream ps)
					{
						requestID = ps.readInt();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "requestID(int): " + requestID);
						}
						thread = ps.readThreadReference();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "thread(ThreadReferenceImpl): " + (thread == null ? "NULL" : "ref=" + thread.ref()));
						}
					}
				}

				/**
				 * Notification of a completed thread in the target VM. The
				 * notification is generated by the dying thread before it terminates.
				 * Because of this timing, it is possible
				 * for {@link VirtualMachine#allThreads} to return this thread
				 * after this event is received.
				 * <p/>
				 * Note that this event gives no information
				 * about the lifetime of the thread object. It may or may not be collected
				 * soon depending on what references exist in the target VM.
				 */
				static class ThreadDeath extends EventsCommon
				{
					static final byte ALT_ID = JDWP.EventKind.THREAD_DEATH;

					@Override
					byte eventKind()
					{
						return ALT_ID;
					}

					/**
					 * Request that generated event
					 */
					final int requestID;

					/**
					 * Ending thread
					 */
					final ThreadReferenceImpl thread;

					ThreadDeath(VirtualMachineImpl vm, PacketStream ps)
					{
						requestID = ps.readInt();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "requestID(int): " + requestID);
						}
						thread = ps.readThreadReference();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "thread(ThreadReferenceImpl): " + (thread == null ? "NULL" : "ref=" + thread.ref()));
						}
					}
				}

				public static class AssemblyLoad extends EventsCommon
				{
					static final byte ALT_ID = JDWP.EventKind.ASSEMBLY_LOAD;

					@Override
					byte eventKind()
					{
						return ALT_ID;
					}

					/**
					 * Request that generated event
					 */
					public final int requestID;

					public final ThreadReferenceImpl thread;

					public final AssemblyMirror assembly;


					public AssemblyLoad(VirtualMachineImpl vm, PacketStream ps)
					{
						requestID = ps.readInt();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "requestID(int): " + requestID);
						}
						thread = ps.readThreadReference();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "thread(ThreadReferenceImpl): " + (thread == null ? "NULL" : "ref=" + thread.ref()));
						}
						assembly = ps.readAssemblyReference();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "assembly(AssemblyReferene): " + (assembly == null ? "NULL" : "ref=" + assembly.id()));
						}
					}
				}

				public static class AssemblyUnLoad extends EventsCommon
				{
					static final byte ALT_ID = JDWP.EventKind.ASSEMBLY_LOAD;

					@Override
					byte eventKind()
					{
						return ALT_ID;
					}

					/**
					 * Request that generated event
					 */
					public final int requestID;

					public final ThreadReferenceImpl thread;

					public final AssemblyMirror assembly;

					public AssemblyUnLoad(VirtualMachineImpl vm, PacketStream ps)
					{
						requestID = ps.readInt();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "requestID(int): " + requestID);
						}
						thread = ps.readThreadReference();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "thread(ThreadReferenceImpl): " + (thread == null ? "NULL" : "ref=" + thread.ref()));
						}
						assembly = ps.readAssemblyReference();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "assembly(AssemblyReferene): " + (assembly == null ? "NULL" : "ref=" + assembly.id()));
						}
					}
				}

				/**
				 * Notification of a class prepare in the target VM. See the JVM
				 * specification for a definition of class preparation. Class prepare
				 * events are not generated for primtiive classes (for example,
				 * java.lang.Integer.TYPE).
				 */
				public static class ClassPrepare extends EventsCommon
				{
					static final byte ALT_ID = JDWP.EventKind.ASSEMBLY_LOAD;

					@Override
					byte eventKind()
					{
						return ALT_ID;
					}

					/**
					 * Request that generated event
					 */
					final int requestID;

					/**
					 * Preparing thread.
					 * In rare cases, this event may occur in a debugger system
					 * thread within the target VM. Debugger threads take precautions
					 * to prevent these events, but they cannot be avoided under some
					 * conditions, especially for some subclasses of
					 * java.lang.Error.
					 * If the event was generated by a debugger system thread, the
					 * value returned by this method is null, and if the requested
					 * <a href="#JDWP_SuspendPolicy">suspend policy</a>
					 * for the event was EVENT_THREAD
					 * all threads will be suspended instead, and the
					 * composite event's suspend policy will reflect this change.
					 * <p/>
					 * Note that the discussion above does not apply to system threads
					 * created by the target VM during its normal (non-debug) operation.
					 */
					final ThreadReferenceImpl thread;

					/**
					 * Type being prepared
					 */
					final long typeID;


					ClassPrepare(VirtualMachineImpl vm, PacketStream ps)
					{
						requestID = ps.readInt();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "requestID(int): " + requestID);
						}
						thread = ps.readThreadReference();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "thread(ThreadReferenceImpl): " + (thread == null ? "NULL" : "ref=" + thread.ref()));
						}
						typeID = ps.readClassRef();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "typeID(long): " + "ref=" + typeID);
						}
					}
				}

				/**
				 * Notification of a class unload in the target VM.
				 * <p/>
				 * There are severe constraints on the debugger back-end during
				 * garbage collection, so unload information is greatly limited.
				 */
				static class ClassUnload extends EventsCommon
				{
					static final byte ALT_ID = JDWP.EventKind.ASSEMBLY_UNLOAD;

					@Override
					byte eventKind()
					{
						return ALT_ID;
					}


					final int requestID;

					final ThreadReferenceImpl thread;

					/**
					 * Type being prepared
					 */
					final long typeID;

					ClassUnload(VirtualMachineImpl vm, PacketStream ps)
					{
						requestID = ps.readInt();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "requestID(int): " + requestID);
						}
						thread = ps.readThreadReference();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "thread(ThreadReferenceImpl): " + (thread == null ? "NULL" : "ref=" + thread.ref()));
						}
						typeID = ps.readClassRef();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "typeID(long): " + "ref=" + typeID);
						}
					}
				}

				public static class VMDeath extends EventsCommon
				{
					static final byte ALT_ID = JDWP.EventKind.VM_DEATH;

					@Override
					byte eventKind()
					{
						return ALT_ID;
					}

					/**
					 * Request that generated event
					 */
					public final int requestID;

					VMDeath(VirtualMachineImpl vm, PacketStream ps)
					{
						requestID = ps.readInt();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "requestID(int): " + requestID);
						}
					}
				}
			}


			/**
			 * Which threads where suspended by this composite event?
			 */
			final byte suspendPolicy;

			/**
			 * Events in set.
			 */
			final Events[] events;

			Composite(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.Event.Composite" + (ps.pkt.flags != 0 ? ", FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				suspendPolicy = ps.readByte();
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "suspendPolicy(byte): " + suspendPolicy);
				}
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "events(Events[]): " + "");
				}
				int eventsCount = ps.readInt();
				events = new Events[eventsCount];
				for(int i = 0; i < eventsCount; i++)
				{
					if(vm.traceReceives)
					{
						vm.printReceiveTrace(5, "events[i](Events): " + "");
					}
					events[i] = new Events(vm, ps);
				}
			}
		}
	}

	static class Error
	{
		static final int NONE = 0;
		static final int INVALID_THREAD = 10;
		static final int INVALID_THREAD_GROUP = 11;
		static final int INVALID_PRIORITY = 12;
		static final int THREAD_NOT_SUSPENDED = 13;
		static final int THREAD_SUSPENDED = 14;
		static final int THREAD_NOT_ALIVE = 15;
		static final int INVALID_OBJECT = 20;
		static final int INVALID_CLASS = 21;
		static final int CLASS_NOT_PREPARED = 22;
		static final int INVALID_METHODID = 23;
		static final int INVALID_LOCATION = 24;
		static final int INVALID_FIELDID = 25;
		static final int INVALID_FRAMEID = 30;
		static final int NO_MORE_FRAMES = 31;
		static final int OPAQUE_FRAME = 32;
		static final int NOT_CURRENT_FRAME = 33;
		static final int TYPE_MISMATCH = 34;
		static final int INVALID_SLOT = 35;
		static final int DUPLICATE = 40;
		static final int NOT_FOUND = 41;
		static final int INVALID_MONITOR = 50;
		static final int NOT_MONITOR_OWNER = 51;
		static final int INTERRUPT = 52;
		static final int INVALID_CLASS_FORMAT = 60;
		static final int CIRCULAR_CLASS_DEFINITION = 61;
		static final int FAILS_VERIFICATION = 62;
		static final int ADD_METHOD_NOT_IMPLEMENTED = 63;
		static final int SCHEMA_CHANGE_NOT_IMPLEMENTED = 64;
		static final int INVALID_TYPESTATE = 65;
		static final int HIERARCHY_CHANGE_NOT_IMPLEMENTED = 66;
		static final int DELETE_METHOD_NOT_IMPLEMENTED = 67;
		static final int UNSUPPORTED_VERSION = 68;
		static final int NAMES_DONT_MATCH = 69;
		static final int CLASS_MODIFIERS_CHANGE_NOT_IMPLEMENTED = 70;
		static final int METHOD_MODIFIERS_CHANGE_NOT_IMPLEMENTED = 71;
		static final int NOT_IMPLEMENTED = 100;
		static final int NULL_POINTER = 100;
		static final int ABSENT_INFORMATION = 101;
		static final int INVALID_EVENT_TYPE = 102;
		static final int ILLEGAL_ARGUMENT = 103;
		static final int OUT_OF_MEMORY = 110;
		static final int ACCESS_DENIED = 111;
		static final int VM_DEAD = 112;
		static final int INTERNAL = 113;
		static final int UNATTACHED_THREAD = 115;
		static final int INVALID_TAG = 500;
		static final int ALREADY_INVOKING = 502;
		static final int INVALID_INDEX = 503;
		static final int INVALID_LENGTH = 504;
		static final int INVALID_STRING = 506;
		static final int INVALID_CLASS_LOADER = 507;
		static final int INVALID_ARRAY = 508;
		static final int TRANSPORT_LOAD = 509;
		static final int TRANSPORT_INIT = 510;
		static final int NATIVE_METHOD = 511;
		static final int INVALID_COUNT = 512;
	}

	static class EventKind
	{
		static final int VM_START = 0;
		static final int VM_DEATH = 1;

		static final int ASSEMBLY_LOAD = 8;
		static final int ASSEMBLY_UNLOAD = 9;
		static final int BREAKPOINT = 10;
		static final int SINGLE_STEP = 11;
		static final int TYPE_LOAD = 12;

		static final int FRAME_POP = 3;
		static final int EXCEPTION = 4;
		static final int USER_DEFINED = 5;
		static final int THREAD_START = 6;
		static final int THREAD_DEATH = 7;

		static final int EXCEPTION_CATCH = 30;
		static final int METHOD_ENTRY = 40;
		static final int METHOD_EXIT = 41;
		static final int METHOD_EXIT_WITH_RETURN_VALUE = 42;
	}

	static class ThreadStatus
	{
		static final int ZOMBIE = 0;
		static final int RUNNING = 1;
		static final int SLEEPING = 2;
		static final int MONITOR = 3;
		static final int WAIT = 4;
	}

	static class SuspendStatus
	{
		static final int SUSPEND_STATUS_SUSPENDED = 0x1;
	}

	static class ClassStatus
	{
		static final int VERIFIED = 1;
		static final int PREPARED = 2;
		static final int INITIALIZED = 4;
		static final int ERROR = 8;
	}

	static class TypeTag
	{
		static final int CLASS = 1;

		static final int ARRAY = 3;
	}

	static class Tag
	{
		static final int ARRAY = 91;
		static final int BYTE = 66;
		static final int CHAR = 67;
		static final int OBJECT = 76;
		static final int FLOAT = 70;
		static final int DOUBLE = 68;
		static final int INT = 73;
		static final int LONG = 74;
		static final int SHORT = 83;
		static final int VOID = 86;
		static final int BOOLEAN = 90;
		static final int STRING = 115;
		static final int THREAD = 116;


		static final int CLASS_OBJECT = 99;
	}

	static class StepDepth
	{
		static final int INTO = 0;
		static final int OVER = 1;
		static final int OUT = 2;
	}

	static class StepSize
	{
		static final int MIN = 0;
		static final int LINE = 1;
	}

	static class SuspendPolicy
	{
		static final int NONE = 0;
		static final int EVENT_THREAD = 1;
		static final int ALL = 2;
	}

	/**
	 * The invoke options are a combination of zero or more of the following bit flags:
	 */
	static class InvokeOptions
	{
		static final int INVOKE_SINGLE_THREADED = 0x01;
		static final int INVOKE_NONVIRTUAL = 0x02;
	}
}