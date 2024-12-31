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

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * A point within the executing code of the target VM.
 * Locations are used to identify the current position of
 * a suspended thread (analogous to an instruction pointer or
 * program counter register in native programs). They are also used
 * to identify the position at which to set a breakpoint.
 * <p/>
 * The availability of a line number for a location will
 * depend on the level of debugging information available from the
 * target VM.
 * <p/>
 * Several mirror interfaces have locations. Each such mirror
 * extends a {@link Locatable} interface.
 * <p/>
 * <a name="strata"><b>Strata</b></a>
 * <p/>
 * The source information for a Location is dependent on the
 * <i>stratum</i> which is used. A stratum is a source code
 * level within a sequence of translations.  For example,
 * say the baz program is written in the programming language
 * "Foo" then translated to the language "Bar" and finally
 * translated into the Java programming language.  The
 * Java programming language stratum is named
 * <code>"Java"</code>, let's say the other strata are named
 * "Foo" and "Bar".  A given location (as viewed by the
 * {@link #sourceName()} and {@link #lineNumber()} methods)
 * might be at line 14 of "baz.foo" in the <code>"Foo"</code>
 * stratum, line 23 of "baz.bar" in the <code>"Bar"</code>
 * stratum and line 71 of the <code>"Java"</code> stratum.
 * Note that while the Java programming language may have
 * only one source file for a reference type, this restriction
 * does not apply to other strata - thus each Location should
 * be consulted to determine its source path.
 * Queries which do not specify a stratum
 * ({@link #sourceName()}, {@link #sourcePath()} and
 * {@link #lineNumber()}) use the VM's default stratum
 * ({@link VirtualMachine#getDefaultStratum()}).
 * If the specified stratum (whether explicitly specified
 * by a method parameter or implicitly as the VM's default)
 * is <code>null</code> or is not available in the declaring
 * type, the declaring type's default stratum is used
 * ({@link #declaringType()}.{@link ReferenceType#defaultStratum()
 * defaultStratum()}).  Note that in the normal case, of code
 * that originates as Java programming language source, there
 * will be only one stratum (<code>"Java"</code>) and it will be
 * returned as the default.  To determine the available strata
 * use {@link ReferenceType#availableStrata()}.
 *
 * @author Robert Field
 * @author Gordon Hirsch
 * @author James McIlree
 * @see mono.debugger.request.EventRequestManager
 * @see StackFrameMirror
 * @see mono.debugger.event.BreakpointEvent
 * @see mono.debugger.event.ExceptionEvent
 * @see Locatable
 * @since 1.3
 */
public interface Location extends Mirror
{
	@Nonnull
	TypeMirror declaringType();

	@Nonnull
	MethodMirror method();

	long codeIndex();

	@Nullable
	String sourcePath();

	int lineNumber();

	int columnNumber();
}
