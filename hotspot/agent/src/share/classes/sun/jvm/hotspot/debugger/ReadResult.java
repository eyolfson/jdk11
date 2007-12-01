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

package sun.jvm.hotspot.debugger;

import java.io.Serializable;

/** The only reason for this is to not throw UnmappedAddressException
    across readBytesFromProcess() calls; we can't decompose the
    RemoteException precisely enough */

public class ReadResult implements Serializable {
  private byte[] data; // NULL if read failed
  private long   failureAddress;

  /** Successful result */
  public ReadResult(byte[] data) {
    this.data = data;
  }

  /** Unsuccessful result */
  public ReadResult(long failureAddress) {
    this.failureAddress = failureAddress;
  }

  public byte[] getData() {
    return data;
  }

  public long getFailureAddress() {
    return failureAddress;
  }
}
