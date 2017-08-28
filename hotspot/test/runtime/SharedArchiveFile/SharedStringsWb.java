/*
 * Copyright (c) 2013, 2015, Oracle and/or its affiliates. All rights reserved.
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

import sun.hotspot.WhiteBox;

// This class is used by the test SharedStrings.java
// It should be launched in CDS mode
public class SharedStringsWb {
    public static void main(String[] args) throws Exception {
        WhiteBox wb = WhiteBox.getWhiteBox();

        if (wb.areSharedStringsIgnored()) {
            System.out.println("Shared strings are ignored, assuming PASS");
            return;
        }

        // The string below is known to be added to CDS archive
        String s = "<init>";
        String internedS = s.intern();

        // Check that it's a valid string
        if (s.getClass() != String.class || !(s instanceof String)) {
            throw new RuntimeException("Shared string is not a valid String: FAIL");
        }

        if (wb.isShared(internedS)) {
            System.out.println("Found shared string, result: PASS");
        } else {
            throw new RuntimeException("String is not shared, result: FAIL");
        }
    }
}


