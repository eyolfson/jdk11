/*
 * Copyright (c) 1999, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.corba.se.idl.constExpr;

// NOTES:

import com.sun.tools.corba.se.idl.Util;
import java.math.BigInteger;

public class BooleanNot extends UnaryExpr
{
  protected BooleanNot (Expression operand)
  {
    super ("!", operand);
  } // ctor

  public Object evaluate () throws EvaluationException
  {
    try
    {
      Object tmp = operand ().evaluate ();
      Boolean op;
      //daz      if (tmp instanceof Number)
      //           op = new Boolean (((Number)tmp).longValue () != 0);
      //         else
      //           op = (Boolean)tmp;
      if (tmp instanceof Number)
      {
        if (tmp instanceof BigInteger)
          op = new Boolean (((BigInteger)tmp).compareTo (zero) != 0);
        else
          op = new Boolean (((Number)tmp).longValue () != 0);
      }
      else
        op = (Boolean)tmp;

      value (new Boolean (!op.booleanValue ()));
    }
    catch (ClassCastException e)
    {
      String[] parameters = {Util.getMessage ("EvaluationException.booleanNot"), operand ().value ().getClass ().getName ()};
      throw new EvaluationException (Util.getMessage ("EvaluationException.2", parameters));
    }
    return value ();
  } // evaluate
} // class BooleanNot
