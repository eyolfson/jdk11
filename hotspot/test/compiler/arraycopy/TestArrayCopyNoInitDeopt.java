/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
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
 * @bug 8072016
 * @summary Infinite deoptimization/recompilation cycles in case of arraycopy with tightly coupled allocation
 * @library /testlibrary /../../test/lib /compiler/whitebox
 * @modules java.base/sun.misc
 *          java.management
 * @build TestArrayCopyNoInitDeopt
 * @run main ClassFileInstaller sun.hotspot.WhiteBox
 * @run main ClassFileInstaller com.oracle.java.testlibrary.Platform
 * @run main/othervm -Xmixed -Xbootclasspath/a:. -XX:+UnlockDiagnosticVMOptions -XX:+WhiteBoxAPI
 *                   -XX:-BackgroundCompilation -XX:-UseOnStackReplacement -XX:TypeProfileLevel=020
 *                   TestArrayCopyNoInitDeopt
 *
 */


import sun.hotspot.WhiteBox;
import sun.hotspot.code.NMethod;
import com.oracle.java.testlibrary.Platform;
import java.lang.reflect.*;

public class TestArrayCopyNoInitDeopt {

    public static int[] m1(Object src) {
        if (src == null) return null;
        int[] dest = new int[10];
        try {
            System.arraycopy(src, 0, dest, 0, 10);
        } catch (ArrayStoreException npe) {
        }
        return dest;
    }

    static Object m2_src(Object src) {
        return src;
    }

    public static int[] m2(Object src) {
        if (src == null) return null;
        src = m2_src(src);
        int[] dest = new int[10];
        try {
            System.arraycopy(src, 0, dest, 0, 10);
        } catch (ArrayStoreException npe) {
        }
        return dest;
    }

    private static final WhiteBox WHITE_BOX = WhiteBox.getWhiteBox();

    static boolean deoptimize(Method method, Object src_obj) throws Exception {
        for (int i = 0; i < 10; i++) {
            method.invoke(null, src_obj);
            if (!WHITE_BOX.isMethodCompiled(method)) {
                return true;
            }
        }
        return false;
    }

    static public void main(String[] args) throws Exception {
        if (Platform.isServer()) {
            int[] src = new int[10];
            Object src_obj = new Object();
            Method method_m1 = TestArrayCopyNoInitDeopt.class.getMethod("m1", Object.class);
            Method method_m2 = TestArrayCopyNoInitDeopt.class.getMethod("m2", Object.class);

            // Warm up
            for (int i = 0; i < 20000; i++) {
                m1(src);
            }

            // And make sure m1 is compiled by C2
            WHITE_BOX.enqueueMethodForCompilation(method_m1, CompilerWhiteBoxTest.COMP_LEVEL_FULL_OPTIMIZATION);

            if (!WHITE_BOX.isMethodCompiled(method_m1)) {
                throw new RuntimeException("m1 not compiled");
            }

            // should deoptimize for type check
            if (!deoptimize(method_m1, src_obj)) {
                throw new RuntimeException("m1 not deoptimized");
            }

            WHITE_BOX.enqueueMethodForCompilation(method_m1, CompilerWhiteBoxTest.COMP_LEVEL_FULL_OPTIMIZATION);

            if (!WHITE_BOX.isMethodCompiled(method_m1)) {
                throw new RuntimeException("m1 not recompiled");
            }

            if (deoptimize(method_m1, src_obj)) {
                throw new RuntimeException("m1 deoptimized again");
            }

            // Same test as above but with speculative types

            // Warm up & make sure we collect type profiling
            for (int i = 0; i < 20000; i++) {
                m2(src);
            }

            // And make sure m2 is compiled by C2
            WHITE_BOX.enqueueMethodForCompilation(method_m2, CompilerWhiteBoxTest.COMP_LEVEL_FULL_OPTIMIZATION);

            if (!WHITE_BOX.isMethodCompiled(method_m2)) {
                throw new RuntimeException("m2 not compiled");
            }

            // should deoptimize for speculative type check
            if (!deoptimize(method_m2, src_obj)) {
                throw new RuntimeException("m2 not deoptimized");
            }

            WHITE_BOX.enqueueMethodForCompilation(method_m2, CompilerWhiteBoxTest.COMP_LEVEL_FULL_OPTIMIZATION);

            if (!WHITE_BOX.isMethodCompiled(method_m2)) {
                throw new RuntimeException("m2 not recompiled");
            }

            // should deoptimize for actual type check
            if (!deoptimize(method_m2, src_obj)) {
                throw new RuntimeException("m2 not deoptimized");
            }

            WHITE_BOX.enqueueMethodForCompilation(method_m2, CompilerWhiteBoxTest.COMP_LEVEL_FULL_OPTIMIZATION);

            if (!WHITE_BOX.isMethodCompiled(method_m2)) {
                throw new RuntimeException("m2 not recompiled");
            }

            if (deoptimize(method_m2, src_obj)) {
                throw new RuntimeException("m2 deoptimized again");
            }
        }
    }
}
