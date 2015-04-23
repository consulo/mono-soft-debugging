package mono.debugger;

import mono.debugger.request.StepRequest;

public class JDWP
{

	public static class VirtualMachine
	{
		static final int COMMAND_SET = 1;

		private VirtualMachine()
		{
		}  // hide constructor


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
			final ThreadMirror[] threads;

			private AllThreads(VirtualMachineImpl vm, PacketStream ps)
			{
				if(vm.traceReceives)
				{
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.VirtualMachine.AllThreads" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
				}
				if(vm.traceReceives)
				{
					vm.printReceiveTrace(4, "threads(ThreadMirror[]): " + "");
				}
				int threadsCount = ps.readInt();
				threads = new ThreadMirror[threadsCount];
				for(int i = 0; i < threadsCount; i++)
				{
					threads[i] = ps.readThreadMirror();
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

	public static class EventRequest
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
		public static class Set
		{
			static final int COMMAND = 1;

			public static class Modifier
			{
				abstract static class ModifierCommon
				{
					abstract void write(PacketStream ps, VirtualMachineImpl vm);
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

				private void write(VirtualMachineImpl vm, PacketStream ps)
				{
					ps.writeByte(modKind);
					aModifierCommon.write(ps, vm);
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
				public static class Count extends ModifierCommon
				{
					static final byte ALT_ID = 1;

					public static Modifier create(int count)
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
					void write(PacketStream ps, VirtualMachineImpl vm)
					{
						ps.writeInt(count);
					}
				}

				/**
				 * Restricts reported events to
				 * those in the given thread.
				 * This modifier can be used with any event kind
				 * except for class unload.
				 */
				public static class ThreadOnly extends ModifierCommon
				{
					static final byte ALT_ID = 3;

					public static Modifier create(ThreadMirror thread)
					{
						return new Modifier(ALT_ID, new ThreadOnly(thread));
					}

					/**
					 * Required thread
					 */
					final ThreadMirror thread;

					ThreadOnly(ThreadMirror thread)
					{
						this.thread = thread;
					}

					@Override
					void write(PacketStream ps, VirtualMachineImpl vm)
					{
						ps.writeId(thread);
					}
				}

				/**
				 * Restricts reported events to those that occur at
				 * the given location.
				 * This modifier can be used with
				 * breakpoint, field access, field modification,
				 * step, and exception event kinds.
				 */
				public static class LocationOnly extends ModifierCommon
				{
					static final byte ALT_ID = 7;

					public static Modifier create(Location loc)
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
					void write(PacketStream ps, VirtualMachineImpl vm)
					{
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

					static Modifier create(TypeMirror exceptionOrNull, boolean caught, boolean uncaught)
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
					final TypeMirror exceptionOrNull;

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

					ExceptionOnly(TypeMirror exceptionOrNull, boolean caught, boolean uncaught)
					{
						this.exceptionOrNull = exceptionOrNull;
						this.caught = caught;
						this.uncaught = uncaught;
					}

					@Override
					void write(PacketStream ps, VirtualMachineImpl vm)
					{
						ps.writeClassRef(exceptionOrNull.id());
						ps.writeBoolean(caught);
						ps.writeBoolean(uncaught);
					}
				}

				/**
				 * Restricts reported step events
				 * to those which satisfy
				 * depth and size constraints.
				 * This modifier can be used with
				 * step event kinds only.
				 */
				public static class Step extends ModifierCommon
				{
					static final byte ALT_ID = 10;

					public static Modifier create(ThreadMirror thread, StepRequest.StepSize size, StepRequest.StepDepth depth)
					{
						return new Modifier(ALT_ID, new Step(thread, size, depth));
					}

					/**
					 * Thread in which to step
					 */
					final ThreadMirror thread;

					/**
					 * size of each step.
					 * See <a href="#JDWP_StepSize">JDWP.StepSize</a>
					 */
					final StepRequest.StepSize size;

					/**
					 * relative call stack limit.
					 * See <a href="#JDWP_StepDepth">JDWP.StepDepth</a>
					 */
					final StepRequest.StepDepth depth;

					Step(ThreadMirror thread, StepRequest.StepSize size, StepRequest.StepDepth depth)
					{
						this.thread = thread;
						this.size = size;
						this.depth = depth;
					}

					@Override
					void write(PacketStream ps, VirtualMachineImpl vm)
					{
						ps.writeId(thread);
						ps.writeInt(size.ordinal());
						ps.writeInt(depth.ordinal());
						if(vm.isAtLeastVersion(2, 16))
						{
							ps.writeInt(0); //TODO [VISTALL] filter
						}
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
				public static class SourceFileMatch extends ModifierCommon
				{
					static final byte ALT_ID = 12;

					public static Modifier create(String... s)
					{
						return new Modifier(ALT_ID, new SourceFileMatch(s));
					}

					final String[] mySourceFileName;

					SourceFileMatch(String... s)
					{
						this.mySourceFileName = s;
					}

					@Override
					void write(PacketStream ps, VirtualMachineImpl vm)
					{
						ps.writeInt(mySourceFileName.length);
						for(String s : mySourceFileName)
						{
							ps.writeString(s);
						}
					}
				}

				public static class TypeNameFilter extends ModifierCommon
				{
					static final byte ALT_ID = 13;

					public static Modifier create(String... a)
					{
						return new Modifier(ALT_ID, new TypeNameFilter(a));
					}

					/**
					 * Required class
					 */
					final String[] myTypes;

					TypeNameFilter(String... a)
					{
						myTypes = a;
					}

					@Override
					void write(PacketStream ps, VirtualMachineImpl vm)
					{
						ps.writeInt(myTypes.length);
						for(String s : myTypes)
						{
							ps.writeString(s);
						}
					}
				}
			}

			public static Set process(
					VirtualMachineImpl vm, int eventKind, int suspendPolicy, Modifier[] modifiers) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, eventKind, suspendPolicy, modifiers);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, int eventKind, int suspendPolicy, Modifier[] modifiers)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				ps.writeByte((byte) eventKind);
				ps.writeByte((byte) suspendPolicy);
				ps.writeByte((byte) modifiers.length);
				for(int i = 0; i < modifiers.length; i++)
				{
					modifiers[i].write(vm, ps);
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
			public final int requestID;

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
		public static class Clear
		{
			static final int COMMAND = 2;

			public static Clear process(
					VirtualMachineImpl vm, byte eventKind, int requestID) throws JDWPException
			{
				PacketStream ps = enqueueCommand(vm, eventKind, requestID);
				return waitForReply(vm, ps);
			}

			static PacketStream enqueueCommand(
					VirtualMachineImpl vm, byte eventKind, int requestID)
			{
				PacketStream ps = new PacketStream(vm, COMMAND_SET, COMMAND);
				ps.writeByte(eventKind);
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
						case JDWP.EventKind.EXCEPTION:
							aEventsCommon = new Exception(vm, ps);
							break;
						case JDWP.EventKind.THREAD_START:
							aEventsCommon = new ThreadStart(vm, ps);
							break;
						case JDWP.EventKind.THREAD_DEATH:
							aEventsCommon = new ThreadDeath(vm, ps);
							break;
						case EventKind.APPDOMAIN_CREATE:
							aEventsCommon = new AppDomainCreate(vm, ps);
							break;
						case EventKind.APPDOMAIN_UNLOAD:
							aEventsCommon = new AppDomainUnload(vm, ps);
							break;
						case JDWP.EventKind.ASSEMBLY_LOAD:
							aEventsCommon = new AssemblyLoad(vm, ps);
							break;
						case JDWP.EventKind.ASSEMBLY_UNLOAD:
							aEventsCommon = new AssemblyUnLoad(vm, ps);
							break;
						case EventKind.USER_BREAK:
							aEventsCommon = new UserBreak(vm, ps);
							break;
						case EventKind.USER_LOG:
							aEventsCommon = new UserLog(vm, ps);
							break;
						case JDWP.EventKind.VM_DEATH:
							aEventsCommon = new VMDeath(vm, ps);
							break;
						case EventKind.TYPE_LOAD:
							aEventsCommon = new TypeLoad(vm, ps);
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
					public final ThreadMirror thread;

					VMStart(VirtualMachineImpl vm, PacketStream ps)
					{
						requestID = ps.readInt();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "requestID(int): " + requestID);
						}
						thread = ps.readThreadMirror();
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
					final ThreadMirror thread;

					/**
					 * Location stepped to
					 */
					final Location location;

					SingleStep(VirtualMachineImpl vm, PacketStream ps)
					{
						requestID = ps.readInt();
						thread = ps.readThreadMirror();
						location = ps.readLocation();
					}
				}

				/**
				 * Notification of a breakpoint in the target VM. The breakpoint event
				 * is generated before the code at its location is executed.
				 */
				public static class Breakpoint extends EventsCommon
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
					public final int requestID;

					/**
					 * Thread which hit breakpoint
					 */
					public final ThreadMirror thread;

					/**
					 * Location hit
					 */
					public final Location location;

					Breakpoint(VirtualMachineImpl vm, PacketStream ps)
					{
						requestID = ps.readInt();
						thread = ps.readThreadMirror();
						location = ps.readLocation();
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
					final ThreadMirror thread;

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
						thread = ps.readThreadMirror();
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
					final ThreadMirror thread;

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
						thread = ps.readThreadMirror();
						location = ps.readLocation();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "location(Location): " + location);
						}
					}
				}

				public static class TypeLoad extends EventsCommon
				{
					static final byte ALT_ID = (byte) mono.debugger.EventKind.TYPE_LOAD.ordinal();

					@Override
					byte eventKind()
					{
						return ALT_ID;
					}

					/**
					 * Request that generated event
					 */
					public final int requestID;

					/**
					 * Thread which exited method
					 */
					public final ThreadMirror thread;

					/**
					 * Location of exit
					 */
					public final TypeMirror typeMirror;

					TypeLoad(VirtualMachineImpl vm, PacketStream ps)
					{
						requestID = ps.readInt();
						thread = ps.readThreadMirror();
						typeMirror = ps.readTypeMirror();
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
					final ThreadMirror thread;

					/**
					 * Location of exception throw
					 * (or first non-native location after throw if thrown from a native method)
					 */
					final Location location;

					/**
					 * Thrown exception
					 */
					final ObjectValueMirror exception;

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
						thread = ps.readThreadMirror();
						location = ps.readLocation();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "location(Location): " + location);
						}
						exception = ps.readObjectMirror();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "exception(ObjectReferenceImpl): " + (exception == null ? "NULL" : "ref=" + exception.id()));
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
					final ThreadMirror thread;

					ThreadStart(VirtualMachineImpl vm, PacketStream ps)
					{
						requestID = ps.readInt();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "requestID(int): " + requestID);
						}
						thread = ps.readThreadMirror();
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
					final ThreadMirror thread;

					ThreadDeath(VirtualMachineImpl vm, PacketStream ps)
					{
						requestID = ps.readInt();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "requestID(int): " + requestID);
						}
						thread = ps.readThreadMirror();
					}
				}

				public static class AppDomainCreate extends EventsCommon
				{
					static final byte ALT_ID = EventKind.APPDOMAIN_CREATE;

					@Override
					byte eventKind()
					{
						return ALT_ID;
					}

					public final int requestID;

					public final ThreadMirror thread;

					public final AppDomainMirror appDomainMirror;

					public AppDomainCreate(VirtualMachineImpl vm, PacketStream ps)
					{
						requestID = ps.readInt();
						thread = ps.readThreadMirror();
						appDomainMirror = ps.readAppDomainMirror();
					}
				}

				public static class AppDomainUnload extends EventsCommon
				{
					static final byte ALT_ID = EventKind.APPDOMAIN_UNLOAD;

					@Override
					byte eventKind()
					{
						return ALT_ID;
					}

					public final int requestID;

					public final ThreadMirror thread;

					public final AppDomainMirror appDomainMirror;

					public AppDomainUnload(VirtualMachineImpl vm, PacketStream ps)
					{
						requestID = ps.readInt();
						thread = ps.readThreadMirror();
						appDomainMirror = ps.readAppDomainMirror();
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

					public final ThreadMirror thread;

					public final AssemblyMirror assembly;


					public AssemblyLoad(VirtualMachineImpl vm, PacketStream ps)
					{
						requestID = ps.readInt();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "requestID(int): " + requestID);
						}
						thread = ps.readThreadMirror();

						assembly = ps.readAssemblyMirror();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "assembly(AssemblyReferene): " + (assembly == null ? "NULL" : "ref=" + assembly.id()));
						}
					}
				}

				public static class UserBreak extends EventsCommon
				{
					static final byte ALT_ID = (byte) mono.debugger.EventKind.USER_BREAK.ordinal();

					@Override
					byte eventKind()
					{
						return ALT_ID;
					}

					public final int requestID;

					public final ThreadMirror thread;

					public UserBreak(VirtualMachineImpl vm, PacketStream ps)
					{
						requestID = ps.readInt();
						thread = ps.readThreadMirror();
					}
				}

				public static class UserLog extends EventsCommon
				{
					static final byte ALT_ID = (byte) mono.debugger.EventKind.USER_LOG.ordinal();

					@Override
					byte eventKind()
					{
						return ALT_ID;
					}

					public final int requestID;
					public final ThreadMirror thread;
					public final int level;
					public final String category;
					public final String message;

					public UserLog(VirtualMachineImpl vm, PacketStream ps)
					{
						requestID = ps.readInt();
						thread = ps.readThreadMirror();
						level = ps.readInt();
						category = ps.readString();
						message = ps.readString();
					}
				}

				public static class AssemblyUnLoad extends EventsCommon
				{
					static final byte ALT_ID = EventKind.ASSEMBLY_LOAD;

					@Override
					byte eventKind()
					{
						return ALT_ID;
					}

					/**
					 * Request that generated event
					 */
					public final int requestID;

					public final ThreadMirror thread;

					public final AssemblyMirror assembly;

					public AssemblyUnLoad(VirtualMachineImpl vm, PacketStream ps)
					{
						requestID = ps.readInt();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "requestID(int): " + requestID);
						}
						thread = ps.readThreadMirror();

						assembly = ps.readAssemblyMirror();
						if(vm.traceReceives)
						{
							vm.printReceiveTrace(6, "assembly(AssemblyReferene): " + (assembly == null ? "NULL" : "ref=" + assembly.id()));
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

					public final int requestID;
					public final ThreadMirror thread;
					public final int exitCode;

					VMDeath(VirtualMachineImpl vm, PacketStream ps)
					{
						requestID = ps.readInt();
						thread = ps.readThreadMirror();
						exitCode = vm.isAtLeastVersion(2, 27) ? ps.readInt() : 0;
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
					vm.printTrace("Receiving Command(id=" + ps.pkt.id + ") JDWP.Event.Composite" + (ps.pkt.flags != 0 ? ", " +
							"FLAGS=" + ps.pkt.flags : "") + (ps.pkt.errorCode != 0 ? ", ERROR CODE=" + ps.pkt.errorCode : ""));
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

		static final int INVALID_OBJECT = 20;
		static final int INVALID_FIELDID = 25;
		static final int INVALID_FRAMEID = 30;
		static final int NOT_IMPLEMENTED = 100;
		static final int NOT_SUSPENDED = 101;
		static final int INVALID_ARGUMENT = 102;
		static final int ERR_UNLOADED = 103;
		static final int ERR_NO_INVOCATION = 104;
		static final int ABSENT_INFORMATION = 105;
		static final int NO_SEQ_POINT_AT_IL_OFFSET = 106;
	}

	@Deprecated
	static class EventKind
	{
		/*
EVENT_KIND_VM_START = 0,
	EVENT_KIND_VM_DEATH = 1,
	EVENT_KIND_THREAD_START = 2,
	EVENT_KIND_THREAD_DEATH = 3,
	EVENT_KIND_APPDOMAIN_CREATE = 4,
	EVENT_KIND_APPDOMAIN_UNLOAD = 5,
	EVENT_KIND_METHOD_ENTRY = 6,
	EVENT_KIND_METHOD_EXIT = 7,
	EVENT_KIND_ASSEMBLY_LOAD = 8,
	EVENT_KIND_ASSEMBLY_UNLOAD = 9,
	EVENT_KIND_BREAKPOINT = 10,
	EVENT_KIND_STEP = 11,
	EVENT_KIND_TYPE_LOAD = 12,
	EVENT_KIND_EXCEPTION = 13,
	EVENT_KIND_KEEPALIVE = 14,
	EVENT_KIND_USER_BREAK = 15,
	EVENT_KIND_USER_LOG = 16
		 */
		static final int VM_START = 0;
		static final int VM_DEATH = 1;
		static final int THREAD_START = 2;
		static final int THREAD_DEATH = 3;
		static final int APPDOMAIN_CREATE = 4;
		static final int APPDOMAIN_UNLOAD = 5;
		static final int METHOD_ENTRY = 6;
		static final int METHOD_EXIT = 7;
		static final int ASSEMBLY_LOAD = 8;
		static final int ASSEMBLY_UNLOAD = 9;
		static final int BREAKPOINT = 10;
		static final int SINGLE_STEP = 11;
		static final int TYPE_LOAD = 12;
		static final int EXCEPTION = 13;
		static final int USER_BREAK = 15;
		static final int USER_LOG = 16;
	}
}