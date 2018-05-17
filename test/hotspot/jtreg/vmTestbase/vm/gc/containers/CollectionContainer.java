/*
 * Copyright (c) 2010, 2018, Oracle and/or its affiliates. All rights reserved.
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
package vm.gc.containers;

import java.util.Collection;
import java.util.Iterator;
import nsk.share.gc.gp.GarbageProducer;
import nsk.share.gc.gp.MemoryStrategy;


/*
 * This class updates collection by removal of random elements
 * via iterator and adding the same number of elements.
 */
class CollectionContainer extends TypicalContainer {

    Collection collection;

    public CollectionContainer(Collection collection, long maximumSize,
            GarbageProducer garbageProducer, MemoryStrategy memoryStrategy, Speed speed) {
        super(maximumSize, garbageProducer, memoryStrategy, speed);
        this.collection = collection;
    }

    @Override
    public void initialize() {
        for (int i = 0; i < count; i++) {
            if (!stresser.continueExecution()) {
                return;
            }
            collection.add(garbageProducer.create(size));
        }
    }

    @Override
    public void update() {
        Iterator iter = collection.iterator();
        int i = 0;
        while (iter.hasNext()) {
            Object obj = iter.next();
            if (i++ / 100 < speed.getValue()) {
                if (!stresser.continueExecution()) {
                    return;
                }
                iter.remove();
                garbageProducer.validate(obj);
            }
        }
        while (i != 0) {
            if (i-- / 100 < speed.getValue()) {
                if (!stresser.continueExecution()) {
                    return;
                }
                collection.add(garbageProducer.create(size));
            }
        }
    }
}
