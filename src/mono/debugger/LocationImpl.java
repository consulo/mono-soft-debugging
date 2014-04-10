/*
 * Copyright (c) 1998, 2004, Oracle and/or its affiliates. All rights reserved.
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

public class LocationImpl extends MirrorImpl implements Location
{
	private MethodMirror method;
	private long codeIndex;

	public LocationImpl(VirtualMachine vm, MethodMirror method, long codeIndex)
	{
		super(vm);

		this.method = method;
		this.codeIndex = codeIndex;
	}

	@Override
	public boolean equals(Object obj)
	{
		if((obj != null) && (obj instanceof Location))
		{
			Location other = (Location) obj;
			return (method().equals(other.method())) &&
					(codeIndex() == other.codeIndex()) &&
					super.equals(obj);
		}
		else
		{
			return false;
		}
	}

	@Override
	public int hashCode()
	{
		return method().hashCode() + (int) codeIndex();
	}


	@Override
	public TypeMirror declaringType()
	{
		return method.declaringType();
	}

	@Override
	public MethodMirror method()
	{
		return method;
	}

	@Override
	public long codeIndex()
	{
		return codeIndex;
	}

	@Override
	public String sourceName() throws AbsentInformationException
	{
		return "";
	}

	@Override
	public String sourcePath() throws AbsentInformationException
	{
		return "";
	}

	@Override
	public int lineNumber()
	{
		return 0;
	}

	@Override
	public String toString()
	{
		return "method: " + method + ", codeIndex: " + codeIndex;
	}
}
