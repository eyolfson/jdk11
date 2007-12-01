/*
 * Copyright 1999 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
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
/*
 * COMPONENT_NAME: idl.parser
 *
 * ORIGINS: 27
 *
 * Licensed Materials - Property of IBM
 * 5639-D57 (C) COPYRIGHT International Business Machines Corp. 1997, 1999
 * RMI-IIOP v1.0
 *
 */

package com.sun.tools.corba.se.idl;

// NOTES:

import java.io.PrintWriter;
import java.util.Hashtable;

import com.sun.tools.corba.se.idl.constExpr.Expression;

/**
 * This is the symbol table entry for constants.
 **/
public class ConstEntry extends SymtabEntry
{
  protected ConstEntry ()
  {
    super ();
  } // ctor

  protected ConstEntry (ConstEntry that)
  {
    super (that);
    if (module ().equals (""))
      module (name ());
    else if (!name ().equals (""))
      module (module () + "/" + name ());
    _value = that._value;
  } // ctor

  /** This is a shallow copy constructor. */
  protected ConstEntry (SymtabEntry that, IDLID clone)
  {
    super (that, clone);
    if (module ().equals (""))
      module (name ());
    else if (!name ().equals (""))
      module (module () + "/" + name ());
  } // ctor

  /** This is a shallow copy clone. */
  public Object clone ()
  {
    return new ConstEntry (this);
  } // clone

  /** Invoke the constant generator.
      @param symbolTable the symbol table is a hash table whose key is
       a fully qualified type name and whose value is a SymtabEntry or
       a subclass of SymtabEntry.
      @param stream the stream to which the generator should sent its output.
      @see SymtabEntry */
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
    constGen.generate (symbolTable, this, stream);
  } // generate

  /** Access the constant generator.
      @returns an object which implements the ConstGen interface.
      @see ConstGen */
  public Generator generator ()
  {
    return constGen;
  } // generator

  public Expression value ()
  {
    return _value;
  } // value

  public void value (Expression newValue)
  {
    _value = newValue;
  } // value

  static ConstGen    constGen;
  private Expression _value = null;
} // class ConstEntry
