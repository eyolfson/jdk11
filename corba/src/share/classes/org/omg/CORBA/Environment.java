/*
 * Copyright 1996-1999 Sun Microsystems, Inc.  All Rights Reserved.
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

package org.omg.CORBA;

/**
 * A container (holder) for an exception that is used in <code>Request</code>
 * operations to make exceptions available to the client.  An
 * <code>Environment</code> object is created with the <code>ORB</code>
 * method <code>create_environment</code>.
 *
 * @since   JDK1.2
 */

public abstract class Environment {

    /**
     * Retrieves the exception in this <code>Environment</code> object.
     *
     * @return                  the exception in this <code>Environment</code> object
     */

    public abstract java.lang.Exception exception();

    /**
     * Inserts the given exception into this <code>Environment</code> object.
     *
     * @param except            the exception to be set
     */

    public abstract void exception(java.lang.Exception except);

    /**
     * Clears this <code>Environment</code> object of its exception.
     */

    public abstract void clear();

}
