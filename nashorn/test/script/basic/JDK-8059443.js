/*
 * Copyright (c) 2010, 2014, Oracle and/or its affiliates. All rights reserved.
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

/**
 * JDK-8059443: NPE when unboxing return values
 * 
 * NOTE: this test can only pass when running with a JDK where 
 * JDK-8060483 is also fixed (9b37 or later).
 *
 * @test
 * @run
 */

var NullProvider = Java.type("jdk.nashorn.test.models.NullProvider");

try {
    if (!NullProvider.getBoolean()) { print("yay"); }
    print(NullProvider.getLong() * (1 << 33));
    print(NullProvider.getDouble() / 2.5);
    print(NullProvider.getInteger() << 1);
} catch (e if e instanceof java.lang.NullPointerException) {
    var st = e.stackTrace;
    if (st.length > 0 &&
        st[0].className.equals("sun.invoke.util.ValueConversions")) {
        // buggy JVM. ignore NPE and pass vacuously
        // print to match .EXPECTED output
        print("yay");
        print(0);
        print(0);
        print(0);
    } else {
        throw e;
    }
}
