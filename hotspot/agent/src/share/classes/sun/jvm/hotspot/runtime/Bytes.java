/*
 * Copyright 2001 Sun Microsystems, Inc.  All Rights Reserved.
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
 *
 */

package sun.jvm.hotspot.runtime;

import sun.jvm.hotspot.debugger.*;

/** Encapsulates some byte-swapping operations defined in the VM */

public class Bytes {
  private boolean swap;

  public Bytes(MachineDescription machDesc) {
    swap = !machDesc.isBigEndian();
  }

  /** Should only swap if the hardware's underlying byte order is
      different from Java's */
  public short swapShort(short x) {
    if (!swap)
      return x;

    return (short) (((x >> 8) & 0xFF) | (x << 8));
  }

  /** Should only swap if the hardware's underlying byte order is
      different from Java's */
  public int swapInt(int x) {
    if (!swap)
      return x;

    return (swapShort((short) x) << 16) | (swapShort((short) (x >> 16)) & 0xFFFF);
  }

  /** Should only swap if the hardware's underlying byte order is
      different from Java's */
  public long swapLong(long x) {
    if (!swap)
      return x;

    return (swapInt((int) x) << 32) | (swapInt((int) (x >> 32)) & 0xFFFFFFFF);
  }
}
