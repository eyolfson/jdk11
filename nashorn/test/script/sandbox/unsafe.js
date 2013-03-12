/*
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * Try to access sensitive class like Unsafe.
 *
 * @test
 * @security
 * @run
 */

function check(e) {
   if (! (e instanceof java.lang.SecurityException)) {
       fail("expected SecurityException, got " + e);
   }
}

try {
    var unsafe = java.lang.Class.forName("sun.misc.Unsafe");
    fail("No SecurityException for Class.forName sun.misc.Unsafe");
} catch (e) {
    check(e);
}

try {
    var unsafe = Java.type("sun.misc.Unsafe"); 
    fail("No SecurityException for Java.type sun.misc.Unsafe");
} catch (e) {
    check(e);
}

try {
    var unsafe = Packages.sun.misc.Unsafe;
    fail("No SecurityException for Packages.sun.misc.Unsafe");
} catch (e) {
    check(e);
}

try {
    var cl = Packages.jdk.nashorn.internal.runtime.Context.class;
    var unsafe = cl.getClassLoader().loadClass("sun.misc.Unsafe");
} catch (e) {
    check(e);
}
