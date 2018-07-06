/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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
 * @library /test/lib
 * @build jdk.test.lib.Utils
 * @bug 8204233
 * @summary Add configurable option for enhanced socket IOException messages
 * @run main/othervm ExceptionText
 * @run main/othervm -Djdk.net.includeInExceptions= ExceptionText
 * @run main/othervm -Djdk.net.includeInExceptions=hostInfo ExceptionText
 * @run main/othervm -Djdk.net.includeInExceptions=somethingElse ExceptionText
 */

import java.net.*;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.*;
import java.util.concurrent.ExecutionException;
import jdk.test.lib.Utils;

public class ExceptionText {

    enum TestTarget {SOCKET, CHANNEL, ASYNC_CHANNEL};

    static boolean propEnabled() {
        String val = System.getProperty("jdk.net.includeInExceptions");
        if ("hostinfo".equalsIgnoreCase(val))
            return true;
        return false;
    }

    public static void main(String args[]) throws Exception {
        boolean prop = propEnabled();
        test(prop);
    }

    static final InetSocketAddress dest  = Utils.refusingEndpoint();
    static final String PORT = ":" + Integer.toString(dest.getPort());
    static final String HOST = dest.getHostString();

    static void test(boolean withProperty) {
        // Socket
        IOException e = getException(TestTarget.SOCKET);
        checkResult(e, withProperty);
        // SocketChannel
        e = getException(TestTarget.CHANNEL);
        checkResult(e, withProperty);
        // AsyncSocketChannel
        e = getException(TestTarget.ASYNC_CHANNEL);
        checkResult(e, withProperty);
    }

    static void checkResult(IOException e, boolean withProperty) {
        String msg = e.getMessage();
        if (!withProperty) {
            if (msg.contains(HOST) || msg.contains(PORT)) {
                System.err.println("msg = " + msg);
                throw new RuntimeException("Test failed: exception contains address info");
            }
        } else {
            if (!msg.contains(HOST) || !msg.contains(PORT)) {
                if (e instanceof ClosedChannelException)
                    return; // has no detail msg
                System.err.println("msg = " + msg);
                throw new RuntimeException("Test failed: exception does not contain address info");
            }
        }
    }

    static IOException getException(TestTarget target) {
        try {
            if (target == TestTarget.SOCKET) {
                Socket s = new Socket();
                s.connect(dest);
            } else if (target == TestTarget.CHANNEL) {
                SocketChannel c = SocketChannel.open(dest);
            } else if (target == TestTarget.ASYNC_CHANNEL) {
                AsynchronousSocketChannel c = AsynchronousSocketChannel.open();
                try {
                    c.connect(dest).get();
                } catch (InterruptedException | ExecutionException ee) {
                    if (ee.getCause() instanceof IOException)
                        throw (IOException)ee.getCause();
                    throw new RuntimeException(ee.getCause());
                }
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return e;
        }
    }
}
