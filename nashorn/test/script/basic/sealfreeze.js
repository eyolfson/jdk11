/*
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
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

/**
 * Basic checks for Object.seal and Object.freeze.
 *
 * @test
 * @run
 */

var obj = { num: Math.E };

print("sealed? " + Object.isSealed(obj));
print("frozen? " + Object.isFrozen(obj));

Object.seal(obj);
print("sealed? " + Object.isSealed(obj));

// delete should fail - because we have sealed
print("deleted num? " + (delete obj.num));
print("num = " + obj.num);

// assignment should succeed
print("assign PI to num");
obj.num = Math.PI;
print("num = " + obj.num);

// assignment should fail -- because we freeze
Object.freeze(obj);
print("frozen? " + Object.isFrozen(obj));

print("assign 1729 to num");
obj.num = 1729;
print("num = " + obj.num);

