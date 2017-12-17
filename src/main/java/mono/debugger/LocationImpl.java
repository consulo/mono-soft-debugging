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

import org.jetbrains.annotations.NotNull;
import mono.debugger.protocol.Method_GetDebugInfo;

public class LocationImpl extends MirrorImpl implements Location
{
	private MethodMirror myMethodMirror;
	private long myCodeIndex;

	private boolean myEntryResolved;
	private Method_GetDebugInfo.Entry myEntry;

	public LocationImpl(VirtualMachine vm, MethodMirror method, long codeIndex)
	{
		super(vm);

		myMethodMirror = method;
		myCodeIndex = codeIndex;
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

	@NotNull
	@Override
	public TypeMirror declaringType()
	{
		return myMethodMirror.declaringType();
	}

	@NotNull
	@Override
	public MethodMirror method()
	{
		return myMethodMirror;
	}

	@Override
	public long codeIndex()
	{
		return myCodeIndex;
	}

	@Override
	public String sourcePath()
	{
		Method_GetDebugInfo.Entry entry = debugEntry();
		return entry == null ? null : entry.sourceFile.name;
	}

	@Override
	public int lineNumber()
	{
		Method_GetDebugInfo.Entry entry = debugEntry();
		return entry == null ? -1 : entry.line;
	}

	@Override
	public int columnNumber()
	{
		Method_GetDebugInfo.Entry entry = debugEntry();
		return entry == null ? -1 : entry.column;
	}

	private Method_GetDebugInfo.Entry debugEntry()
	{
		if(myEntryResolved)
		{
			return myEntry;
		}

		myEntryResolved = true;
		if(myCodeIndex == -1)
		{
			return null;
		}

		Method_GetDebugInfo.Entry[] entries = myMethodMirror.debugInfo();

		for(int i = entries.length - 1; i >= 0; --i)
		{
			Method_GetDebugInfo.Entry entry = entries[i];
			if(entry.offset <= myCodeIndex)
			{
				return myEntry = entry;
			}
		}
		return myEntry;
	}

	@Override
	public String toString()
	{
		return "method: " + myMethodMirror + ", codeIndex: " + myCodeIndex;
	}
}
