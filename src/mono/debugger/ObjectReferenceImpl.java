/*
 * Copyright (c) 1998, 2011, Oracle and/or its affiliates. All rights reserved.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectReferenceImpl extends ValueImpl implements ObjectReference, VMListener, MirrorWithId
{
	protected long ref;

	private int gcDisableCount = 0;
	boolean addedListener = false;


	// Return the ClassTypeImpl upon which to invoke a method.
	// By default it is our very own referenceType() but subclasses
	// can override.
	protected ClassTypeImpl invokableReferenceType(MethodMirrorOld method)
	{
		return (ClassTypeImpl) referenceType();
	}

	public ObjectReferenceImpl(VirtualMachine aVm, long aRef)
	{
		super(aVm);

		ref = aRef;
	}

	protected String description()
	{
		return "ObjectReference " + uniqueID();
	}

	/*
	 * VMListener implementation
	 */
	@Override
	public boolean vmSuspended(VMAction action)
	{
		return true;
	}

	@Override
	public boolean vmNotSuspended(VMAction action)
	{
		// make sure that cache and listener management are synchronized
		synchronized(vm.state())
		{
			if(addedListener)
			{
				/*
                 * If a listener was added (i.e. this is not a
                 * ObjectReference that adds a listener on startup),
                 * remove it here.
                 */
				addedListener = false;
				return false;  // false says remove
			}
			else
			{
				return true;
			}
		}
	}

	@Override
	public boolean equals(Object obj)
	{
		if((obj != null) && (obj instanceof ObjectReferenceImpl))
		{
			ObjectReferenceImpl other = (ObjectReferenceImpl) obj;
			return (ref() == other.ref()) && super.equals(obj);
		}
		else
		{
			return false;
		}
	}

	@Override
	public int hashCode()
	{
		return (int) ref();
	}

	@Override
	public Type type()
	{
		return referenceType();
	}

	@Override
	public ReferenceType referenceType()
	{
		return null;
	}

	@Override
	public Value getValue(Field sig)
	{
		List<Field> list = new ArrayList<Field>(1);
		list.add(sig);
		Map<Field, Value> map = getValues(list);
		return map.get(sig);
	}

	@Override
	public Map<Field, Value> getValues(List<? extends Field> theFields)
	{
		validateMirrors(theFields);

		List<Field> staticFields = new ArrayList<Field>(0);
		int size = theFields.size();
		List<Field> instanceFields = new ArrayList<Field>(size);

		for(int i = 0; i < size; i++)
		{
			Field field = (Field) theFields.get(i);

			// Make sure the field is valid
			((ReferenceTypeImpl) referenceType()).validateFieldAccess(field);

			// FIX ME! We need to do some sanity checking
			// here; make sure the field belongs to this
			// object.
			if(field.isStatic())
			{
				staticFields.add(field);
			}
			else
			{
				instanceFields.add(field);
			}
		}

		Map<Field, Value> map;
		if(staticFields.size() > 0)
		{
			map = referenceType().getValues(staticFields);
		}
		else
		{
			map = new HashMap<Field, Value>(size);
		}

		size = instanceFields.size();

		JDWP.ObjectReference.GetValues.Field[] queryFields = new JDWP.ObjectReference.GetValues.Field[size];
		for(int i = 0; i < size; i++)
		{
			FieldImpl field = (FieldImpl) instanceFields.get(i);/* thanks OTI */
			queryFields[i] = new JDWP.ObjectReference.GetValues.Field(field.ref());
		}
		ValueImpl[] values;
		try
		{
			values = JDWP.ObjectReference.GetValues.
					process(vm, this, queryFields).values;
		}
		catch(JDWPException exc)
		{
			throw exc.toJDIException();
		}

		if(size != values.length)
		{
			throw new InternalException("Wrong number of values returned from target VM");
		}
		for(int i = 0; i < size; i++)
		{
			FieldImpl field = (FieldImpl) instanceFields.get(i);
			map.put(field, values[i]);
		}

		return map;
	}

	@Override
	public void setValue(Field field, Value value) throws InvalidTypeException, ClassNotLoadedException
	{

		validateMirror(field);
		validateMirrorOrNull(value);

		// Make sure the field is valid
		((ReferenceTypeImpl) referenceType()).validateFieldSet(field);

		if(field.isStatic())
		{
			ReferenceType type = referenceType();
			if(type instanceof TypeMirrorOld)
			{
				((TypeMirrorOld) type).setValue(field, value);
				return;
			}
			else
			{
				throw new IllegalArgumentException("Invalid type for static field set");
			}
		}

		try
		{
			JDWP.ObjectReference.SetValues.FieldValue[] fvals = new JDWP.ObjectReference.SetValues.FieldValue[1];
			fvals[0] = new JDWP.ObjectReference.SetValues.FieldValue(((FieldImpl) field).ref(),
					// Validate and convert if necessary
					ValueImpl.prepareForAssignment(value, (FieldImpl) field));
			try
			{
				JDWP.ObjectReference.SetValues.process(vm, this, fvals);
			}
			catch(JDWPException exc)
			{
				throw exc.toJDIException();
			}
		}
		catch(ClassNotLoadedException e)
		{
            /*
             * Since we got this exception,
             * the field type must be a reference type. The value
             * we're trying to set is null, but if the field's
             * class has not yet been loaded through the enclosing
             * class loader, then setting to null is essentially a
             * no-op, and we should allow it without an exception.
             */
			if(value != null)
			{
				throw e;
			}
		}
	}

	void validateMethodInvocation(MethodMirrorOld method, int options) throws InvalidTypeException, InvocationException
	{

        /*
         * Method must be in this object's class, a superclass, or
         * implemented interface
         */
		ReferenceTypeImpl declType = (ReferenceTypeImpl) method.declaringType();
		if(!declType.isAssignableFrom(this))
		{
			throw new IllegalArgumentException("Invalid method");
		}

		ClassTypeImpl clazz = invokableReferenceType(method);

        /*
         * Method must be a non-constructor
         */
		if(method.isConstructor())
		{
			throw new IllegalArgumentException("Cannot invoke constructor");
		}

        /*
         * For nonvirtual invokes, method must have a body
         */
		if((options & INVOKE_NONVIRTUAL) != 0)
		{
			if(method.isAbstract())
			{
				throw new IllegalArgumentException("Abstract method");
			}
		}

        /*
         * Get the class containing the method that will be invoked.
         * This class is needed only for proper validation of the
         * method argument types.
         */
		ClassTypeImpl invokedClass;
		if((options & INVOKE_NONVIRTUAL) != 0)
		{
			// No overrides in non-virtual invokes
			invokedClass = clazz;
		}
		else
		{
            /*
             * For virtual invokes, find any override of the method.
             * Since we are looking for a method with a real body, we
             * don't need to bother with interfaces/abstract methods.
             */
			MethodMirrorOld invoker = clazz.concreteMethodByName(method.name(), method.signature());
			//  isAssignableFrom check above guarantees non-null
			invokedClass = (ClassTypeImpl) invoker.declaringType();
		}
        /* The above code is left over from previous versions.
         * We haven't had time to divine the intent.  jjh, 7/31/2003
         */
	}

	PacketStream sendInvokeCommand(
			final ThreadMirror thread, final ClassTypeImpl refType, final MethodImpl method, final ValueImpl[] args, final int options)
	{
		CommandSender sender = new CommandSender()
		{
			@Override
			public PacketStream send()
			{
				return JDWP.ObjectReference.InvokeMethod.enqueueCommand(vm, ObjectReferenceImpl.this, thread, refType, method.ref(), args, options);
			}
		};

		PacketStream stream;
		if((options & INVOKE_SINGLE_THREADED) != 0)
		{
			stream = thread.sendResumingCommand(sender);
		}
		else
		{
			stream = vm.sendResumingCommand(sender);
		}
		return stream;
	}

	@Override
	public Value invokeMethod(
			ThreadMirror threadIntf,
			MethodMirrorOld methodIntf,
			List<? extends Value> origArguments,
			int options) throws InvalidTypeException, IncompatibleThreadStateException, InvocationException, ClassNotLoadedException
	{
		validateMirror(threadIntf);
		validateMirror(methodIntf);
		validateMirrorsOrNulls(origArguments);

		MethodImpl method = (MethodImpl) methodIntf;
		ThreadMirror thread = (ThreadMirror) threadIntf;

		if(method.isStatic())
		{
			if(referenceType() instanceof TypeMirrorOld)
			{
				TypeMirrorOld type = (TypeMirrorOld) referenceType();
				return type.invokeMethod(thread, method, origArguments, options);
			}
			else
			{
				throw new IllegalArgumentException("Invalid type for static method invocation");
			}
		}

		validateMethodInvocation(method, options);

		List<Value> arguments = method.validateAndPrepareArgumentsForInvoke(origArguments);

		ValueImpl[] args = arguments.toArray(new ValueImpl[0]);
		JDWP.ObjectReference.InvokeMethod ret;
		try
		{
			PacketStream stream = sendInvokeCommand(thread, invokableReferenceType(method), method, args, options);
			ret = JDWP.ObjectReference.InvokeMethod.waitForReply(vm, stream);
		}
		catch(JDWPException exc)
		{
			if(exc.errorCode() == JDWP.Error.INVALID_THREAD)
			{
				throw new IncompatibleThreadStateException();
			}
			else
			{
				throw exc.toJDIException();
			}
		}

        /*
         * There is an implict VM-wide suspend at the conclusion
         * of a normal (non-single-threaded) method invoke
         */
		if((options & INVOKE_SINGLE_THREADED) == 0)
		{
			vm.notifySuspend();
		}

		if(ret.exception != null)
		{
			throw new InvocationException(ret.exception);
		}
		else
		{
			return ret.returnValue;
		}
	}

	/* leave synchronized to keep count accurate */
	@Override
	public synchronized void disableCollection()
	{
		if(gcDisableCount == 0)
		{
			try
			{
				JDWP.ObjectReference.DisableCollection.process(vm, this);
			}
			catch(JDWPException exc)
			{
				throw exc.toJDIException();
			}
		}
		gcDisableCount++;
	}

	/* leave synchronized to keep count accurate */
	@Override
	public synchronized void enableCollection()
	{
		gcDisableCount--;

		if(gcDisableCount == 0)
		{
			try
			{
				JDWP.ObjectReference.EnableCollection.process(vm, this);
			}
			catch(JDWPException exc)
			{
				// If already collected, no harm done, no exception
				if(exc.errorCode() != JDWP.Error.INVALID_OBJECT)
				{
					throw exc.toJDIException();
				}
				return;
			}
		}
	}

	@Override
	public boolean isCollected()
	{
		try
		{
			return JDWP.ObjectReference.IsCollected.process(vm, this).
					isCollected;
		}
		catch(JDWPException exc)
		{
			throw exc.toJDIException();
		}
	}

	@Override
	public long uniqueID()
	{
		return ref();
	}


	@Override
	public List<ObjectReference> referringObjects(long maxReferrers)
	{

		if(maxReferrers < 0)
		{
			throw new IllegalArgumentException("maxReferrers is less than zero: " + maxReferrers);
		}

		int intMax = (maxReferrers > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) maxReferrers;
		// JDWP can't currently handle more than this (in mustang)

		try
		{
			return Arrays.asList((ObjectReference[]) JDWP.ObjectReference.ReferringObjects.
					process(vm, this, intMax).referringObjects);
		}
		catch(JDWPException exc)
		{
			throw exc.toJDIException();
		}
	}

	public long ref()
	{
		return ref;
	}

	@Override
	public long id()
	{
		return ref;
	}

	boolean isClassObject()
	{
        /*
         * Don't need to worry about subclasses since java.lang.Class is final.
         */
		return referenceType().name().equals("java.lang.Class");
	}

	@Override
	ValueImpl prepareForAssignmentTo(ValueContainer destination) throws InvalidTypeException, ClassNotLoadedException
	{

		validateAssignment(destination);
		return this;            // conversion never necessary
	}

	void validateAssignment(ValueContainer destination) throws InvalidTypeException, ClassNotLoadedException
	{

        /*
         * Do these simpler checks before attempting a query of the destination's
         * type which might cause a confusing ClassNotLoadedException if
         * the destination is primitive or an array.
         */
        /*
         * TO DO: Centralize JNI signature knowledge
         */
		if(destination.signature().length() == 1)
		{
			throw new InvalidTypeException("Can't assign object value to primitive");
		}
		if((destination.signature().charAt(0) == '[') && (type().signature().charAt(0) != '['))
		{
			throw new InvalidTypeException("Can't assign non-array value to an array");
		}
		if("void".equals(destination.typeName()))
		{
			throw new InvalidTypeException("Can't assign object value to a void");
		}

		// Validate assignment
		ReferenceType destType = (ReferenceTypeImpl) destination.type();
		ReferenceTypeImpl myType = (ReferenceTypeImpl) referenceType();
		if(!myType.isAssignableTo(destType))
		{
			JNITypeParser parser = new JNITypeParser(destType.signature());
			String destTypeName = parser.typeName();
			throw new InvalidTypeException("Can't assign " +
					type().name() +
					" to " + destTypeName);
		}
	}


	@Override
	public String toString()
	{
		ReferenceType referenceType = referenceType();
		return "instance of " + (referenceType == null ? null : referenceType.name()) + "(id=" + uniqueID() + ")";
	}

	@Override
	int typeValueKey()
	{
		return JDWP.Tag.OBJECT;
	}
}
