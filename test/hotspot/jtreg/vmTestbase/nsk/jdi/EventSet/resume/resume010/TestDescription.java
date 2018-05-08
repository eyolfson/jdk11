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
 * @summary converted from VM Testbase nsk/jdi/EventSet/resume/resume010.
 * VM Testbase keywords: [jpda, jdi]
 * VM Testbase readme:
 * DESCRIPTION:
 *     The test for the implementation of an object of the type
 *     EventSet.
 *     The test checks up that a result of the method
 *     com.sun.jdi.EventSet.resume()
 *     complies with its spec:
 *     public void resume()
 *      Resumes threads suspended by this event set.
 *      If the suspendPolicy() is EventRequest.SUSPEND_ALL,
 *      a call to this method is equivalent to VirtualMachine.resume().
 *      If the suspend policy is EventRequest.SUSPEND_EVENT_THREAD,
 *      a call to this method is equivalent to ThreadReference.resume()
 *      for the event thread. Otherwise, a call to this method is a no-op.
 *     The test checks up on three StepEvent sets with suspendPolicy()
 *     NONE, EVENT_THREAD, and ALL.
 *     The test works as follows:
 *     The debugger program - nsk.jdi.EventSet.resume.resume010;
 *     the debuggee program - nsk.jdi.EventSet.resume.resume010a.
 *     Using nsk.jdi.share classes,
 *     the debugger gets the debuggee running on another JavaVM,
 *     creates the object debuggee.VM, and waits for VMStartEvent.
 *     Upon getting the debuggee VM started,
 *     the debugger calls corresponding debuggee.VM methods to get
 *     needed data and to perform checks.
 *     In case of error the test produces the return value 97 and
 *     a corresponding error message(s).
 *     Otherwise, the test is passed and produces
 *     the return value 95 and no message.
 * COMMENTS:
 *
 * @library /vmTestbase
 *          /test/lib
 * @run driver jdk.test.lib.FileInstaller . .
 * @build nsk.jdi.EventSet.resume.resume010
 *        nsk.jdi.EventSet.resume.resume010a
 * @run main/othervm PropertyResolvingWrapper
 *      nsk.jdi.EventSet.resume.resume010
 *      -verbose
 *      -arch=${os.family}-${os.simpleArch}
 *      -waittime=5
 *      -debugee.vmkind=java
 *      -transport.address=dynamic
 *      "-debugee.vmkeys=${test.vm.opts} ${test.java.opts}"
 */

