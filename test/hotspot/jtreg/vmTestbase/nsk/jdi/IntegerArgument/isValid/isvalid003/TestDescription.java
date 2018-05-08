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
 * @summary converted from VM Testbase nsk/jdi/IntegerArgument/isValid/isvalid003.
 * VM Testbase keywords: [quick, jpda, jdi]
 * VM Testbase readme:
 * DESCRIPTION:
 *     The test for the implementation of an object of the type
 *     Connector.IntegerArgument.
 *     The test checks up that a result of the method
 *         IntegerArgument.isValid(String value)
 *     complies with the following requirements:
 *     "true  if value represents an int that is
 *         min() <= value <= max()"
 *     in case when parameter is one of "", "a", null, null-string.
 *     The test works as follows:
 *     - Virtual Machine Manager is invoked.
 *     - First Connector.IntegerArgument object is searched among
 *     Arguments of Connectors. If no intArgument is found out
 *     the test exits with the return value = 95 and a warning message.
 *     - The follwing checks are made:
 *         !intArgument.isValid("")
 *           expected result - true
 *         !intArgument.isValid("a")
 *           expected result - true
 *     In case of unexpected result
 *     the test produces the return value 97 and
 *     a corresponding error message(s).
 *     Otherwise, the test is passed and produces
 *     the return value 95 and no message.
 * COMMENTS:
 *
 * @library /vmTestbase
 *          /test/lib
 * @run driver jdk.test.lib.FileInstaller . .
 * @build nsk.jdi.IntegerArgument.isValid.isvalid003
 * @run main/othervm PropertyResolvingWrapper
 *      nsk.jdi.IntegerArgument.isValid.isvalid003
 *      -verbose
 *      -arch=${os.family}-${os.simpleArch}
 *      -waittime=5
 *      -debugee.vmkind=java
 *      -transport.address=dynamic
 *      "-debugee.vmkeys=${test.vm.opts} ${test.java.opts}"
 */

