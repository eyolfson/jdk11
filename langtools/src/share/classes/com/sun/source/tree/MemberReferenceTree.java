/*
 * Copyright (c) 2011, 2014, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.source.tree;

import java.util.List;

import javax.lang.model.element.Name;

/**
 * A tree node for a member reference expression.
 *
 * For example:
 * <pre>
 *   <em>expression</em> # <em>[ identifier | new ]</em>
 * </pre>
 *
 * @since 1.8
 */
@jdk.Exported
public interface MemberReferenceTree extends ExpressionTree {

    /**
     * There are two kinds of member references: (i) method references and
     * (ii) constructor references
     */
    @jdk.Exported
    public enum ReferenceMode {
        /** enum constant for method references. */
        INVOKE,
        /** enum constant for constructor references. */
        NEW
    }

    /**
     * Returns the mode of the reference.
     * @return the mode
     */
    ReferenceMode getMode();

    /**
     * Returns the qualifier expression for the reference.
     * @return the qualifier expression
     */
    ExpressionTree getQualifierExpression();

    /**
     * Returns the name of the reference.
     * @return the name
     */
    Name getName();

    /**
     * Returns the type arguments for the reference.
     * @return the type arguments
     */
    List<? extends ExpressionTree> getTypeArguments();
}
