/*
 * Copyright (c) 1999, 2000, Oracle and/or its affiliates. All rights reserved.
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

public class ClassObjectReferenceImpl extends ObjectReferenceImpl
                                      implements ClassObjectReference {
    private ReferenceType reflectedType;

    ClassObjectReferenceImpl(VirtualMachine vm, long ref) {
        super(vm, ref);
    }

    @Override
	public ReferenceType reflectedType() {
        if (reflectedType == null) {
            try {
                JDWP.ClassObjectReference.ReflectedType reply =
                    JDWP.ClassObjectReference.ReflectedType.process(vm, this);
                reflectedType = vm.referenceType(reply.typeID);

            } catch (JDWPException exc) {
                throw exc.toJDIException();
            }
        }
        return reflectedType;
    }

    @Override
	byte typeValueKey() {
        return JDWP.Tag.CLASS_OBJECT;
    }

    @Override
	public String toString() {
        return "instance of " + referenceType().name() +
               "(reflected class=" + reflectedType().name() + ", " + "id=" + uniqueID() + ")";
    }
}
