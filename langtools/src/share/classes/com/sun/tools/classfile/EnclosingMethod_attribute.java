
/*
 * Copyright 2007-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.tools.classfile;

import java.io.IOException;

/**
 * See JVMS3, section 4.8.7.
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
public class EnclosingMethod_attribute extends Attribute {
    EnclosingMethod_attribute(ClassReader cr, int name_index, int length) throws IOException {
        super(name_index, length);
        class_index = cr.readUnsignedShort();
        method_index = cr.readUnsignedShort();
    }

    public EnclosingMethod_attribute(ConstantPool constant_pool, int class_index, int method_index)
            throws ConstantPoolException {
        this(constant_pool.getUTF8Index(Attribute.EnclosingMethod), class_index, method_index);
    }

    public EnclosingMethod_attribute(int name_index, int class_index, int method_index) {
        super(name_index, 4);
        this.class_index = class_index;
        this.method_index = method_index;
    }

    public String getClassName(ConstantPool constant_pool) throws ConstantPoolException {
        return constant_pool.getClassInfo(class_index).getName();
    }

    public String getMethodName(ConstantPool constant_pool) throws ConstantPoolException {
        if (method_index == 0)
            return "";
        return constant_pool.getNameAndTypeInfo(method_index).getName();
    }

    public <R, D> R accept(Visitor<R, D> visitor, D data) {
        return visitor.visitEnclosingMethod(this, data);
    }

    public final int class_index;
    public final int method_index;
}
