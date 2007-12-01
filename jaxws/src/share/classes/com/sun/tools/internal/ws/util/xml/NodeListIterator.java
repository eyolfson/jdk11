/*
 * Portions Copyright 2006 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.tools.internal.ws.util.xml;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.w3c.dom.NodeList;

/**
 * @author WS Development Team
 */
public class NodeListIterator implements Iterator {

    protected NodeList _list;
    protected int _index;

    public NodeListIterator(NodeList list) {
        _list = list;
        _index = 0;
    }

    public boolean hasNext() {
        if (_list == null)
            return false;
        return _index < _list.getLength();
    }

    public Object next() throws NoSuchElementException {
        if (_list.getLength() == 0)
            throw new NoSuchElementException();
        Object obj = _list.item(_index);
        if (obj != null)
            ++_index;
        return obj;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
