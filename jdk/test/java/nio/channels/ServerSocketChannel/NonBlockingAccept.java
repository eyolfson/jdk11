/*
 * Copyright (c) 2003, 2004, Oracle and/or its affiliates. All rights reserved.
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

/* @test
 * @bug 4801882 5046333
 * @summary test ServerSocketAdaptor.accept on nonblocking channel
 * @library ..
 * @build TestUtil
 * @run main NonBlockingAccept
 * @key randomness
 */

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;


public class NonBlockingAccept {

    public static void main(String[] args) throws Exception {

        ServerSocketChannel ssc = ServerSocketChannel.open();
        InetSocketAddress isa = TestUtil.bindToRandomPort(ssc);
        ssc.configureBlocking(false);
        ServerSocket ss = ssc.socket();

        // Exception should be thrown when no connection is waiting
        try {
            ss.accept();
            throw new RuntimeException("Expected exception not thrown");
        } catch (IllegalBlockingModeException ibme) {
            // Correct result
        }

        // No exception should be thrown when a connection is waiting (5046333)
        SocketChannel sc = SocketChannel.open();
        sc.configureBlocking(false);
        sc.connect(isa);
        Thread.sleep(100);
        ss.accept();

    }

}
