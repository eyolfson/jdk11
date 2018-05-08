/*
 * Copyright (c) 2001, 2018, Oracle and/or its affiliates. All rights reserved.
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

package nsk.jdi.WatchpointRequest.addInstanceFilter;

import nsk.share.*;
import nsk.share.jpda.*;
import nsk.share.jdi.*;

/**
 * This class is used as debuggee application for the instancefilter008 JDI test.
 */

public class instancefilter008a {

    //----------------------------------------------------- templete section

    static final int PASSED = 0;
    static final int FAILED = 2;
    static final int PASS_BASE = 95;

    static ArgumentHandler argHandler;
    static Log log;

    //--------------------------------------------------   log procedures

    private static void log1(String message) {
        log.display("**> debuggee: " + message);
    }

    private static void logErr(String message) {
        log.complain("**> debuggee: " + message);
    }

    //====================================================== test program

    static Threadinstancefilter008a thread1 = null;
    static Threadinstancefilter008a thread2 = null;

    static instancefilter008aTestClass objTC[] = { new instancefilter008aTestClass(), new instancefilter008aTestClass(), new instancefilter008aTestClass() };

    //------------------------------------------------------ common section

    static int exitCode = PASSED;

    static int instruction = 1;
    static int end         = 0;
                                   //    static int quit        = 0;
                                   //    static int continue    = 2;
    static int maxInstr    = 1;    // 2;

    static int lineForComm = 2;

    private static void methodForCommunication() {
        int i1 = instruction;
        int i2 = i1;
        int i3 = i2;
    }
    //----------------------------------------------------   main method

    public static void main (String argv[]) {

        argHandler = new ArgumentHandler(argv);
        log = argHandler.createDebugeeLog();

        log1("debuggee started!");

        label0:
            for (int i = 0; ; i++) {

                if (instruction > maxInstr) {
                    logErr("ERROR: unexpected instruction: " + instruction);
                    exitCode = FAILED;
                    break ;
                }

                switch (i) {

    //------------------------------------------------------  section tested

                    case 0:
                            thread1 = new Threadinstancefilter008a("thread1");
                            thread2 = new Threadinstancefilter008a("thread2");
                            break;

                    case 1:
                            threadStart(thread1);
                            threadStart(thread2);
                            synchronized(lockingObj[0]) {
                                log1("main: synchronized(lockingObj[0])");
                            }
                            synchronized(lockingObj[1]) {
                                log1("main: synchronized(lockingObj[1])");
                            }

    //-------------------------------------------------    standard end section

                    default:
                            instruction = end;
                            break;
                }
                log1("methodForCommunication();");
                methodForCommunication();
                if (instruction == end)
                    break;
            }

        log1("debuggee exits");
        System.exit(exitCode + PASS_BASE);
    }


    static Object waitnotifyObj = new Object();

    static int threadStart(Thread t) {
        synchronized (waitnotifyObj) {
            t.start();
            try {
                waitnotifyObj.wait();
            } catch ( Exception e) {
                exitCode = FAILED;
                logErr("       Exception : " + e );
                return FAILED;
            }
        }
        return PASSED;
    }

    static Object lockingObj[] = new Object[2];
    static volatile int number = 0;

    static class Threadinstancefilter008a extends Thread {

        String tName = null;
        int tNumber;

        public Threadinstancefilter008a(String threadName) {
            super(threadName);
            tName = threadName;
            tNumber = number;
            number++;
            lockingObj[tNumber] = threadName;
        }

        public void run() {
            log1("  'run': enter  :: threadName == " + tName);
            if (lockingObj[tNumber] == null)
                log1("lockingObj[tNumber] == null");
            synchronized(lockingObj[tNumber]) {
                synchronized (waitnotifyObj) {
                    waitnotifyObj.notify();
                }
                log1(" objTC[tNumber].method();  :: threadName == " + tName + "  tNumber == " + tNumber);
                objTC[tNumber].method();
            }
            log1("  'run': exit   :: threadName == " + tName);
            return;
        }
    }

}

class instancefilter008aTestClass {

    static int breakpointLine = 3;

    int var1 = 0;
    int var2 = 0;
    int var3 = 0;

    public void method () {
        var1 = 1;
        var3 = var1;
        var2 = var3;
    }
}
