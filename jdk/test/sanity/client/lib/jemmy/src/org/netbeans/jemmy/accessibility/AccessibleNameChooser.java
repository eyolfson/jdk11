/*
 * Copyright (c) 1997, 2016, Oracle and/or its affiliates. All rights reserved.
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
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package org.netbeans.jemmy.accessibility;

import javax.accessibility.AccessibleContext;

import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.StringComparator;

public class AccessibleNameChooser extends AccessibilityChooser {

    String name;
    StringComparator comparator;

    public AccessibleNameChooser(String name, StringComparator comparator) {
        this.name = name;
        this.comparator = comparator;
    }

    public AccessibleNameChooser(String name) {
        this(name, Operator.getDefaultStringComparator());
    }

    @Override
    public final boolean checkContext(AccessibleContext context) {
        return comparator.equals(context.getAccessibleName(), name);
    }

    @Override
    public String getDescription() {
        return "JComponent with \"" + name + "\" accessible name";
    }

    @Override
    public String toString() {
        return "AccessibleNameChooser{" + "name=" + name + ", comparator=" + comparator + '}';
    }
}
