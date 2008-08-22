/*
 * Copyright 2000-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

package sun.jvm.hotspot.oops;

import java.io.*;
import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.types.*;

// An Instance is an instance of a Java Class

public class Instance extends Oop {
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static long typeSize;

  private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
    Type type = db.lookupType("instanceOopDesc");
    typeSize = type.getSize();
  }

  Instance(OopHandle handle, ObjectHeap heap) {
    super(handle, heap);
  }

  // Returns header size in bytes.
  public static long getHeaderSize() {
    if (VM.getVM().isCompressedOopsEnabled()) {
      return typeSize - VM.getVM().getIntSize();
    } else {
      return typeSize;
    }
  }

  public boolean isInstance()          { return true; }

  public void iterateFields(OopVisitor visitor, boolean doVMFields) {
    super.iterateFields(visitor, doVMFields);
    ((InstanceKlass) getKlass()).iterateNonStaticFields(visitor);
  }

  public void printValueOn(PrintStream tty) {
    // Special-case strings.
    // FIXME: would like to do this in more type-safe fashion (need
    // SystemDictionary analogue)
    if (getKlass().getName().asString().equals("java/lang/String")) {
      tty.print("\"" + OopUtilities.stringOopToString(this) + "\"");
    } else {
      super.printValueOn(tty);
    }
  }
}
