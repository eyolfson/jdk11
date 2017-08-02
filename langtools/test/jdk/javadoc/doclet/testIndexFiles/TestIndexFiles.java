/*
 * Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
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
 * @bug      8170825
 * @summary  Perform tests on index files generated by javadoc.
 * @author   bpatel
 * @library  ../lib
 * @modules jdk.javadoc/jdk.javadoc.internal.tool
 * @build    JavadocTester
 * @run main TestIndexFiles
 */

public class TestIndexFiles extends JavadocTester {

    public static void main(String... args) throws Exception {
        TestIndexFiles tester = new TestIndexFiles();
        tester.runTests();
    }

    @Test
    void testIndexFiles() {
        javadoc("-d", "out", "-splitindex", "-Xdoclint:none", "-sourcepath", testSrc,
                "-use", "pkg");
        checkExit(Exit.OK);
        checkIndexFiles(true);
    }

    void checkIndexFiles(boolean found) {
        checkOutput("index-files/index-1.html", found,
                "<li>Prev Letter</li>\n"
                + "<li><a href=\"index-2.html\">Next Letter</a></li>");
        checkOutput("index-files/index-5.html", found,
                "<li><a href=\"index-4.html\">Prev Letter</a></li>\n"
                + "<li>Next Letter</li>");
        checkOutput("index-files/index-1.html", !found,
                "<li><a href=\"index-0.html\">Prev Letter</a></li>\n"
                + "<li><a href=\"index-1.html\">Next Letter</a></li>");
        checkOutput("index-files/index-5.html", !found,
                "<li><a href=\"index-4.html\">Prev Letter</a></li>\n"
                + "<li><a href=\"index-5.html\">Next Letter</a></li>");
    }
}
