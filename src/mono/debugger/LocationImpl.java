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
	private final ReferenceTypeImpl declaringType;
	private Method method;
	private long methodRef;
	private long codeIndex;
	private LineInfo baseLineInfo = null;
	private LineInfo otherLineInfo = null;

	public LocationImpl(VirtualMachine vm, Method method, long codeIndex)
	{
		super(vm);

		this.method = method;
		this.codeIndex = method.isNative() ? -1 : codeIndex;
		this.declaringType = (ReferenceTypeImpl) method.declaringType();
	}

	/*
	 * This constructor allows lazy creation of the method mirror. This
	 * can be a performance savings if the method mirror does not yet
	 * exist.
	 */
	public LocationImpl(VirtualMachine vm, ReferenceTypeImpl declaringType, long methodRef, long codeIndex)
	{
		super(vm);

		this.method = null;
		this.codeIndex = codeIndex;
		this.declaringType = declaringType;
		this.methodRef = methodRef;
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
		/*
         * TO DO: better hash code?
         */
		return method().hashCode() + (int) codeIndex();
	}

	@Override
	public int compareTo(Location object)
	{
		LocationImpl other = (LocationImpl) object;
		int rc = method().compareTo(other.method());
		if(rc == 0)
		{
			long diff = codeIndex() - other.codeIndex();
			if(diff < 0)
			{
				return -1;
			}
			else if(diff > 0)
			{
				return 1;
			}
			else
			{
				return 0;
			}
		}
		return rc;
	}

	@Override
	public ReferenceType declaringType()
	{
		return declaringType;
	}

	@Override
	public Method method()
	{
		if(method == null)
		{
			method = declaringType.getMethodMirror(methodRef);
			if(method.isNative())
			{
				codeIndex = -1;
			}
		}
		return method;
	}

	@Override
	public long codeIndex()
	{
		method();  // be sure information is up-to-date
		return codeIndex;
	}

	LineInfo getBaseLineInfo()
	{
		LineInfo lineInfo;

        /* check if there is cached info to use */
		if(baseLineInfo != null)
		{
			return baseLineInfo;
		}

        /* compute the line info */
		MethodImpl methodImpl = (MethodImpl) method();
		lineInfo = methodImpl.codeIndexToLineInfo(codeIndex());

        /* cache it */
		addBaseLineInfo(lineInfo);

		return lineInfo;
	}

	LineInfo getLineInfo()
	{
		return getBaseLineInfo();

	}

	void addStratumLineInfo(LineInfo lineInfo)
	{
		otherLineInfo = lineInfo;
	}

	void addBaseLineInfo(LineInfo lineInfo)
	{
		baseLineInfo = lineInfo;
	}

	@Override
	public String sourceName() throws AbsentInformationException
	{
		return sourceName0();
	}

	String sourceName0() throws AbsentInformationException
	{
		return getLineInfo().liSourceName();
	}

	@Override
	public String sourcePath() throws AbsentInformationException
	{
		return sourcePath0();
	}

	String sourcePath0() throws AbsentInformationException
	{
		return getLineInfo().liSourcePath();
	}

	@Override
	public int lineNumber()
	{
		return lineNumber0();
	}

	int lineNumber0()
	{
		return getLineInfo().liLineNumber();
	}

	@Override
	public String toString()
	{
		if(lineNumber() == -1)
		{
			return method().toString() + "+" + codeIndex();
		}
		else
		{
			return declaringType().name() + ":" + lineNumber();
		}
	}
}
