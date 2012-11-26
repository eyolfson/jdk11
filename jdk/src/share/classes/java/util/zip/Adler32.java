/*
 * Copyright (c) 1996, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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

package java.util.zip;

import java.nio.ByteBuffer;
import sun.nio.ch.DirectBuffer;

/**
 * A class that can be used to compute the Adler-32 checksum of a data
 * stream. An Adler-32 checksum is almost as reliable as a CRC-32 but
 * can be computed much faster.
 *
 * <p> Passing a {@code null} argument to a method in this class will cause
 * a {@link NullPointerException} to be thrown.
 *
 * @see         Checksum
 * @author      David Connelly
 */
public
class Adler32 implements Checksum {

    private int adler = 1;

    /**
     * Creates a new Adler32 object.
     */
    public Adler32() {
    }

    /**
     * Updates the checksum with the specified byte (the low eight
     * bits of the argument b).
     *
     * @param b the byte to update the checksum with
     */
    public void update(int b) {
        adler = update(adler, b);
    }

    /**
     * Updates the checksum with the specified array of bytes.
     */
    public void update(byte[] b, int off, int len) {
        if (b == null) {
            throw new NullPointerException();
        }
        if (off < 0 || len < 0 || off > b.length - len) {
            throw new ArrayIndexOutOfBoundsException();
        }
        adler = updateBytes(adler, b, off, len);
    }

    /**
     * Updates the checksum with the specified array of bytes.
     *
     * @param b the byte array to update the checksum with
     */
    public void update(byte[] b) {
        adler = updateBytes(adler, b, 0, b.length);
    }


    /**
     * Updates the checksum with the bytes from the specified buffer.
     *
     * The checksum is updated using
     * buffer.{@link java.nio.Buffer#remaining() remaining()}
     * bytes starting at
     * buffer.{@link java.nio.Buffer#position() position()}
     * Upon return, the buffer's position will be updated to its
     * limit; its limit will not have been changed.
     *
     * @param buffer the ByteBuffer to update the checksum with
     * @since 1.8
     */
    public void update(ByteBuffer buffer) {
        int pos = buffer.position();
        int limit = buffer.limit();
        assert (pos <= limit);
        int rem = limit - pos;
        if (rem <= 0)
            return;
        if (buffer instanceof DirectBuffer) {
            adler = updateByteBuffer(adler, ((DirectBuffer)buffer).address(), pos, rem);
        } else if (buffer.hasArray()) {
            adler = updateBytes(adler, buffer.array(), pos + buffer.arrayOffset(), rem);
        } else {
            byte[] b = new byte[rem];
            buffer.get(b);
            adler = updateBytes(adler, b, 0, b.length);
        }
        buffer.position(limit);
    }

    /**
     * Resets the checksum to initial value.
     */
    public void reset() {
        adler = 1;
    }

    /**
     * Returns the checksum value.
     */
    public long getValue() {
        return (long)adler & 0xffffffffL;
    }

    private native static int update(int adler, int b);
    private native static int updateBytes(int adler, byte[] b, int off,
                                          int len);
    private native static int updateByteBuffer(int adler, long addr,
                                               int off, int len);
}
