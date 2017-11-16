/*
 * Copyright (c) 2017, 2017, Oracle and/or its affiliates. All rights reserved.
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
package org.graalvm.compiler.jtt.optimize;

import org.junit.Test;

import org.graalvm.compiler.jtt.JTTTest;

/*
 * Tests constant folding of double operations.
 */
public class Fold_Double04 extends JTTTest {

    // Contrived check whether both arguments are the same kind of zero
    public static boolean test(double x, double y) {
        if (x == 0) {
            if (1 / x == Double.NEGATIVE_INFINITY) {
                return 1 / y == Double.NEGATIVE_INFINITY;
            } else {
                return 1 / y == Double.POSITIVE_INFINITY;
            }
        }
        return false;
    }

    @Test
    public void run0() throws Throwable {
        runTest("test", -0d, -0d);
    }

    @Test
    public void run1() throws Throwable {
        runTest("test", -0d, 0d);
    }

    @Test
    public void run2() throws Throwable {
        runTest("test", 0d, -0d);
    }

    @Test
    public void run3() throws Throwable {
        runTest("test", 0d, 0d);
    }

}
