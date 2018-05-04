/*
 * Copyright (c) 1997, 2017, Oracle and/or its affiliates. All rights reserved.
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

#ifndef SHARE_VM_OOPS_OBJARRAYKLASS_HPP
#define SHARE_VM_OOPS_OBJARRAYKLASS_HPP

#include "classfile/classLoaderData.hpp"
#include "oops/arrayKlass.hpp"
#include "utilities/macros.hpp"

// ObjArrayKlass is the klass for objArrays

class ObjArrayKlass : public ArrayKlass {
  friend class VMStructs;
  friend class JVMCIVMStructs;
 private:
  // If you add a new field that points to any metaspace object, you
  // must add this field to ObjArrayKlass::metaspace_pointers_do().
  Klass* _element_klass;            // The klass of the elements of this array type
  Klass* _bottom_klass;             // The one-dimensional type (InstanceKlass or TypeArrayKlass)

  // Constructor
  ObjArrayKlass(int n, Klass* element_klass, Symbol* name);
  static ObjArrayKlass* allocate(ClassLoaderData* loader_data, int n, Klass* k, Symbol* name, TRAPS);
 public:
  // For dummy objects
  ObjArrayKlass() {}

  // Instance variables
  Klass* element_klass() const      { return _element_klass; }
  void set_element_klass(Klass* k)  { _element_klass = k; }
  Klass** element_klass_addr()      { return &_element_klass; }

  Klass* bottom_klass() const       { return _bottom_klass; }
  void set_bottom_klass(Klass* k)   { _bottom_klass = k; }
  Klass** bottom_klass_addr()       { return &_bottom_klass; }

  ModuleEntry* module() const;
  PackageEntry* package() const;

  // Compiler/Interpreter offset
  static ByteSize element_klass_offset() { return in_ByteSize(offset_of(ObjArrayKlass, _element_klass)); }

  // Dispatched operation
  bool can_be_primary_super_slow() const;
  GrowableArray<Klass*>* compute_secondary_supers(int num_extra_slots,
                                                  Array<Klass*>* transitive_interfaces);
  bool compute_is_subtype_of(Klass* k);
  DEBUG_ONLY(bool is_objArray_klass_slow()  const  { return true; })
  int oop_size(oop obj) const;

  // Allocation
  static Klass* allocate_objArray_klass(ClassLoaderData* loader_data,
                                          int n, Klass* element_klass, TRAPS);

  objArrayOop allocate(int length, TRAPS);
  oop multi_allocate(int rank, jint* sizes, TRAPS);

  // Copying
  void  copy_array(arrayOop s, int src_pos, arrayOop d, int dst_pos, int length, TRAPS);

  // Compute protection domain
  oop protection_domain() const { return bottom_klass()->protection_domain(); }

  virtual void metaspace_pointers_do(MetaspaceClosure* iter);

 private:
  // Either oop or narrowOop depending on UseCompressedOops.
  // must be called from within ObjArrayKlass.cpp
  template <class T> void do_copy(arrayOop s, T* src, arrayOop d,
                                  T* dst, int length, TRAPS);
 protected:
  // Returns the ObjArrayKlass for n'th dimension.
  virtual Klass* array_klass_impl(bool or_null, int n, TRAPS);

  // Returns the array class with this class as element type.
  virtual Klass* array_klass_impl(bool or_null, TRAPS);

 public:

  static ObjArrayKlass* cast(Klass* k) {
    return const_cast<ObjArrayKlass*>(cast(const_cast<const Klass*>(k)));
  }

  static const ObjArrayKlass* cast(const Klass* k) {
    assert(k->is_objArray_klass(), "cast to ObjArrayKlass");
    return static_cast<const ObjArrayKlass*>(k);
  }

  // Sizing
  static int header_size()                { return sizeof(ObjArrayKlass)/wordSize; }
  int size() const                        { return ArrayKlass::static_size(header_size()); }

  // Initialization (virtual from Klass)
  void initialize(TRAPS);

  // GC specific object visitors
  //
#if INCLUDE_PARALLELGC
  // Parallel Scavenge
  void oop_ps_push_contents(  oop obj, PSPromotionManager* pm);
  // Parallel Compact
  void oop_pc_follow_contents(oop obj, ParCompactionManager* cm);
  void oop_pc_update_pointers(oop obj, ParCompactionManager* cm);
#endif

  // Oop fields (and metadata) iterators
  //  [nv = true]  Use non-virtual calls to do_oop_nv.
  //  [nv = false] Use virtual calls to do_oop.
  //
  // The ObjArrayKlass iterators also visits the Object's klass.

 private:

  // Iterate over oop elements and metadata.
  template <bool nv, typename OopClosureType>
  inline void oop_oop_iterate(oop obj, OopClosureType* closure);

  // Iterate over oop elements within mr, and metadata.
  template <bool nv, typename OopClosureType>
  inline void oop_oop_iterate_bounded(oop obj, OopClosureType* closure, MemRegion mr);

  // Iterate over oop elements with indices within [start, end), and metadata.
  template <bool nv, class OopClosureType>
  inline void oop_oop_iterate_range(oop obj, OopClosureType* closure, int start, int end);

  // Iterate over oop elements within [start, end), and metadata.
  // Specialized for [T = oop] or [T = narrowOop].
  template <bool nv, typename T, class OopClosureType>
  inline void oop_oop_iterate_range_specialized(objArrayOop a, OopClosureType* closure, int start, int end);

 public:
  // Iterate over all oop elements.
  template <bool nv, class OopClosureType>
  inline void oop_oop_iterate_elements(objArrayOop a, OopClosureType* closure);

 private:
  // Iterate over all oop elements.
  // Specialized for [T = oop] or [T = narrowOop].
  template <bool nv, typename T, class OopClosureType>
  inline void oop_oop_iterate_elements_specialized(objArrayOop a, OopClosureType* closure);

  // Iterate over all oop elements with indices within mr.
  template <bool nv, class OopClosureType>
  inline void oop_oop_iterate_elements_bounded(objArrayOop a, OopClosureType* closure, MemRegion mr);

  // Iterate over oop elements within [low, high)..
  // Specialized for [T = oop] or [T = narrowOop].
  template <bool nv, typename T, class OopClosureType>
  inline void oop_oop_iterate_elements_specialized_bounded(objArrayOop a, OopClosureType* closure, void* low, void* high);


 public:

  ALL_OOP_OOP_ITERATE_CLOSURES_1(OOP_OOP_ITERATE_DECL)
  ALL_OOP_OOP_ITERATE_CLOSURES_2(OOP_OOP_ITERATE_DECL)
  ALL_OOP_OOP_ITERATE_CLOSURES_1(OOP_OOP_ITERATE_DECL_RANGE)
  ALL_OOP_OOP_ITERATE_CLOSURES_2(OOP_OOP_ITERATE_DECL_RANGE)

#if INCLUDE_OOP_OOP_ITERATE_BACKWARDS
  ALL_OOP_OOP_ITERATE_CLOSURES_1(OOP_OOP_ITERATE_DECL_NO_BACKWARDS)
  ALL_OOP_OOP_ITERATE_CLOSURES_2(OOP_OOP_ITERATE_DECL_NO_BACKWARDS)
#endif

  // JVM support
  jint compute_modifier_flags(TRAPS) const;

 public:
  // Printing
  void print_on(outputStream* st) const;
  void print_value_on(outputStream* st) const;

  void oop_print_value_on(oop obj, outputStream* st);
#ifndef PRODUCT
  void oop_print_on      (oop obj, outputStream* st);
#endif //PRODUCT

  const char* internal_name() const;

  // Verification
  void verify_on(outputStream* st);

  void oop_verify_on(oop obj, outputStream* st);
};

#endif // SHARE_VM_OOPS_OBJARRAYKLASS_HPP
