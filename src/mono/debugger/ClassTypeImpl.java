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
import java.util.List;
import java.util.Map;

public class ClassTypeImpl extends ReferenceTypeImpl
    implements TypeMirrorOld
{
    private boolean cachedSuperclass = false;
    private TypeMirrorOld superclass = null;
    private int lastLine = -1;
    private List<TypeMirrorOld> interfaces = null;

    protected ClassTypeImpl(VirtualMachine aVm,long aRef) {
        super(aVm, aRef);
    }

    @Override
	public TypeMirrorOld superclass() {
        if(!cachedSuperclass)  {
            ClassTypeImpl sup = null;
            try {
                sup = JDWP.ClassType.Superclass.
                    process(vm, this).superclass;
            } catch (JDWPException exc) {
                throw exc.toJDIException();
            }

            /*
             * If there is a superclass, cache its
             * ClassType here. Otherwise,
             * leave the cache reference null.
             */
            if (sup != null) {
                superclass = sup;
            }
            cachedSuperclass = true;
        }

        return superclass;
    }

    @Override
	public List<TypeMirrorOld> subclasses() {
        List<TypeMirrorOld> subs = new ArrayList<TypeMirrorOld>();


        return subs;
    }

    @Override
	public boolean isEnum() {
        TypeMirrorOld superclass = superclass();
        if (superclass != null &&
            superclass.name().equals("java.lang.Enum")) {
            return true;
        }
        return false;
    }

    @Override
	public void setValue(Field field, Value value)
        throws InvalidTypeException, ClassNotLoadedException {

        validateMirror(field);
        validateMirrorOrNull(value);
        validateFieldSet(field);

        // More validation specific to setting from a ClassType
        if(!field.isStatic()) {
            throw new IllegalArgumentException(
                            "Must set non-static field through an instance");
        }

        try {
            JDWP.ClassType.SetValues.FieldValue[] values =
                          new JDWP.ClassType.SetValues.FieldValue[1];
            values[0] = new JDWP.ClassType.SetValues.FieldValue(
                    ((FieldImpl)field).ref(),
                    // validate and convert if necessary
                    ValueImpl.prepareForAssignment(value, (FieldImpl)field));

            try {
                JDWP.ClassType.SetValues.process(vm, this, values);
            } catch (JDWPException exc) {
                throw exc.toJDIException();
            }
        } catch (ClassNotLoadedException e) {
            /*
             * Since we got this exception,
             * the field type must be a reference type. The value
             * we're trying to set is null, but if the field's
             * class has not yet been loaded through the enclosing
             * class loader, then setting to null is essentially a
             * no-op, and we should allow it without an exception.
             */
            if (value != null) {
                throw e;
            }
        }
    }

    PacketStream sendInvokeCommand(final ThreadMirror thread,
                                   final MethodImpl method,
                                   final ValueImpl[] args,
                                   final int options) {
        CommandSender sender =
            new CommandSender() {
                @Override
				public PacketStream send() {
                    return JDWP.ClassType.InvokeMethod.enqueueCommand(
                                          vm, ClassTypeImpl.this, thread,
                                          method.ref(), args, options);
                }
        };

        PacketStream stream;
        if ((options & INVOKE_SINGLE_THREADED) != 0) {
            stream = thread.sendResumingCommand(sender);
        } else {
            stream = vm.sendResumingCommand(sender);
        }
        return stream;
    }

    PacketStream sendNewInstanceCommand(final ThreadMirror thread,
                                   final MethodImpl method,
                                   final ValueImpl[] args,
                                   final int options) {
        CommandSender sender =
            new CommandSender() {
                @Override
				public PacketStream send() {
                    return JDWP.ClassType.NewInstance.enqueueCommand(
                                          vm, ClassTypeImpl.this, thread,
                                          method.ref(), args, options);
                }
        };

        PacketStream stream;
        if ((options & INVOKE_SINGLE_THREADED) != 0) {
            stream = thread.sendResumingCommand(sender);
        } else {
            stream = vm.sendResumingCommand(sender);
        }
        return stream;
    }

    @Override
	public Value invokeMethod(ThreadMirror threadIntf, MethodMirrorOld methodIntf,
                              List<? extends Value> origArguments, int options)
                                   throws InvalidTypeException,
                                          ClassNotLoadedException,
                                          IncompatibleThreadStateException,
                                          InvocationException {
        validateMirror(threadIntf);
        validateMirror(methodIntf);
        validateMirrorsOrNulls(origArguments);

        MethodImpl method = (MethodImpl)methodIntf;
        ThreadMirror thread = (ThreadMirror)threadIntf;

        validateMethodInvocation(method);

        List<? extends Value> arguments = method.validateAndPrepareArgumentsForInvoke(origArguments);

        ValueImpl[] args = arguments.toArray(new ValueImpl[0]);
        JDWP.ClassType.InvokeMethod ret;
        try {
            PacketStream stream =
                sendInvokeCommand(thread, method, args, options);
            ret = JDWP.ClassType.InvokeMethod.waitForReply(vm, stream);
        } catch (JDWPException exc) {
            if (exc.errorCode() == JDWP.Error.INVALID_THREAD) {
                throw new IncompatibleThreadStateException();
            } else {
                throw exc.toJDIException();
            }
        }

        /*
         * There is an implict VM-wide suspend at the conclusion
         * of a normal (non-single-threaded) method invoke
         */
        if ((options & INVOKE_SINGLE_THREADED) == 0) {
            vm.notifySuspend();
        }

        if (ret.exception != null) {
            throw new InvocationException(ret.exception);
        } else {
            return ret.returnValue;
        }
    }

    @Override
	public ObjectReference newInstance(ThreadMirror threadIntf,
                                       MethodMirrorOld methodIntf,
                                       List<? extends Value> origArguments,
                                       int options)
                                   throws InvalidTypeException,
                                          ClassNotLoadedException,
                                          IncompatibleThreadStateException,
                                          InvocationException {
        validateMirror(threadIntf);
        validateMirror(methodIntf);
        validateMirrorsOrNulls(origArguments);

        MethodImpl method = (MethodImpl)methodIntf;
        ThreadMirror thread = (ThreadMirror)threadIntf;

        validateConstructorInvocation(method);

        List<Value> arguments = method.validateAndPrepareArgumentsForInvoke(
                                                       origArguments);
        ValueImpl[] args = arguments.toArray(new ValueImpl[0]);
        JDWP.ClassType.NewInstance ret = null;
        try {
            PacketStream stream =
                sendNewInstanceCommand(thread, method, args, options);
            ret = JDWP.ClassType.NewInstance.waitForReply(vm, stream);
        } catch (JDWPException exc) {
            if (exc.errorCode() == JDWP.Error.INVALID_THREAD) {
                throw new IncompatibleThreadStateException();
            } else {
                throw exc.toJDIException();
            }
        }

        /*
         * There is an implict VM-wide suspend at the conclusion
         * of a normal (non-single-threaded) method invoke
         */
        if ((options & INVOKE_SINGLE_THREADED) == 0) {
            vm.notifySuspend();
        }

        if (ret.exception != null) {
            throw new InvocationException(ret.exception);
        } else {
            return ret.newObject;
        }
    }

    @Override
	public MethodMirrorOld concreteMethodByName(String name, String signature)  {
       MethodMirrorOld method = null;
       for (MethodMirrorOld candidate : visibleMethods()) {
           if (candidate.name().equals(name) &&
               candidate.signature().equals(signature) &&
               !candidate.isAbstract()) {

               method = candidate;
               break;
           }
       }
       return method;
   }

   @Override
   public List<MethodMirrorOld> allMethods() {
        ArrayList<MethodMirrorOld> list = new ArrayList<MethodMirrorOld>(methods());

        TypeMirrorOld clazz = superclass();
        while (clazz != null) {
            list.addAll(clazz.methods());
            clazz = clazz.superclass();
        }
        return list;
    }

    @Override
	List<ReferenceType> inheritedTypes() {
        List<ReferenceType> inherited = new ArrayList<ReferenceType>();
        if (superclass() != null) {
            inherited.add(0, (ReferenceType)superclass()); /* insert at front */
        }

        return inherited;
    }

    void validateMethodInvocation(MethodMirrorOld method)
                                   throws InvalidTypeException,
                                          InvocationException {
        /*
         * Method must be in this class or a superclass.
         */
        ReferenceTypeImpl declType = (ReferenceTypeImpl)method.declaringType();
        if (!declType.isAssignableFrom(this)) {
            throw new IllegalArgumentException("Invalid method");
        }

        /*
         * Method must be a static and not a static initializer
         */
        if (!method.isStatic()) {
            throw new IllegalArgumentException("Cannot invoke instance method on a class type");
        } else if (method.isStaticInitializer()) {
            throw new IllegalArgumentException("Cannot invoke static initializer");
        }
    }

    void validateConstructorInvocation(MethodMirrorOld method)
                                   throws InvalidTypeException,
                                          InvocationException {
        /*
         * Method must be in this class.
         */
        ReferenceTypeImpl declType = (ReferenceTypeImpl)method.declaringType();
        if (!declType.equals(this)) {
            throw new IllegalArgumentException("Invalid constructor");
        }

        /*
         * Method must be a constructor
         */
        if (!method.isConstructor()) {
            throw new IllegalArgumentException("Cannot create instance with non-constructor");
        }
    }

    @Override
	void addVisibleMethods(Map<String, MethodMirrorOld> methodMap) {
        /*
         * Add methods from
         * parent types first, so that the methods in this class will
         * overwrite them in the hash table
         */


        ClassTypeImpl clazz = (ClassTypeImpl)superclass();
        if (clazz != null) {
            clazz.addVisibleMethods(methodMap);
        }

        addToMethodMap(methodMap, methods());
    }

    @Override
	boolean isAssignableTo(ReferenceType type) {
        ClassTypeImpl superclazz = (ClassTypeImpl)superclass();
        if (this.equals(type)) {
            return true;
        } else if ((superclazz != null) && superclazz.isAssignableTo(type)) {
            return true;
        } else {

            return false;
        }
    }

    @Override
	public String toString() {
       return "class " + name();
    }
}
