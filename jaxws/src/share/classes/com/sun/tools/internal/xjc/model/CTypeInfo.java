/*
 * Copyright 2006 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.tools.internal.xjc.model;

import com.sun.codemodel.internal.JClass;
import com.sun.codemodel.internal.JCodeModel;
import com.sun.codemodel.internal.JExpression;
import com.sun.codemodel.internal.JType;
import com.sun.tools.internal.xjc.model.nav.NClass;
import com.sun.tools.internal.xjc.model.nav.NType;
import com.sun.tools.internal.xjc.outline.Aspect;
import com.sun.tools.internal.xjc.outline.Outline;
import com.sun.xml.internal.bind.v2.model.core.TypeInfo;
import com.sun.xml.internal.xsom.XmlString;

/**
 * {@link TypeInfo} at the compile-time.
 * Either {@link CClassInfo}, {@link CBuiltinLeafInfo}, or {@link CElementInfo}.
 *
 * <p>
 * This interface implements {@link TypeUse} so that a {@link CTypeInfo}
 * instance can be used as a {@link TypeUse} instance.
 *
 * @author Kohsuke Kawaguchi
 */
public interface CTypeInfo extends TypeInfo<NType,NClass>, TypeUse, CCustomizable {

    /**
     * Returns the {@link JClass} that represents the class being bound,
     * under the given {@link Outline}.
     *
     * @see NType#toType(Outline, Aspect)
     */
    JType toType(Outline o, Aspect aspect);
}
