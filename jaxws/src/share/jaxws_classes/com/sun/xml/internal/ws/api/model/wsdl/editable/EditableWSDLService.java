/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.internal.ws.api.model.wsdl.editable;

import javax.xml.namespace.QName;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import com.sun.xml.internal.ws.api.model.wsdl.WSDLService;

public interface EditableWSDLService extends WSDLService {

    @Override
    @NotNull
    EditableWSDLModel getParent();

    @Override
    EditableWSDLPort get(QName portName);

    @Override
    EditableWSDLPort getFirstPort();

    @Override
    @Nullable
    EditableWSDLPort getMatchingPort(QName portTypeName);

    @Override
    Iterable<? extends EditableWSDLPort> getPorts();

    /**
     * Associate WSDL port with port QName
     *
     * @param portName Port QName
     * @param port     Port
     */
    public void put(QName portName, EditableWSDLPort port);

    /**
     * Freezes WSDL model to prevent further modification
     *
     * @param root WSDL Model
     */
    void freeze(EditableWSDLModel root);
}
