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
 * @summary converted from VM Testbase nsk/jdi/VirtualMachine/redefineClasses/redefineclasses011.
 * VM Testbase keywords: [quick, jpda, jdi, redefine]
 * VM Testbase readme:
 * DESCRIPTION:
 *     The test against the method com.sun.jdi.VirtualMachine.redefineClasses()
 *     and checks up the following assertion:
 *        "If canUnrestrictedlyRedefineClasses() is false, attempting to
 *         add a constructor will throw UnsupportedOperationException
 *         exception"
 *     The test consists of the following files:
 *         redefineclasses011.java             - debugger
 *         redefineclasses011a.java            - initial debugee
 *         newclassXX/redefineclasses011a.java - redefining debugees
 *     This test performs the following cases:
 *       1. newclass01 - adding an explicit <public> constructor into a public
 *            class without constructor declarations. No exception is expected:
 *            the JLS 2nd ed. says:
 *              "8.8.7 Default Constructor
 *               If a class contains no constructor declarations, then a
 *               default constructor that takes no parameters is automatically
 *               provided.
 *               ...
 *               If the class is declared public, then the default constructor
 *               is implicitly given the access modifier public (6.6)"
 *       2. newclass02 - adding <protected> constructor into a public class
 *            without constructor declarations, exception may occure
 *       3. newclass03 - adding <private> constructor into a public class
 *            without constructor declarations, exception may occure
 *       4. newclass04 - adding <private package> constructor into a public class
 *            without constructor declarations, exception may occure
 *     The test checks two different cases for suspended debugee and not
 *     suspended one.
 * COMMENTS:
 *     The test was fixed due to the following bug:
 *     4762721 TEST_BUG: Some tests for RedefineClasses erroneously change
 *             ctor modifier
 *     Test was fixed according to test bug:
 *     4778296 TEST_BUG: debuggee VM intemittently hangs after resuming
 *     - handling VMStartEvent was removed from the debugger part of the test
 *
 * @library /vmTestbase
 *          /test/lib
 * @run driver jdk.test.lib.FileInstaller . .
 * @build nsk.jdi.VirtualMachine.redefineClasses.redefineclasses011
 *        nsk.jdi.VirtualMachine.redefineClasses.redefineclasses011a
 *
 * @comment compile newclassXX to bin/newclassXX
 *          with full debug info
 * @run driver nsk.share.ExtraClassesBuilder
 *      -g:lines,source,vars
 *      newclass01 newclass02 newclass03 newclass04
 *
 * @run main/othervm PropertyResolvingWrapper
 *      nsk.jdi.VirtualMachine.redefineClasses.redefineclasses011
 *      ./bin
 *      -verbose
 *      -arch=${os.family}-${os.simpleArch}
 *      -waittime=5
 *      -debugee.vmkind=java
 *      -transport.address=dynamic
 *      "-debugee.vmkeys=${test.vm.opts} ${test.java.opts}"
 */

