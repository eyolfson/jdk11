/*
 * Copyright 2000-2006 Sun Microsystems, Inc.  All Rights Reserved.
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
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 */

inline void MarkSweep::mark_object(oop obj) {
#ifndef SERIALGC
  if (UseParallelOldGC && VerifyParallelOldWithMarkSweep) {
    assert(PSParallelCompact::mark_bitmap()->is_marked(obj),
           "Should be marked in the marking bitmap");
  }
#endif // SERIALGC

  // some marks may contain information we need to preserve so we store them away
  // and overwrite the mark.  We'll restore it at the end of markSweep.
  markOop mark = obj->mark();
  obj->set_mark(markOopDesc::prototype()->set_marked());

  if (mark->must_be_preserved(obj)) {
    preserve_mark(obj, mark);
  }
}

template <class T> inline void MarkSweep::follow_root(T* p) {
  assert(!Universe::heap()->is_in_reserved(p),
         "roots shouldn't be things within the heap");
#ifdef VALIDATE_MARK_SWEEP
  if (ValidateMarkSweep) {
    guarantee(!_root_refs_stack->contains(p), "should only be in here once");
    _root_refs_stack->push(p);
  }
#endif
  T heap_oop = oopDesc::load_heap_oop(p);
  if (!oopDesc::is_null(heap_oop)) {
    oop obj = oopDesc::decode_heap_oop_not_null(heap_oop);
    if (!obj->mark()->is_marked()) {
      mark_object(obj);
      obj->follow_contents();
    }
  }
  follow_stack();
}

template <class T> inline void MarkSweep::mark_and_follow(T* p) {
//  assert(Universe::heap()->is_in_reserved(p), "should be in object space");
  T heap_oop = oopDesc::load_heap_oop(p);
  if (!oopDesc::is_null(heap_oop)) {
    oop obj = oopDesc::decode_heap_oop_not_null(heap_oop);
    if (!obj->mark()->is_marked()) {
      mark_object(obj);
      obj->follow_contents();
    }
  }
}

template <class T> inline void MarkSweep::mark_and_push(T* p) {
//  assert(Universe::heap()->is_in_reserved(p), "should be in object space");
  T heap_oop = oopDesc::load_heap_oop(p);
  if (!oopDesc::is_null(heap_oop)) {
    oop obj = oopDesc::decode_heap_oop_not_null(heap_oop);
    if (!obj->mark()->is_marked()) {
      mark_object(obj);
      _marking_stack->push(obj);
    }
  }
}

template <class T> inline void MarkSweep::adjust_pointer(T* p, bool isroot) {
  T heap_oop = oopDesc::load_heap_oop(p);
  if (!oopDesc::is_null(heap_oop)) {
    oop obj     = oopDesc::decode_heap_oop_not_null(heap_oop);
    oop new_obj = oop(obj->mark()->decode_pointer());
    assert(new_obj != NULL ||                         // is forwarding ptr?
           obj->mark() == markOopDesc::prototype() || // not gc marked?
           (UseBiasedLocking && obj->mark()->has_bias_pattern()) ||
                                                      // not gc marked?
           obj->is_shared(),                          // never forwarded?
           "should be forwarded");
    if (new_obj != NULL) {
      assert(Universe::heap()->is_in_reserved(new_obj),
             "should be in object space");
      oopDesc::encode_store_heap_oop_not_null(p, new_obj);
    }
  }
  VALIDATE_MARK_SWEEP_ONLY(track_adjusted_pointer(p, isroot));
}

template <class T> inline void MarkSweep::KeepAliveClosure::do_oop_work(T* p) {
#ifdef VALIDATE_MARK_SWEEP
  if (ValidateMarkSweep) {
    if (!Universe::heap()->is_in_reserved(p)) {
      _root_refs_stack->push(p);
    } else {
      _other_refs_stack->push(p);
    }
  }
#endif
  mark_and_push(p);
}
