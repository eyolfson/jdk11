/*
 * Copyright (c) 2002, Oracle and/or its affiliates. All rights reserved.
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
 * @bug 4525364
 * @summary Determine if duplicate throws tags can be used.
 * @author jamieh
 * @library ../lib/
 * @build JavadocTester
 * @build TestDupThrowsTags
 * @run main TestDupThrowsTags
 */
public class TestDupThrowsTags extends JavadocTester {

    private static final String BUG_ID = "4525364";
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, SRC_DIR + FS + "TestDupThrowsTags.java"
    };

    /**
     * The entry point of the test.
     * @param args the array of command line arguments.
     */
    public static void main(String[] args) {
        String[][] tests = new String[4][2];
        for (int i = 0; i < tests.length; i++) {
            tests[i][0] = BUG_ID + FS + "TestDupThrowsTags.html";
            tests[i][1] = "Test "+(i+1)+" passes";
        }
        TestDupThrowsTags tester = new TestDupThrowsTags();
        run(tester, ARGS, tests, NO_TEST);
        tester.printSummary();
    }

    /**
     * {@inheritDoc}
     */
    public String getBugId() {
        return BUG_ID;
    }

    /**
     * {@inheritDoc}
     */
    public String getBugName() {
        return getClass().getName();
    }

    /**
     * @throws java.io.IOException Test 1 passes
     * @throws java.io.IOException Test 2 passes
     * @throws java.lang.NullPointerException Test 3 passes
     * @throws java.io.IOException Test 4 passes
     */
    public void method(){}

}
