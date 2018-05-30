/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2018, SAP and/or its affiliates.
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
#include "precompiled.hpp"

#include "classfile/systemDictionary.hpp"
#include "memory/metaspace/printMetaspaceInfoKlassClosure.hpp"
#include "memory/resourceArea.hpp"
#include "oops/constantPool.inline.hpp"
#include "oops/instanceKlass.hpp"
#include "oops/klass.hpp"
#include "utilities/constantTag.hpp"
#include "utilities/globalDefinitions.hpp"
#include "utilities/ostream.hpp"


namespace metaspace {

PrintMetaspaceInfoKlassClosure::PrintMetaspaceInfoKlassClosure(outputStream* out, bool do_print)
: _out(out), _do_print(do_print)
, _num_classes(0), _num_instance_classes(0), _num_array_classes(0) {
}

void PrintMetaspaceInfoKlassClosure::do_klass(Klass* k) {
  _num_classes ++;
  if (k->is_instance_klass()) {
    _num_instance_classes ++;
  } else if (k->is_array_klass()) {
    _num_array_classes ++;
  }
  if (_do_print) {
    _out->cr_indent();
    _out->print(UINTX_FORMAT_W(4) ": ", _num_classes);
    ResourceMark rm;
    _out->print("%s", k->external_name());
  }
}

} // namespace metaspace
