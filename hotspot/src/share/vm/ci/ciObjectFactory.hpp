/*
 * Copyright (c) 1999, 2011, Oracle and/or its affiliates. All rights reserved.
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

#ifndef SHARE_VM_CI_CIOBJECTFACTORY_HPP
#define SHARE_VM_CI_CIOBJECTFACTORY_HPP

#include "ci/ciClassList.hpp"
#include "ci/ciObject.hpp"
#include "utilities/growableArray.hpp"

// ciObjectFactory
//
// This class handles requests for the creation of new instances
// of ciObject and its subclasses.  It contains a caching mechanism
// which ensures that for each oop, at most one ciObject is created.
// This invariant allows efficient implementation of ciObject.
class ciObjectFactory : public ResourceObj {
  friend class VMStructs;
  friend class ciEnv;

private:
  static volatile bool _initialized;
  static GrowableArray<ciObject*>* _shared_ci_objects;
  static ciSymbol*                 _shared_ci_symbols[];
  static int                       _shared_ident_limit;

  Arena*                    _arena;
  GrowableArray<ciObject*>* _ci_objects;
  GrowableArray<ciMethod*>* _unloaded_methods;
  GrowableArray<ciKlass*>* _unloaded_klasses;
  GrowableArray<ciInstance*>* _unloaded_instances;
  GrowableArray<ciReturnAddress*>* _return_addresses;
  GrowableArray<ciSymbol*>* _symbols;  // keep list of symbols created
  int                       _next_ident;

public:
  struct NonPermObject : public ResourceObj {
    ciObject*      _object;
    NonPermObject* _next;

    inline NonPermObject(NonPermObject* &bucket, oop key, ciObject* object);
    ciObject*     object()  { return _object; }
    NonPermObject* &next()  { return _next; }
  };
private:
  enum { NON_PERM_BUCKETS = 61 };
  NonPermObject* _non_perm_bucket[NON_PERM_BUCKETS];
  int _non_perm_count;

  int find(oop key, GrowableArray<ciObject*>* objects);
  bool is_found_at(int index, oop key, GrowableArray<ciObject*>* objects);
  void insert(int index, ciObject* obj, GrowableArray<ciObject*>* objects);
  ciObject* create_new_object(oop o);
  static bool is_equal(NonPermObject* p, oop key) {
    return p->object()->get_oop() == key;
  }

  NonPermObject* &find_non_perm(oop key);
  void insert_non_perm(NonPermObject* &where, oop key, ciObject* obj);

  void init_ident_of(ciObject* obj);
  void init_ident_of(ciSymbol* obj);

  Arena* arena() { return _arena; }

  void print_contents_impl();

  ciInstance* get_unloaded_instance(ciInstanceKlass* klass);

public:
  static bool is_initialized() { return _initialized; }

  static void initialize();
  void init_shared_objects();
  void remove_symbols();

  ciObjectFactory(Arena* arena, int expected_size);

  // Get the ciObject corresponding to some oop.
  ciObject* get(oop key);

  ciSymbol* get_symbol(Symbol* key);

  // Get the ciSymbol corresponding to one of the vmSymbols.
  static ciSymbol* vm_symbol_at(int index);

  // Get the ciMethod representing an unloaded/unfound method.
  ciMethod* get_unloaded_method(ciInstanceKlass* holder,
                                ciSymbol*        name,
                                ciSymbol*        signature);

  // Get a ciKlass representing an unloaded klass.
  ciKlass* get_unloaded_klass(ciKlass* accessing_klass,
                              ciSymbol* name,
                              bool create_if_not_found);

  // Get a ciInstance representing an unresolved klass mirror.
  ciInstance* get_unloaded_klass_mirror(ciKlass* type);

  // Get a ciInstance representing an unresolved method handle constant.
  ciInstance* get_unloaded_method_handle_constant(ciKlass*  holder,
                                                  ciSymbol* name,
                                                  ciSymbol* signature,
                                                  int       ref_kind);

  // Get a ciInstance representing an unresolved method type constant.
  ciInstance* get_unloaded_method_type_constant(ciSymbol* signature);


  // Get the ciMethodData representing the methodData for a method
  // with none.
  ciMethodData* get_empty_methodData();

  ciReturnAddress* get_return_address(int bci);

  void print_contents();
  void print();
};

#endif // SHARE_VM_CI_CIOBJECTFACTORY_HPP
