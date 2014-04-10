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

import javax.lang.model.type.PrimitiveType;

public class ArrayTypeImpl extends ReferenceTypeImpl
    implements ArrayType
{
    protected ArrayTypeImpl(VirtualMachine aVm, long aRef) {
        super(aVm, aRef);
    }

    @Override
	public ArrayReference newInstance(int length) {
        try {
            return (ArrayReference)JDWP.ArrayType.NewInstance.
                                       process(vm, this, length).newArray;
        } catch (JDWPException exc) {
            throw exc.toJDIException();
        }
    }

    @Override
	public String componentSignature() {
        return signature().substring(1); // Just skip the leading '['
    }

    @Override
	public String componentTypeName() {
        JNITypeParser parser = new JNITypeParser(componentSignature());
        return parser.typeName();
    }

    Type type() throws ClassNotLoadedException {
        return findType(componentSignature());
    }

    @Override
	void addVisibleMethods(Map<String, MethodMirror> map) {
        // arrays don't have methods
    }

    @Override
	public List<MethodMirror> allMethods() {
        return new ArrayList<MethodMirror>(0);   // arrays don't have methods
    }

    /*
     * Find the type object, if any, of a component type of this array.
     * The component type does not have to be immediate; e.g. this method
     * can be used to find the component Foo of Foo[][]. This method takes
     * advantage of the property that an array and its component must have
     * the same class loader. Since array set operations don't have an
     * implicit enclosing type like field and variable set operations,
     * this method is sometimes needed for proper type checking.
     */
    Type findComponentType(String signature) throws ClassNotLoadedException {
        return null;
    }

    @Override
	public Type componentType() throws ClassNotLoadedException {
        return findComponentType(componentSignature());
    }

    static boolean isComponentAssignable(Type destination, Type source) {
        if (source instanceof PrimitiveType) {
            // Assignment of primitive arrays requires identical
            // component types.
            return source.equals(destination);
        } else {
            if (destination instanceof PrimitiveType) {
                return false;
            }

            ReferenceTypeImpl refSource = (ReferenceTypeImpl)source;
            ReferenceTypeImpl refDestination = (ReferenceTypeImpl)destination;
            // Assignment of object arrays requires availability
            // of widening conversion of component types
            return refSource.isAssignableTo(refDestination);
        }
    }

    /*
     * Return true if an instance of the  given reference type
     * can be assigned to a variable of this type
     */
	@Override
	boolean isAssignableTo(ReferenceType destType) {
        if (destType instanceof ArrayType) {
            try {
                Type destComponentType = ((ArrayType)destType).componentType();
                return isComponentAssignable(destComponentType, componentType());
            } catch (ClassNotLoadedException e) {
                // One or both component types has not yet been
                // loaded => can't assign
                return false;
            }
        }
		else {
            // Only valid ClassType assignee is Object
            return destType.name().equals("java.lang.Object");
        }
    }

    @Override
	List<ReferenceType> inheritedTypes() {
        return new ArrayList<ReferenceType>(0);
    }

    @Override
	void getModifiers() {
        if (modifiers != -1) {
            return;
        }
        /*
         * For object arrays, the return values for Interface
         * Accessible.isPrivate(), Accessible.isProtected(),
         * etc... are the same as would be returned for the
         * component type.  Fetch the modifier bits from the
         * component type and use those.
         *
         * For primitive arrays, the modifiers are always
         *   VMModifiers.FINAL | VMModifiers.PUBLIC
         *
         * Reference mono.debugger.Accessible.java.
         */
        try {
            Type t = componentType();
            {
                ReferenceType rt = (ReferenceType)t;
                modifiers = rt.modifiers();
            }
        } catch (ClassNotLoadedException cnle) {
            cnle.printStackTrace();
        }
    }

    @Override
	public String toString() {
       return "array class " + name();
    }

    /*
     * Save a pointless trip over the wire for these methods
     * which have undefined results for arrays.
     */
    @Override
	public boolean isPrepared() { return true; }
    @Override
	public boolean isVerified() { return true; }
    @Override
	public boolean isInitialized() { return true; }
    @Override
	public boolean failedToInitialize() { return false; }
    @Override
	public boolean isAbstract() { return false; }

    /*
     * Defined always to be true for arrays
     */
    @Override
	public boolean isFinal() { return true; }
}
