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

public class ShortValueImpl extends PrimitiveValueImpl
                            implements ShortValue {
    private short value;

    ShortValueImpl(VirtualMachine aVm,short aValue) {
        super(aVm);

        value = aValue;
    }

    @Override
	public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof ShortValue)) {
            return (value == ((ShortValue)obj).value()) &&
                   super.equals(obj);
        } else {
            return false;
        }
    }

    @Override
	public int hashCode() {
        /*
         * TO DO: Better hash code
         */
        return intValue();
    }

    @Override
	public int compareTo(ShortValue obj) {
        short other = obj.value();
        return value() - other;
    }

    @Override
	public Type type() {
        return vm.theShortType();
    }

    @Override
	public short value() {
        return value;
    }

    @Override
	public boolean booleanValue() {
        return(value == 0)?false:true;
    }

    @Override
	public byte byteValue() {
        return(byte)value;
    }

    @Override
	public char charValue() {
        return(char)value;
    }

    @Override
	public short shortValue() {
        return value;
    }

    @Override
	public int intValue() {
        return(int)value;
    }

    @Override
	public long longValue() {
        return(long)value;
    }

    @Override
	public float floatValue() {
        return(float)value;
    }

    @Override
	public double doubleValue() {
        return(double)value;
    }

    @Override
	byte checkedByteValue() throws InvalidTypeException {
        if ((value > Byte.MAX_VALUE) || (value < Byte.MIN_VALUE)) {
            throw new InvalidTypeException("Can't convert " + value + " to byte");
        } else {
            return super.checkedByteValue();
        }
    }

    @Override
	char checkedCharValue() throws InvalidTypeException {
        if ((value > Character.MAX_VALUE) || (value < Character.MIN_VALUE)) {
            throw new InvalidTypeException("Can't convert " + value + " to char");
        } else {
            return super.checkedCharValue();
        }
    }

    @Override
	public String toString() {
        return "" + value;
    }

    @Override
	byte typeValueKey() {
        return JDWP.Tag.SHORT;
    }
}
