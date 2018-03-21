/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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
 *
 */

#ifndef SHARE_VM_OOPS_METHOD_INLINE_HPP
#define SHARE_VM_OOPS_METHOD_INLINE_HPP

#include "oops/method.hpp"
#include "runtime/orderAccess.inline.hpp"

inline address Method::from_compiled_entry() const {
  return OrderAccess::load_acquire(&_from_compiled_entry);
}

inline address Method::from_interpreted_entry() const {
  return OrderAccess::load_acquire(&_from_interpreted_entry);
}

inline void Method::set_method_data(MethodData* data) {
  // The store into method must be released. On platforms without
  // total store order (TSO) the reference may become visible before
  // the initialization of data otherwise.
  OrderAccess::release_store(&_method_data, data);
}

inline CompiledMethod* volatile Method::code() const {
  assert( check_code(), "" );
  return OrderAccess::load_acquire(&_code);
}

inline bool Method::has_compiled_code() const { return code() != NULL; }

#endif // SHARE_VM_OOPS_METHOD_INLINE_HPP
