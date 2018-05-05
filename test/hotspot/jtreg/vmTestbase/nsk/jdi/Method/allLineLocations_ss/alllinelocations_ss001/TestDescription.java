/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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


/*
 * @test
 *
 * @summary converted from VM Testbase nsk/jdi/Method/allLineLocations_ss/alllinelocations_ss001.
 * VM Testbase keywords: [jpda, jdi, feature_sde]
 * VM Testbase readme:
 * DESCRIPTION:
 *     The test for the implementation of an object of the type
 *     Method.
 *     The test checks up that a result of the method
 *     com.sun.jdi.Method.allLineLocations(String,String)
 *     complies with its spec:
 *     public List allLineLocations(String stratum, String sourceName)
 *                       throws AbsentInformationException
 *     Returns a list containing a Location object for
 *     each executable source line in this method.
 *     Each location maps a source line to a range of code indices.
 *     The beginning of the range can be determined through Location.codeIndex().
 *     The returned list is ordered by code index (from low to high).
 *     The returned list may contain multiple locations for
 *     a particular line number, if the compiler and/or VM has mapped that line
 *     to two or more disjoint code index ranges.
 *     If the method is native or abstract, an empty list is returned.
 *     Returned list is for the specified stratum
 *     (see Location for a description of strata).
 *     Parameters: stratum - The stratum to retrieve information from or
 *                           null for the #defaultStratum()
 *                 sourceName - Return locations only within this source file or
 *                           null to return locations.
 *     Returns: a List of all source line Location objects.
 *     Throws: AbsentInformationException -
 *             if there is no line number information for this
 *             (non-native, non-abstract) method.
 *             Or if sourceName is non-null and
 *             source name information is not present.
 *     The case for testing includes only non-abstract,
 *     non-native method and defaultStratum.
 *     The test checks up that a list of locations for the method
 *       - is not empty and
 *       - is ordered in a proper, from low to high, sequence.
 *     The test works as follows:
 *     The debugger program - nsk.jdi.Method.allLineLocations_ss.alllinelocations_ss001;
 *     the debuggee program - nsk.jdi.Method.allLineLocations_ss.alllinelocations_ss001a.
 *     Using nsk.jdi.share classes,
 *     the debugger gets the debuggee running on another JavaVM,
 *     creates the object debuggee.VM,
 *     establishes a pipe with the debuggee program, and then
 *     send to the programm commands, to which the debuggee replies
 *     via the pipe. Upon getting reply,
 *     the debugger calls corresponding debuggee.VM methods to get
 *     needed data and compares the data got to the data expected.
 *     In case of error the test produces the return value 97 and
 *     a corresponding error message(s).
 *     Otherwise, the test is passed and produces
 *     the return value 95 and no message.
 * COMMENTS:
 *     The option
 *         JAVAC_OPTS=-g
 *     is put in the locationsofline002.cfg file.
 *
 * @library /vmTestbase
 *          /test/lib
 * @run driver jdk.test.lib.FileInstaller . .
 * @build nsk.jdi.Method.allLineLocations_ss.alllinelocations_ss001
 *        nsk.jdi.Method.allLineLocations_ss.alllinelocations_ss001a
 *
 * @comment make sure alllinelocations_ss001a is compiled with full debug info
 * @clean nsk.jdi.Method.allLineLocations_ss.alllinelocations_ss001a
 * @compile -g:lines,source,vars ../alllinelocations_ss001a.java
 *
 * @run main/othervm PropertyResolvingWrapper
 *      nsk.jdi.Method.allLineLocations_ss.alllinelocations_ss001
 *      -verbose
 *      -arch=${os.family}-${os.simpleArch}
 *      -waittime=5
 *      -debugee.vmkind=java
 *      -transport.address=dynamic
 *      "-debugee.vmkeys=${test.vm.opts} ${test.java.opts}"
 */

