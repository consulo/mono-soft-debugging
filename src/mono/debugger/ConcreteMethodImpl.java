/*
 * Copyright (c) 2000, 2011, Oracle and/or its affiliates. All rights reserved.
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

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Represents methods with method bodies.
 * That is, non-native non-abstract methods.
 * Private to MethodImpl.
 */
public class ConcreteMethodImpl extends MethodImpl {

    /*
     * A subset of the line number info that is softly cached
     */
    static private class SoftLocationXRefs {
        final String stratumID;   // The stratum of this information
        final Map<Integer, List<Location>> lineMapper;     // Maps line number to location(s)
        final List<Location> lineLocations; // List of locations ordered by code index

        /*
         * Note: these do not necessarily correspond to
         * the line numbers of the first and last elements
         * in the lineLocations list. Use these only for bounds
         * checking and with lineMapper.
         */
        final int lowestLine;
        final int highestLine;

        SoftLocationXRefs(String stratumID, Map<Integer, List<Location>> lineMapper, List<Location> lineLocations,
                     int lowestLine, int highestLine) {
            this.stratumID = stratumID;
            this.lineMapper = Collections.unmodifiableMap(lineMapper);
            this.lineLocations =
                Collections.unmodifiableList(lineLocations);
            this.lowestLine = lowestLine;
            this.highestLine = highestLine;
        }
    }

    private Location location = null;
    private SoftReference<SoftLocationXRefs> softBaseLocationXRefsRef;
    private SoftReference<SoftLocationXRefs> softOtherLocationXRefsRef;
    private SoftReference<List<LocalVariable>> variablesRef = null;
    private boolean absentVariableInformation = false;
    private long firstIndex = -1;
    private long lastIndex = -1;
    private SoftReference<byte[]> bytecodesRef = null;
    private int argSlotCount = -1;

    ConcreteMethodImpl(VirtualMachine vm, ReferenceTypeImpl declaringType,
                       long ref,
                       String name, String signature,
                       String genericSignature, int modifiers) {

        // The generic signature is set when this is created
        super(vm, declaringType, ref, name, signature,
              genericSignature, modifiers);
    }

    @Override
	public Location location() {
        if (location == null) {
            getBaseLocations();
        }
        return location;
    }

    List<Location> sourceNameFilter(List<Location> list,

                          String sourceName)
                            throws AbsentInformationException {
        if (sourceName == null) {
            return list;
        } else {
            /* needs sourceName filteration */
            List<Location> locs = new ArrayList<Location>();
            for (Location loc : list) {
                if (((LocationImpl)loc).sourceName().equals(sourceName)) {
                    locs.add(loc);
                }
            }
            return locs;
        }
    }

    @Override
	List<Location> allLineLocations0(String sourceName)
                            throws AbsentInformationException {
        List<Location> lineLocations = getLocations().lineLocations;

        if (lineLocations.size() == 0) {
            throw new AbsentInformationException();
        }

        return Collections.unmodifiableList(
          sourceNameFilter(lineLocations,  sourceName));
    }

    @Override
	List<Location> locationsOfLine0(
                         String sourceName,
                         int lineNumber)
                            throws AbsentInformationException {
        SoftLocationXRefs info = getLocations();

        if (info.lineLocations.size() == 0) {
            throw new AbsentInformationException();
        }

        /*
         * Find the locations which match the line number
         * passed in.
         */
        List<Location> list = info.lineMapper.get(new Integer(lineNumber));

        if (list == null) {
            list = new ArrayList<Location>(0);
        }
        return Collections.unmodifiableList(
          sourceNameFilter(list, sourceName));
    }


    @Override
	public Location locationOfCodeIndex(long codeIndex) {
        if (firstIndex == -1) {
            getBaseLocations();
        }

        /*
         * Check for invalid code index.
         */
        if (codeIndex < firstIndex || codeIndex > lastIndex) {
            return null;
        }

        return new LocationImpl(virtualMachine(), this, codeIndex);
    }


    @Override
	LineInfo codeIndexToLineInfo(long codeIndex) {
        if (firstIndex == -1) {
            getBaseLocations();
        }

        /*
         * Check for invalid code index.
         */
        if (codeIndex < firstIndex || codeIndex > lastIndex) {
            throw new InternalError(
                    "Location with invalid code index");
        }

        List<Location> lineLocations = getLocations().lineLocations;

        /*
         * Check for absent line numbers.
         */
        if (lineLocations.size() == 0) {
            return super.codeIndexToLineInfo(codeIndex);
        }

        Iterator<Location> iter = lineLocations.iterator();
        /*
         * Treat code before the beginning of the first line table
         * entry as part of the first line.  javac will generate
         * code like this for some local classes. This "prolog"
         * code contains assignments from locals in the enclosing
         * scope to synthetic fields in the local class.  Same for
         * other language prolog code.
         */
        LocationImpl bestMatch = (LocationImpl)iter.next();
        while (iter.hasNext()) {
            LocationImpl current = (LocationImpl)iter.next();
            if (current.codeIndex() > codeIndex) {
                break;
            }
            bestMatch = current;
        }
        return bestMatch.getLineInfo();
    }


    @Override
	public List<LocalVariable> variables() throws AbsentInformationException {
        return getVariables();
    }

    @Override
	public List<LocalVariable> variablesByName(String name) throws AbsentInformationException {
        List<LocalVariable> variables = getVariables();

        List<LocalVariable> retList = new ArrayList<LocalVariable>(2);
        Iterator<LocalVariable> iter = variables.iterator();
        while(iter.hasNext()) {
            LocalVariable variable = iter.next();
            if (variable.name().equals(name)) {
                retList.add(variable);
            }
        }
        return retList;
    }

    @Override
	public List<LocalVariable> arguments() throws AbsentInformationException {
        List<LocalVariable> variables = getVariables();

        List<LocalVariable> retList = new ArrayList<LocalVariable>(variables.size());
        Iterator<LocalVariable> iter = variables.iterator();
        while(iter.hasNext()) {
            LocalVariable variable = iter.next();
            if (variable.isArgument()) {
                retList.add(variable);
            }
        }
        return retList;
    }

    @Override
	public byte[] bytecodes() {
        byte[] bytecodes = (bytecodesRef == null) ? null :
                                     bytecodesRef.get();
        if (bytecodes == null) {
            try {
                bytecodes = JDWP.Method.Bytecodes.
                                 process(vm, declaringType, ref).bytes;
            } catch (JDWPException exc) {
                throw exc.toJDIException();
            }
            bytecodesRef = new SoftReference<byte[]>(bytecodes);
        }
        /*
         * Arrays are always modifiable, so it is a little unsafe
         * to return the cached bytecodes directly; instead, we
         * make a clone at the cost of using more memory.
         */
        return bytecodes.clone();
    }

    @Override
	int argSlotCount() throws AbsentInformationException {
        if (argSlotCount == -1) {
            getVariables();
        }
        return argSlotCount;
    }

    private SoftLocationXRefs getLocations() {
        return getBaseLocations();

    }

    private SoftLocationXRefs getBaseLocations() {
        SoftLocationXRefs info = (softBaseLocationXRefsRef == null) ? null :
                                     softBaseLocationXRefsRef.get();
        if (info != null) {
            return info;
        }

        JDWP.Method.LineTable lntab = null;
        try {
            lntab = JDWP.Method.LineTable.process(vm, declaringType, ref);
        } catch (JDWPException exc) {
            /*
             * Note: the absent info error shouldn't happen here
             * because the first and last index are always available.
             */
            throw exc.toJDIException();
        }

        int count  = lntab.lines.length;

        List<Location> lineLocations = new ArrayList<Location>(count);
        Map<Integer, List<Location>>lineMapper = new HashMap<Integer, List<Location>>();
        int lowestLine = -1;
        int highestLine = -1;
        for (int i = 0; i < count; i++) {
            long bci = lntab.lines[i].lineCodeIndex;
            int lineNumber = lntab.lines[i].lineNumber;

            /*
             * Some compilers will point multiple consecutive
             * lines at the same location. We need to choose
             * one of them so that we can consistently map back
             * and forth between line and location. So we choose
             * to record only the last line entry at a particular
             * location.
             */
            if ((i + 1 == count) || (bci != lntab.lines[i+1].lineCodeIndex)) {
                // Remember the largest/smallest line number
                if (lineNumber > highestLine) {
                    highestLine = lineNumber;
                }
                if ((lineNumber < lowestLine) || (lowestLine == -1)) {
                    lowestLine = lineNumber;
                }
                LocationImpl loc =
                    new LocationImpl(virtualMachine(), this, bci);
                loc.addBaseLineInfo(
                    new BaseLineInfo(lineNumber, declaringType));

                // Add to the location list
                lineLocations.add(loc);

                // Add to the line -> locations map
                Integer key = new Integer(lineNumber);
                List<Location> mappedLocs = lineMapper.get(key);
                if (mappedLocs == null) {
                    mappedLocs = new ArrayList<Location>(1);
                    lineMapper.put(key, mappedLocs);
                }
                mappedLocs.add(loc);
            }
        }

        /*
         * firstIndex, lastIndex, and startLocation need to be
         * retrieved only once since they are strongly referenced.
         */
        if (location == null) {
            firstIndex = lntab.start;
            lastIndex = lntab.end;
            /*
             * The startLocation is the first one in the
             * location list if we have one;
             * otherwise, we construct a location for a
             * method start with no line info
             */
            if (count > 0) {
                location = lineLocations.get(0);
            } else {
                location = new LocationImpl(virtualMachine(), this,
                                            firstIndex);
            }
        }

        info = new SoftLocationXRefs(SDE.BASE_STRATUM_NAME,
                                lineMapper, lineLocations,
                                lowestLine, highestLine);
        softBaseLocationXRefsRef = new SoftReference<SoftLocationXRefs>(info);
        return info;
    }

    private List<LocalVariable> getVariables1_4() throws AbsentInformationException {
        JDWP.Method.VariableTable vartab = null;
        try {
            vartab = JDWP.Method.VariableTable.
                                     process(vm, declaringType, ref);
        } catch (JDWPException exc) {
            if (exc.errorCode() == JDWP.Error.ABSENT_INFORMATION) {
                absentVariableInformation = true;
                throw new AbsentInformationException();
            } else {
                throw exc.toJDIException();
            }
        }

        // Get the number of slots used by argument variables
        argSlotCount = vartab.argCnt;
        int count = vartab.slots.length;
        List<LocalVariable> variables = new ArrayList<LocalVariable>(count);
        for (int i=0; i<count; i++) {
            JDWP.Method.VariableTable.SlotInfo si = vartab.slots[i];

            /*
             * Skip "this*" entries because they are never real
             * variables from the JLS perspective.
             */
            if (!si.name.startsWith("this$") && !si.name.equals("this")) {
                Location scopeStart = new LocationImpl(virtualMachine(),
                                                       this, si.codeIndex);
                Location scopeEnd =
                    new LocationImpl(virtualMachine(), this,
                                     si.codeIndex + si.length - 1);
                LocalVariable variable =
                    new LocalVariableImpl(virtualMachine(), this,
                                          si.slot, scopeStart, scopeEnd,
                                          si.name, si.signature, null);
                // Add to the variable list
                variables.add(variable);
            }
        }
        return variables;
    }

    private List<LocalVariable> getVariables1() throws AbsentInformationException {

        return getVariables1_4();
    }

    private List<LocalVariable> getVariables() throws AbsentInformationException {
        if (absentVariableInformation) {
            throw new AbsentInformationException();
        }

        List<LocalVariable> variables = (variablesRef == null) ? null :
                                        variablesRef.get();
        if (variables != null) {
            return variables;
        }
        variables = getVariables1();
        variables = Collections.unmodifiableList(variables);
        variablesRef = new SoftReference<List<LocalVariable>>(variables);
        return variables;
    }
}
