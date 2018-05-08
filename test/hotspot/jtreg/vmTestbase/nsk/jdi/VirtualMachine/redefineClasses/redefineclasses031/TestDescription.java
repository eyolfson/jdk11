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
 * @summary converted from VM Testbase nsk/jdi/VirtualMachine/redefineClasses/redefineclasses031.
 * VM Testbase keywords: [jpda, jdi, redefine]
 * VM Testbase readme:
 * DESCRIPTION:
 *     The test checks up com.sun.jdi.VirtualMachine.redefineClasses().
 *     In for loop the test sets breakpoint and tries to redefines debugee's
 *     method by changing value of debugee's field. After resuming
 *     debugee, the test checks if a tested field contains correct
 *     value.
 * COMMENTS
 *     This is a regression test on the following bug:
 *         4724076 Redefine does not work in for/while loop
 *     Test was fixed according to test bug:
 *     4778296 TEST_BUG: debuggee VM intemittently hangs after resuming
 *     - handling VMStartEvent was removed from the debugger part of the test
 *
 * @library /vmTestbase
 *          /test/lib
 * @run driver jdk.test.lib.FileInstaller . .
 * @build nsk.jdi.VirtualMachine.redefineClasses.redefineclasses031
 *        nsk.jdi.VirtualMachine.redefineClasses.redefineclasses031a
 *
 * @comment compile newclassXX to bin/newclassXX
 *          with full debug info
 * @run driver nsk.share.ExtraClassesBuilder
 *      -g:lines,source,vars
 *      newclass01 newclass02 newclass03 newclass04 newclass05
 *
 * @run main/othervm PropertyResolvingWrapper
 *      nsk.jdi.VirtualMachine.redefineClasses.redefineclasses031
 *      ./bin
 *      -verbose
 *      -arch=${os.family}-${os.simpleArch}
 *      -waittime=5
 *      -debugee.vmkind=java
 *      -transport.address=dynamic
 *      "-debugee.vmkeys=${test.vm.opts} ${test.java.opts}"
 */

