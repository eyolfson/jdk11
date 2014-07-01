/*
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.security.auth;

import java.security.Principal;

/**
 * <p> This class implements the <code>Principal</code> interface
 * and represents a user's Solaris identification number (UID).
 *
 * <p> Principals such as this <code>SolarisNumericUserPrincipal</code>
 * may be associated with a particular <code>Subject</code>
 * to augment that <code>Subject</code> with an additional
 * identity.  Refer to the <code>Subject</code> class for more information
 * on how to achieve this.  Authorization decisions can then be based upon
 * the Principals associated with a <code>Subject</code>.
 * @deprecated As of JDK&nbsp;1.4, replaced by
 *             {@link UnixNumericUserPrincipal}.
 *             This class is entirely deprecated.
 *
 * @see java.security.Principal
 * @see javax.security.auth.Subject
 */
@jdk.Exported(false)
@Deprecated
public class SolarisNumericUserPrincipal implements
                                        Principal,
                                        java.io.Serializable {

    private static final long serialVersionUID = -3178578484679887104L;

    private static final java.util.ResourceBundle rb =
          java.security.AccessController.doPrivileged
          (new java.security.PrivilegedAction<java.util.ResourceBundle>() {
              public java.util.ResourceBundle run() {
                  return (java.util.ResourceBundle.getBundle
                                ("sun.security.util.AuthResources"));
              }
           });


    /**
     * @serial
     */
    private String name;

    /**
     * Create a <code>SolarisNumericUserPrincipal</code> using a
     * <code>String</code> representation of the
     * user's identification number (UID).
     *
     * <p>
     *
     * @param name the user identification number (UID) for this user.
     *
     * @exception NullPointerException if the <code>name</code>
     *                  is <code>null</code>.
     */
    public SolarisNumericUserPrincipal(String name) {
        if (name == null)
            throw new NullPointerException(rb.getString("provided.null.name"));

        this.name = name;
    }

    /**
     * Create a <code>SolarisNumericUserPrincipal</code> using a
     * long representation of the user's identification number (UID).
     *
     * <p>
     *
     * @param name the user identification number (UID) for this user
     *                  represented as a long.
     */
    public SolarisNumericUserPrincipal(long name) {
        this.name = Long.toString(name);
    }

    /**
     * Return the user identification number (UID) for this
     * <code>SolarisNumericUserPrincipal</code>.
     *
     * <p>
     *
     * @return the user identification number (UID) for this
     *          <code>SolarisNumericUserPrincipal</code>
     */
    public String getName() {
        return name;
    }

    /**
     * Return the user identification number (UID) for this
     * <code>SolarisNumericUserPrincipal</code> as a long.
     *
     * <p>
     *
     * @return the user identification number (UID) for this
     *          <code>SolarisNumericUserPrincipal</code> as a long.
     */
    public long longValue() {
        return Long.parseLong(name);
    }

    /**
     * Return a string representation of this
     * <code>SolarisNumericUserPrincipal</code>.
     *
     * <p>
     *
     * @return a string representation of this
     *          <code>SolarisNumericUserPrincipal</code>.
     */
    public String toString() {
        return(rb.getString("SolarisNumericUserPrincipal.") + name);
    }

    /**
     * Compares the specified Object with this
     * <code>SolarisNumericUserPrincipal</code>
     * for equality.  Returns true if the given object is also a
     * <code>SolarisNumericUserPrincipal</code> and the two
     * SolarisNumericUserPrincipals
     * have the same user identification number (UID).
     *
     * <p>
     *
     * @param o Object to be compared for equality with this
     *          <code>SolarisNumericUserPrincipal</code>.
     *
     * @return true if the specified Object is equal equal to this
     *          <code>SolarisNumericUserPrincipal</code>.
     */
    public boolean equals(Object o) {
        if (o == null)
            return false;

        if (this == o)
            return true;

        if (!(o instanceof SolarisNumericUserPrincipal))
            return false;
        SolarisNumericUserPrincipal that = (SolarisNumericUserPrincipal)o;

        if (this.getName().equals(that.getName()))
            return true;
        return false;
    }

    /**
     * Return a hash code for this <code>SolarisNumericUserPrincipal</code>.
     *
     * <p>
     *
     * @return a hash code for this <code>SolarisNumericUserPrincipal</code>.
     */
    public int hashCode() {
        return name.hashCode();
    }
}
