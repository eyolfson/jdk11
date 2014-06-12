/*
* Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
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
 * @ignore 8025645
 * @test TestUseCompressedOopsErgo
 * @key gc
 * @bug 8010722
 * @summary Tests ergonomics for UseCompressedOops.
 * @library /testlibrary /testlibrary/whitebox
 * @build TestUseCompressedOopsErgo TestUseCompressedOopsErgoTools
 * @run main ClassFileInstaller sun.hotspot.WhiteBox
 * @run main/othervm TestUseCompressedOopsErgo -XX:+UseG1GC
 * @run main/othervm TestUseCompressedOopsErgo -XX:+UseParallelGC
 * @run main/othervm TestUseCompressedOopsErgo -XX:+UseParallelGC -XX:-UseParallelOldGC
 * @run main/othervm TestUseCompressedOopsErgo -XX:+UseConcMarkSweepGC
 * @run main/othervm TestUseCompressedOopsErgo -XX:+UseSerialGC
 */

public class TestUseCompressedOopsErgo {

  public static void main(String args[]) throws Exception {
    if (!TestUseCompressedOopsErgoTools.is64bitVM()) {
      // this test is relevant for 64 bit VMs only
      return;
    }
    final String[] gcFlags = args;
    TestUseCompressedOopsErgoTools.checkCompressedOopsErgo(gcFlags);
  }
}

