/*
 * Copyright 2008 Sun Microsystems, Inc.  All Rights Reserved.
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
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

/* @test
   @bug 4117820
   @summary Verify that SJIS.Decoder works properly for values between 0xA000 and 0xA0FC
 */

import java.nio.charset.*;
import java.nio.*;

public class TestIllegalSJIS {

  public static void main(String[] args) throws Exception
  {
    CharsetDecoder dec = Charset.forName("SJIS").newDecoder()
      .onUnmappableCharacter(CodingErrorAction.REPLACE)
      .onMalformedInput(CodingErrorAction.REPLACE);
    byte[] sjis      = {(byte)0xA0, (byte)0x00};

    int b;
    for (b = 0; b < 0xFD; b++) {
      sjis[1] = (byte) b;
      CharBuffer cb = dec.decode(ByteBuffer.wrap(sjis));
      if (cb.charAt(0) != 0xFFFD) {
        throw new Exception(Integer.toHexString(0xa000 + b) + " failed to convert to 0xFFFD");
      }
    }
  }
}
