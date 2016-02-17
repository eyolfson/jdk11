/*
 * Copyright (c) 2015, 2016, Oracle and/or its affiliates. All rights reserved.
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
#include "code/codeBlob.hpp"
#include "compiler/abstractCompiler.hpp"
#include "compiler/compileBroker.hpp"
#include "jvmci/jvmciCodeInstaller.hpp"
#include "jvmci/jvmciCompilerToVM.hpp"
#include "jvmci/jvmciEnv.hpp"
#include "jvmci/jvmciRuntime.hpp"
#include "jvmci/vmStructs_jvmci.hpp"
#include "oops/oop.hpp"
#include "oops/objArrayKlass.hpp"
#include "runtime/globals.hpp"
#include "runtime/sharedRuntime.hpp"
#include "runtime/thread.hpp"
#include "runtime/vm_version.hpp"

#if INCLUDE_ALL_GCS
#include "gc/g1/g1SATBCardTableModRefBS.hpp"
#include "gc/g1/heapRegion.hpp"
#endif


#define VM_STRUCTS(nonstatic_field, static_field, unchecked_nonstatic_field, volatile_nonstatic_field) \
  static_field(CompilerToVM::Data,             Klass_vtable_start_offset,              int)                                          \
  static_field(CompilerToVM::Data,             Klass_vtable_length_offset,             int)                                          \
                                                                                                                                     \
  static_field(CompilerToVM::Data,             Method_extra_stack_entries,             int)                                          \
                                                                                                                                     \
  static_field(CompilerToVM::Data,             SharedRuntime_ic_miss_stub,             address)                                      \
  static_field(CompilerToVM::Data,             SharedRuntime_handle_wrong_method_stub, address)                                      \
  static_field(CompilerToVM::Data,             SharedRuntime_deopt_blob_unpack,        address)                                      \
  static_field(CompilerToVM::Data,             SharedRuntime_deopt_blob_uncommon_trap, address)                                      \
                                                                                                                                     \
  static_field(CompilerToVM::Data,             ThreadLocalAllocBuffer_alignment_reserve, size_t)                                     \
                                                                                                                                     \
  static_field(CompilerToVM::Data,             Universe_collectedHeap,                 CollectedHeap*)                               \
  static_field(CompilerToVM::Data,             Universe_base_vtable_size,              int)                                          \
  static_field(CompilerToVM::Data,             Universe_narrow_oop_base,               address)                                      \
  static_field(CompilerToVM::Data,             Universe_narrow_oop_shift,              int)                                          \
  static_field(CompilerToVM::Data,             Universe_narrow_klass_base,             address)                                      \
  static_field(CompilerToVM::Data,             Universe_narrow_klass_shift,            int)                                          \
  static_field(CompilerToVM::Data,             Universe_non_oop_bits,                  void*)                                        \
  static_field(CompilerToVM::Data,             Universe_verify_oop_mask,               uintptr_t)                                    \
  static_field(CompilerToVM::Data,             Universe_verify_oop_bits,               uintptr_t)                                    \
                                                                                                                                     \
  static_field(CompilerToVM::Data,             _supports_inline_contig_alloc,          bool)                                         \
  static_field(CompilerToVM::Data,             _heap_end_addr,                         HeapWord**)                                   \
  static_field(CompilerToVM::Data,             _heap_top_addr,                         HeapWord**)                                   \
                                                                                                                                     \
  static_field(CompilerToVM::Data,             cardtable_start_address,                jbyte*)                                       \
  static_field(CompilerToVM::Data,             cardtable_shift,                        int)                                          \
                                                                                                                                     \
  static_field(CompilerToVM::Data,             vm_page_size,                           int)                                          \
                                                                                                                                     \
  static_field(Abstract_VM_Version,            _features,                              uint64_t)                                     \
                                                                                                                                     \
  nonstatic_field(Array<int>,                  _length,                                int)                                          \
  unchecked_nonstatic_field(Array<u1>,         _data,                                  sizeof(u1))                                   \
  unchecked_nonstatic_field(Array<u2>,         _data,                                  sizeof(u2))                                   \
  nonstatic_field(Array<Klass*>,               _length,                                int)                                          \
  nonstatic_field(Array<Klass*>,               _data[0],                               Klass*)                                       \
                                                                                                                                     \
  volatile_nonstatic_field(BasicLock,          _displaced_header,                      markOop)                                      \
                                                                                                                                     \
  static_field(CodeCache,                      _low_bound,                             address)                                      \
  static_field(CodeCache,                      _high_bound,                            address)                                      \
                                                                                                                                     \
  nonstatic_field(CollectedHeap,               _total_collections,                     unsigned int)                                 \
                                                                                                                                     \
  nonstatic_field(CompileTask,                 _num_inlined_bytecodes,                 int)                                          \
                                                                                                                                     \
  nonstatic_field(ConstantPool,                _tags,                                  Array<u1>*)                                   \
  nonstatic_field(ConstantPool,                _pool_holder,                           InstanceKlass*)                               \
  nonstatic_field(ConstantPool,                _length,                                int)                                          \
                                                                                                                                     \
  nonstatic_field(ConstMethod,                 _constants,                             ConstantPool*)                                \
  nonstatic_field(ConstMethod,                 _flags,                                 u2)                                           \
  nonstatic_field(ConstMethod,                 _code_size,                             u2)                                           \
  nonstatic_field(ConstMethod,                 _name_index,                            u2)                                           \
  nonstatic_field(ConstMethod,                 _signature_index,                       u2)                                           \
  nonstatic_field(ConstMethod,                 _max_stack,                             u2)                                           \
  nonstatic_field(ConstMethod,                 _max_locals,                            u2)                                           \
                                                                                                                                     \
  nonstatic_field(DataLayout,                  _header._struct._tag,                   u1)                                           \
  nonstatic_field(DataLayout,                  _header._struct._flags,                 u1)                                           \
  nonstatic_field(DataLayout,                  _header._struct._bci,                   u2)                                           \
  nonstatic_field(DataLayout,                  _cells[0],                              intptr_t)                                     \
                                                                                                                                     \
  nonstatic_field(Deoptimization::UnrollBlock, _size_of_deoptimized_frame,             int)                                          \
  nonstatic_field(Deoptimization::UnrollBlock, _caller_adjustment,                     int)                                          \
  nonstatic_field(Deoptimization::UnrollBlock, _number_of_frames,                      int)                                          \
  nonstatic_field(Deoptimization::UnrollBlock, _total_frame_sizes,                     int)                                          \
  nonstatic_field(Deoptimization::UnrollBlock, _frame_sizes,                           intptr_t*)                                    \
  nonstatic_field(Deoptimization::UnrollBlock, _frame_pcs,                             address*)                                     \
  nonstatic_field(Deoptimization::UnrollBlock, _initial_info,                          intptr_t)                                     \
  nonstatic_field(Deoptimization::UnrollBlock, _unpack_kind,                           int)                                          \
                                                                                                                                     \
  nonstatic_field(ExceptionTableElement,       start_pc,                                       u2)                                   \
  nonstatic_field(ExceptionTableElement,       end_pc,                                         u2)                                   \
  nonstatic_field(ExceptionTableElement,       handler_pc,                                     u2)                                   \
  nonstatic_field(ExceptionTableElement,       catch_type_index,                               u2)                                   \
                                                                                                                                     \
  nonstatic_field(Flag,                        _type,                                          const char*)                          \
  nonstatic_field(Flag,                        _name,                                          const char*)                          \
  unchecked_nonstatic_field(Flag,              _addr,                                          sizeof(void*))                        \
  nonstatic_field(Flag,                        _flags,                                         Flag::Flags)                          \
  static_field(Flag,                           flags,                                          Flag*)                                \
                                                                                                                                     \
  nonstatic_field(InstanceKlass,               _fields,                                       Array<u2>*)                            \
  nonstatic_field(InstanceKlass,               _constants,                                    ConstantPool*)                         \
  nonstatic_field(InstanceKlass,               _source_file_name_index,                       u2)                                    \
  nonstatic_field(InstanceKlass,               _init_state,                                   u1)                                    \
                                                                                                                                     \
  volatile_nonstatic_field(JavaFrameAnchor,    _last_Java_sp,                                 intptr_t*)                             \
  volatile_nonstatic_field(JavaFrameAnchor,    _last_Java_pc,                                 address)                               \
                                                                                                                                     \
  nonstatic_field(JavaThread,                  _threadObj,                                    oop)                                   \
  nonstatic_field(JavaThread,                  _anchor,                                       JavaFrameAnchor)                       \
  nonstatic_field(JavaThread,                  _vm_result,                                    oop)                                   \
  volatile_nonstatic_field(JavaThread,         _exception_oop,                                oop)                                   \
  volatile_nonstatic_field(JavaThread,         _exception_pc,                                 address)                               \
  volatile_nonstatic_field(JavaThread,         _is_method_handle_return,                      int)                                   \
  nonstatic_field(JavaThread,                  _osthread,                                     OSThread*)                             \
  nonstatic_field(JavaThread,                  _satb_mark_queue,                              SATBMarkQueue)                         \
  nonstatic_field(JavaThread,                  _dirty_card_queue,                             DirtyCardQueue)                        \
  nonstatic_field(JavaThread,                  _pending_deoptimization,                       int)                                   \
  nonstatic_field(JavaThread,                  _pending_failed_speculation,                   oop)                                   \
  nonstatic_field(JavaThread,                  _pending_transfer_to_interpreter,              bool)                                  \
  nonstatic_field(JavaThread,                  _jvmci_counters,                               jlong*)                                \
  nonstatic_field(JavaThread,                  _reserved_stack_activation,                    address)                               \
                                                                                                                                     \
  static_field(java_lang_Class,                _klass_offset,                                 int)                                   \
  static_field(java_lang_Class,                _array_klass_offset,                           int)                                   \
                                                                                                                                     \
  nonstatic_field(JVMCIEnv,                    _task,                                         CompileTask*)                          \
  nonstatic_field(JVMCIEnv,                    _jvmti_can_hotswap_or_post_breakpoint,         bool)                                  \
                                                                                                                                     \
  nonstatic_field(Klass,                       _secondary_super_cache,                        Klass*)                                \
  nonstatic_field(Klass,                       _secondary_supers,                             Array<Klass*>*)                        \
  nonstatic_field(Klass,                       _super,                                        Klass*)                                \
  nonstatic_field(Klass,                       _super_check_offset,                           juint)                                 \
  nonstatic_field(Klass,                       _subklass,                                     Klass*)                                \
  nonstatic_field(Klass,                       _layout_helper,                                jint)                                  \
  nonstatic_field(Klass,                       _prototype_header,                             markOop)                               \
  nonstatic_field(Klass,                       _next_sibling,                                 Klass*)                                \
  nonstatic_field(Klass,                       _java_mirror,                                  oop)                                   \
  nonstatic_field(Klass,                       _modifier_flags,                               jint)                                  \
  nonstatic_field(Klass,                       _access_flags,                                 AccessFlags)                           \
                                                                                                                                     \
  nonstatic_field(LocalVariableTableElement,   start_bci,                                     u2)                                    \
  nonstatic_field(LocalVariableTableElement,   length,                                        u2)                                    \
  nonstatic_field(LocalVariableTableElement,   name_cp_index,                                 u2)                                    \
  nonstatic_field(LocalVariableTableElement,   descriptor_cp_index,                           u2)                                    \
  nonstatic_field(LocalVariableTableElement,   signature_cp_index,                            u2)                                    \
  nonstatic_field(LocalVariableTableElement,   slot,                                          u2)                                    \
                                                                                                                                     \
  nonstatic_field(Method,                      _constMethod,                                  ConstMethod*)                          \
  nonstatic_field(Method,                      _method_data,                                  MethodData*)                           \
  nonstatic_field(Method,                      _method_counters,                              MethodCounters*)                       \
  nonstatic_field(Method,                      _access_flags,                                 AccessFlags)                           \
  nonstatic_field(Method,                      _vtable_index,                                 int)                                   \
  nonstatic_field(Method,                      _intrinsic_id,                                 u2)                                    \
  nonstatic_field(Method,                      _flags,                                        u2)                                    \
  volatile_nonstatic_field(Method,             _code,                                         nmethod*)                              \
  volatile_nonstatic_field(Method,             _from_compiled_entry,                          address)                               \
                                                                                                                                     \
  nonstatic_field(MethodCounters,              _invocation_counter,                           InvocationCounter)                     \
  nonstatic_field(MethodCounters,              _backedge_counter,                             InvocationCounter)                     \
                                                                                                                                     \
  nonstatic_field(MethodData,                  _size,                                         int)                                   \
  nonstatic_field(MethodData,                  _data_size,                                    int)                                   \
  nonstatic_field(MethodData,                  _data[0],                                      intptr_t)                              \
  nonstatic_field(MethodData,                  _trap_hist._array[0],                          u1)                                    \
  nonstatic_field(MethodData,                  _jvmci_ir_size,                                int)                                   \
                                                                                                                                     \
  nonstatic_field(nmethod,                     _verified_entry_point,                         address)                               \
  nonstatic_field(nmethod,                     _comp_level,                                   int)                                   \
                                                                                                                                     \
  nonstatic_field(ObjArrayKlass,               _element_klass,                                Klass*)                                \
                                                                                                                                     \
  volatile_nonstatic_field(oopDesc,            _mark,                                         markOop)                               \
  volatile_nonstatic_field(oopDesc,            _metadata._klass,                              Klass*)                                \
                                                                                                                                     \
  static_field(os,                             _polling_page,                                 address)                               \
                                                                                                                                     \
  volatile_nonstatic_field(OSThread,           _interrupted,                                  jint)                                  \
                                                                                                                                     \
  static_field(StubRoutines,                _verify_oop_count,                                jint)                                  \
                                                                                                                                     \
  static_field(StubRoutines,                _throw_delayed_StackOverflowError_entry,          address)                               \
                                                                                                                                     \
  static_field(StubRoutines,                _jbyte_arraycopy,                                 address)                               \
  static_field(StubRoutines,                _jshort_arraycopy,                                address)                               \
  static_field(StubRoutines,                _jint_arraycopy,                                  address)                               \
  static_field(StubRoutines,                _jlong_arraycopy,                                 address)                               \
  static_field(StubRoutines,                _oop_arraycopy,                                   address)                               \
  static_field(StubRoutines,                _oop_arraycopy_uninit,                            address)                               \
  static_field(StubRoutines,                _jbyte_disjoint_arraycopy,                        address)                               \
  static_field(StubRoutines,                _jshort_disjoint_arraycopy,                       address)                               \
  static_field(StubRoutines,                _jint_disjoint_arraycopy,                         address)                               \
  static_field(StubRoutines,                _jlong_disjoint_arraycopy,                        address)                               \
  static_field(StubRoutines,                _oop_disjoint_arraycopy,                          address)                               \
  static_field(StubRoutines,                _oop_disjoint_arraycopy_uninit,                   address)                               \
  static_field(StubRoutines,                _arrayof_jbyte_arraycopy,                         address)                               \
  static_field(StubRoutines,                _arrayof_jshort_arraycopy,                        address)                               \
  static_field(StubRoutines,                _arrayof_jint_arraycopy,                          address)                               \
  static_field(StubRoutines,                _arrayof_jlong_arraycopy,                         address)                               \
  static_field(StubRoutines,                _arrayof_oop_arraycopy,                           address)                               \
  static_field(StubRoutines,                _arrayof_oop_arraycopy_uninit,                    address)                               \
  static_field(StubRoutines,                _arrayof_jbyte_disjoint_arraycopy,                address)                               \
  static_field(StubRoutines,                _arrayof_jshort_disjoint_arraycopy,               address)                               \
  static_field(StubRoutines,                _arrayof_jint_disjoint_arraycopy,                 address)                               \
  static_field(StubRoutines,                _arrayof_jlong_disjoint_arraycopy,                address)                               \
  static_field(StubRoutines,                _arrayof_oop_disjoint_arraycopy,                  address)                               \
  static_field(StubRoutines,                _arrayof_oop_disjoint_arraycopy_uninit,           address)                               \
  static_field(StubRoutines,                _checkcast_arraycopy,                             address)                               \
  static_field(StubRoutines,                _checkcast_arraycopy_uninit,                      address)                               \
  static_field(StubRoutines,                _unsafe_arraycopy,                                address)                               \
  static_field(StubRoutines,                _generic_arraycopy,                               address)                               \
                                                                                                                                     \
  static_field(StubRoutines,                _aescrypt_encryptBlock,                           address)                               \
  static_field(StubRoutines,                _aescrypt_decryptBlock,                           address)                               \
  static_field(StubRoutines,                _cipherBlockChaining_encryptAESCrypt,             address)                               \
  static_field(StubRoutines,                _cipherBlockChaining_decryptAESCrypt,             address)                               \
  static_field(StubRoutines,                _updateBytesCRC32,                                address)                               \
  static_field(StubRoutines,                _crc_table_adr,                                   address)                               \
                                                                                                                                     \
  nonstatic_field(Thread,                   _tlab,                                            ThreadLocalAllocBuffer)                \
  nonstatic_field(Thread,                   _allocated_bytes,                                 jlong)                                 \
                                                                                                                                     \
  nonstatic_field(ThreadLocalAllocBuffer,   _start,                                           HeapWord*)                             \
  nonstatic_field(ThreadLocalAllocBuffer,   _top,                                             HeapWord*)                             \
  nonstatic_field(ThreadLocalAllocBuffer,   _end,                                             HeapWord*)                             \
  nonstatic_field(ThreadLocalAllocBuffer,   _pf_top,                                          HeapWord*)                             \
  nonstatic_field(ThreadLocalAllocBuffer,   _desired_size,                                    size_t)                                \
  nonstatic_field(ThreadLocalAllocBuffer,   _refill_waste_limit,                              size_t)                                \
  nonstatic_field(ThreadLocalAllocBuffer,   _number_of_refills,                               unsigned)                              \
  nonstatic_field(ThreadLocalAllocBuffer,   _fast_refill_waste,                               unsigned)                              \
  nonstatic_field(ThreadLocalAllocBuffer,   _slow_allocations,                                unsigned)                              \
                                                                                                                                     \
  nonstatic_field(ThreadShadow,             _pending_exception,                               oop)                                   \
                                                                                                                                     \
  static_field(vmSymbols,                   _symbols[0],                                      Symbol*)                               \
                                                                                                                                     \
  nonstatic_field(vtableEntry,              _method,                                          Method*)                               \

#define VM_TYPES(declare_type, declare_toplevel_type, declare_integer_type, declare_unsigned_integer_type) \
  declare_integer_type(bool)                                              \
  declare_unsigned_integer_type(size_t)                                   \
  declare_integer_type(intx)                                              \
  declare_unsigned_integer_type(uintx)                                    \
                                                                          \
  declare_toplevel_type(BasicLock)                                        \
  declare_toplevel_type(CompilerToVM)                                     \
  declare_toplevel_type(ExceptionTableElement)                            \
  declare_toplevel_type(Flag)                                             \
  declare_toplevel_type(Flag*)                                            \
  declare_toplevel_type(JVMCIEnv)                                         \
  declare_toplevel_type(LocalVariableTableElement)                        \
  declare_toplevel_type(narrowKlass)                                      \
  declare_toplevel_type(Symbol*)                                          \
  declare_toplevel_type(vtableEntry)                                      \
                                                                          \
  declare_toplevel_type(oopDesc)                                          \
    declare_type(arrayOopDesc, oopDesc)                                   \
                                                                          \
  declare_toplevel_type(MetaspaceObj)                                     \
    declare_type(Metadata, MetaspaceObj)                                  \
    declare_type(Klass, Metadata)                                         \
      declare_type(InstanceKlass, Klass)                                  \
    declare_type(ConstantPool, Metadata)                                  \

#define VM_INT_CONSTANTS(declare_constant, declare_constant_with_value, declare_preprocessor_constant) \
  declare_preprocessor_constant("ASSERT", DEBUG_ONLY(1) NOT_DEBUG(0))     \
  declare_preprocessor_constant("FIELDINFO_TAG_SIZE", FIELDINFO_TAG_SIZE) \
  declare_preprocessor_constant("STACK_BIAS", STACK_BIAS)                 \
                                                                          \
  declare_constant(CompLevel_full_optimization)                           \
  declare_constant(HeapWordSize)                                          \
  declare_constant(InvocationEntryBci)                                    \
  declare_constant(LogKlassAlignmentInBytes)                              \
                                                                          \
  declare_constant(JVM_ACC_WRITTEN_FLAGS)                                 \
  declare_constant(JVM_ACC_MONITOR_MATCH)                                 \
  declare_constant(JVM_ACC_HAS_MONITOR_BYTECODES)                         \
  declare_constant(JVM_ACC_HAS_FINALIZER)                                 \
  declare_constant(JVM_ACC_FIELD_INTERNAL)                                \
  declare_constant(JVM_ACC_FIELD_STABLE)                                  \
  declare_constant(JVM_ACC_FIELD_HAS_GENERIC_SIGNATURE)                   \
  declare_preprocessor_constant("JVM_ACC_SYNTHETIC", JVM_ACC_SYNTHETIC)   \
  declare_preprocessor_constant("JVM_RECOGNIZED_FIELD_MODIFIERS", JVM_RECOGNIZED_FIELD_MODIFIERS) \
                                                                          \
  declare_constant(JVM_CONSTANT_Utf8)                                     \
  declare_constant(JVM_CONSTANT_Unicode)                                  \
  declare_constant(JVM_CONSTANT_Integer)                                  \
  declare_constant(JVM_CONSTANT_Float)                                    \
  declare_constant(JVM_CONSTANT_Long)                                     \
  declare_constant(JVM_CONSTANT_Double)                                   \
  declare_constant(JVM_CONSTANT_Class)                                    \
  declare_constant(JVM_CONSTANT_String)                                   \
  declare_constant(JVM_CONSTANT_Fieldref)                                 \
  declare_constant(JVM_CONSTANT_Methodref)                                \
  declare_constant(JVM_CONSTANT_InterfaceMethodref)                       \
  declare_constant(JVM_CONSTANT_NameAndType)                              \
  declare_constant(JVM_CONSTANT_MethodHandle)                             \
  declare_constant(JVM_CONSTANT_MethodType)                               \
  declare_constant(JVM_CONSTANT_InvokeDynamic)                            \
  declare_constant(JVM_CONSTANT_ExternalMax)                              \
                                                                          \
  declare_constant(JVM_CONSTANT_Invalid)                                  \
  declare_constant(JVM_CONSTANT_InternalMin)                              \
  declare_constant(JVM_CONSTANT_UnresolvedClass)                          \
  declare_constant(JVM_CONSTANT_ClassIndex)                               \
  declare_constant(JVM_CONSTANT_StringIndex)                              \
  declare_constant(JVM_CONSTANT_UnresolvedClassInError)                   \
  declare_constant(JVM_CONSTANT_MethodHandleInError)                      \
  declare_constant(JVM_CONSTANT_MethodTypeInError)                        \
  declare_constant(JVM_CONSTANT_InternalMax)                              \
                                                                          \
  declare_constant(ArrayData::array_len_off_set)                          \
  declare_constant(ArrayData::array_start_off_set)                        \
                                                                          \
  declare_constant(BitData::exception_seen_flag)                          \
  declare_constant(BitData::null_seen_flag)                               \
  declare_constant(BranchData::not_taken_off_set)                         \
                                                                          \
  declare_constant_with_value("CardTableModRefBS::dirty_card", CardTableModRefBS::dirty_card_val()) \
                                                                          \
  declare_constant(CodeInstaller::VERIFIED_ENTRY)                         \
  declare_constant(CodeInstaller::UNVERIFIED_ENTRY)                       \
  declare_constant(CodeInstaller::OSR_ENTRY)                              \
  declare_constant(CodeInstaller::EXCEPTION_HANDLER_ENTRY)                \
  declare_constant(CodeInstaller::DEOPT_HANDLER_ENTRY)                    \
  declare_constant(CodeInstaller::INVOKEINTERFACE)                        \
  declare_constant(CodeInstaller::INVOKEVIRTUAL)                          \
  declare_constant(CodeInstaller::INVOKESTATIC)                           \
  declare_constant(CodeInstaller::INVOKESPECIAL)                          \
  declare_constant(CodeInstaller::INLINE_INVOKE)                          \
  declare_constant(CodeInstaller::POLL_NEAR)                              \
  declare_constant(CodeInstaller::POLL_RETURN_NEAR)                       \
  declare_constant(CodeInstaller::POLL_FAR)                               \
  declare_constant(CodeInstaller::POLL_RETURN_FAR)                        \
  declare_constant(CodeInstaller::CARD_TABLE_SHIFT)                       \
  declare_constant(CodeInstaller::CARD_TABLE_ADDRESS)                     \
  declare_constant(CodeInstaller::HEAP_TOP_ADDRESS)                       \
  declare_constant(CodeInstaller::HEAP_END_ADDRESS)                       \
  declare_constant(CodeInstaller::NARROW_KLASS_BASE_ADDRESS)              \
  declare_constant(CodeInstaller::CRC_TABLE_ADDRESS)                      \
  declare_constant(CodeInstaller::INVOKE_INVALID)                         \
                                                                          \
  declare_constant(ConstantPool::CPCACHE_INDEX_TAG)                       \
                                                                          \
  declare_constant(ConstMethod::_has_linenumber_table)                    \
  declare_constant(ConstMethod::_has_localvariable_table)                 \
  declare_constant(ConstMethod::_has_exception_table)                     \
                                                                          \
  declare_constant(CounterData::count_off)                                \
                                                                          \
  declare_constant(DataLayout::cell_size)                                 \
  declare_constant(DataLayout::no_tag)                                    \
  declare_constant(DataLayout::bit_data_tag)                              \
  declare_constant(DataLayout::counter_data_tag)                          \
  declare_constant(DataLayout::jump_data_tag)                             \
  declare_constant(DataLayout::receiver_type_data_tag)                    \
  declare_constant(DataLayout::virtual_call_data_tag)                     \
  declare_constant(DataLayout::ret_data_tag)                              \
  declare_constant(DataLayout::branch_data_tag)                           \
  declare_constant(DataLayout::multi_branch_data_tag)                     \
  declare_constant(DataLayout::arg_info_data_tag)                         \
  declare_constant(DataLayout::call_type_data_tag)                        \
  declare_constant(DataLayout::virtual_call_type_data_tag)                \
  declare_constant(DataLayout::parameters_type_data_tag)                  \
  declare_constant(DataLayout::speculative_trap_data_tag)                 \
                                                                          \
  declare_constant(Deoptimization::Unpack_deopt)                          \
  declare_constant(Deoptimization::Unpack_exception)                      \
  declare_constant(Deoptimization::Unpack_uncommon_trap)                  \
  declare_constant(Deoptimization::Unpack_reexecute)                      \
                                                                          \
  declare_constant(Deoptimization::_action_bits)                          \
  declare_constant(Deoptimization::_reason_bits)                          \
  declare_constant(Deoptimization::_debug_id_bits)                        \
  declare_constant(Deoptimization::_action_shift)                         \
  declare_constant(Deoptimization::_reason_shift)                         \
  declare_constant(Deoptimization::_debug_id_shift)                       \
                                                                          \
  declare_constant(Deoptimization::Action_none)                           \
  declare_constant(Deoptimization::Action_maybe_recompile)                \
  declare_constant(Deoptimization::Action_reinterpret)                    \
  declare_constant(Deoptimization::Action_make_not_entrant)               \
  declare_constant(Deoptimization::Action_make_not_compilable)            \
                                                                          \
  declare_constant(Deoptimization::Reason_none)                           \
  declare_constant(Deoptimization::Reason_null_check)                     \
  declare_constant(Deoptimization::Reason_range_check)                    \
  declare_constant(Deoptimization::Reason_class_check)                    \
  declare_constant(Deoptimization::Reason_array_check)                    \
  declare_constant(Deoptimization::Reason_unreached0)                     \
  declare_constant(Deoptimization::Reason_constraint)                     \
  declare_constant(Deoptimization::Reason_div0_check)                     \
  declare_constant(Deoptimization::Reason_loop_limit_check)               \
  declare_constant(Deoptimization::Reason_type_checked_inlining)          \
  declare_constant(Deoptimization::Reason_optimized_type_check)           \
  declare_constant(Deoptimization::Reason_aliasing)                       \
  declare_constant(Deoptimization::Reason_transfer_to_interpreter)        \
  declare_constant(Deoptimization::Reason_not_compiled_exception_handler) \
  declare_constant(Deoptimization::Reason_unresolved)                     \
  declare_constant(Deoptimization::Reason_jsr_mismatch)                   \
  declare_constant(Deoptimization::Reason_LIMIT)                          \
                                                                          \
  declare_constant_with_value("dirtyCardQueueBufferOffset", in_bytes(DirtyCardQueue::byte_offset_of_buf())) \
  declare_constant_with_value("dirtyCardQueueIndexOffset", in_bytes(DirtyCardQueue::byte_offset_of_index())) \
                                                                          \
  declare_constant(FieldInfo::access_flags_offset)                        \
  declare_constant(FieldInfo::name_index_offset)                          \
  declare_constant(FieldInfo::signature_index_offset)                     \
  declare_constant(FieldInfo::initval_index_offset)                       \
  declare_constant(FieldInfo::low_packed_offset)                          \
  declare_constant(FieldInfo::high_packed_offset)                         \
  declare_constant(FieldInfo::field_slots)                                \
                                                                          \
  declare_constant(InstanceKlass::linked)                                 \
  declare_constant(InstanceKlass::fully_initialized)                      \
                                                                          \
  declare_constant(JumpData::taken_off_set)                               \
  declare_constant(JumpData::displacement_off_set)                        \
                                                                          \
  declare_constant(JVMCIEnv::ok)                                          \
  declare_constant(JVMCIEnv::dependencies_failed)                         \
  declare_constant(JVMCIEnv::dependencies_invalid)                        \
  declare_constant(JVMCIEnv::cache_full)                                  \
  declare_constant(JVMCIEnv::code_too_large)                              \
                                                                          \
  declare_constant(Klass::_lh_neutral_value)                              \
  declare_constant(Klass::_lh_instance_slow_path_bit)                     \
  declare_constant(Klass::_lh_log2_element_size_shift)                    \
  declare_constant(Klass::_lh_log2_element_size_mask)                     \
  declare_constant(Klass::_lh_element_type_shift)                         \
  declare_constant(Klass::_lh_element_type_mask)                          \
  declare_constant(Klass::_lh_header_size_shift)                          \
  declare_constant(Klass::_lh_header_size_mask)                           \
  declare_constant(Klass::_lh_array_tag_shift)                            \
  declare_constant(Klass::_lh_array_tag_type_value)                       \
  declare_constant(Klass::_lh_array_tag_obj_value)                        \
                                                                          \
  declare_constant(markOopDesc::no_hash)                                  \
                                                                          \
  declare_constant(Method::_jfr_towrite)                                  \
  declare_constant(Method::_caller_sensitive)                             \
  declare_constant(Method::_force_inline)                                 \
  declare_constant(Method::_dont_inline)                                  \
  declare_constant(Method::_hidden)                                       \
  declare_constant(Method::_reserved_stack_access)                        \
                                                                          \
  declare_constant(Method::nonvirtual_vtable_index)                       \
  declare_constant(Method::invalid_vtable_index)                          \
                                                                          \
  declare_constant(MultiBranchData::per_case_cell_count)                  \
                                                                          \
  declare_constant(ReceiverTypeData::nonprofiled_count_off_set)           \
  declare_constant(ReceiverTypeData::receiver_type_row_cell_count)        \
  declare_constant(ReceiverTypeData::receiver0_offset)                    \
  declare_constant(ReceiverTypeData::count0_offset)                       \
                                                                          \
  declare_constant_with_value("satbMarkQueueBufferOffset", in_bytes(SATBMarkQueue::byte_offset_of_buf())) \
  declare_constant_with_value("satbMarkQueueIndexOffset", in_bytes(SATBMarkQueue::byte_offset_of_index())) \
  declare_constant_with_value("satbMarkQueueActiveOffset", in_bytes(SATBMarkQueue::byte_offset_of_active())) \
                                                                          \
  declare_constant(vmIntrinsics::_invokeBasic)                            \
  declare_constant(vmIntrinsics::_linkToVirtual)                          \
  declare_constant(vmIntrinsics::_linkToStatic)                           \
  declare_constant(vmIntrinsics::_linkToSpecial)                          \
  declare_constant(vmIntrinsics::_linkToInterface)                        \
                                                                          \
  declare_constant(vmSymbols::FIRST_SID)                                  \
  declare_constant(vmSymbols::SID_LIMIT)                                  \

#define VM_LONG_CONSTANTS(declare_constant, declare_preprocessor_constant) \
  declare_constant(InvocationCounter::count_increment)                    \
  declare_constant(InvocationCounter::count_shift)                        \
                                                                          \
  declare_constant(markOopDesc::hash_shift)                               \
                                                                          \
  declare_constant(markOopDesc::biased_lock_mask_in_place)                \
  declare_constant(markOopDesc::age_mask_in_place)                        \
  declare_constant(markOopDesc::epoch_mask_in_place)                      \
  declare_constant(markOopDesc::hash_mask)                                \
  declare_constant(markOopDesc::hash_mask_in_place)                       \
                                                                          \
  declare_constant(markOopDesc::unlocked_value)                           \
  declare_constant(markOopDesc::biased_lock_pattern)                      \
                                                                          \
  declare_constant(markOopDesc::no_hash_in_place)                         \
  declare_constant(markOopDesc::no_lock_in_place)                         \

#define VM_ADDRESSES(declare_address, declare_preprocessor_address, declare_function) \
  declare_function(SharedRuntime::register_finalizer)                     \
  declare_function(SharedRuntime::exception_handler_for_return_address)   \
  declare_function(SharedRuntime::OSR_migration_end)                      \
  declare_function(SharedRuntime::enable_stack_reserved_zone)             \
  declare_function(SharedRuntime::dsin)                                   \
  declare_function(SharedRuntime::dcos)                                   \
  declare_function(SharedRuntime::dtan)                                   \
  declare_function(SharedRuntime::dexp)                                   \
  declare_function(SharedRuntime::dlog)                                   \
  declare_function(SharedRuntime::dlog10)                                 \
  declare_function(SharedRuntime::dpow)                                   \
                                                                          \
  declare_function(os::dll_load)                                          \
  declare_function(os::dll_lookup)                                        \
  declare_function(os::javaTimeMillis)                                    \
  declare_function(os::javaTimeNanos)                                     \
                                                                          \
  declare_function(Deoptimization::fetch_unroll_info)                     \
  COMPILER2_PRESENT(declare_function(Deoptimization::uncommon_trap))      \
  declare_function(Deoptimization::unpack_frames)                         \
                                                                          \
  declare_function(JVMCIRuntime::new_instance) \
  declare_function(JVMCIRuntime::new_array) \
  declare_function(JVMCIRuntime::new_multi_array) \
  declare_function(JVMCIRuntime::dynamic_new_array) \
  declare_function(JVMCIRuntime::dynamic_new_instance) \
  \
  declare_function(JVMCIRuntime::thread_is_interrupted) \
  declare_function(JVMCIRuntime::vm_message) \
  declare_function(JVMCIRuntime::identity_hash_code) \
  declare_function(JVMCIRuntime::exception_handler_for_pc) \
  declare_function(JVMCIRuntime::monitorenter) \
  declare_function(JVMCIRuntime::monitorexit) \
  declare_function(JVMCIRuntime::create_null_exception) \
  declare_function(JVMCIRuntime::create_out_of_bounds_exception) \
  declare_function(JVMCIRuntime::log_primitive) \
  declare_function(JVMCIRuntime::log_object) \
  declare_function(JVMCIRuntime::log_printf) \
  declare_function(JVMCIRuntime::vm_error) \
  declare_function(JVMCIRuntime::load_and_clear_exception) \
  declare_function(JVMCIRuntime::write_barrier_pre) \
  declare_function(JVMCIRuntime::write_barrier_post) \
  declare_function(JVMCIRuntime::validate_object) \
  \
  declare_function(JVMCIRuntime::test_deoptimize_call_int)


#if INCLUDE_ALL_GCS

#define VM_STRUCTS_G1(nonstatic_field, static_field) \
  static_field(HeapRegion, LogOfHRGrainBytes, int)

#define VM_INT_CONSTANTS_G1(declare_constant, declare_constant_with_value, declare_preprocessor_constant) \
  declare_constant_with_value("G1SATBCardTableModRefBS::g1_young_gen", G1SATBCardTableModRefBS::g1_young_card_val())

#endif // INCLUDE_ALL_GCS


#ifdef TARGET_OS_FAMILY_linux

#define VM_ADDRESSES_OS(declare_address, declare_preprocessor_address, declare_function) \
  declare_preprocessor_address("RTLD_DEFAULT", RTLD_DEFAULT)

#endif // TARGET_OS_FAMILY_linux


#ifdef TARGET_OS_FAMILY_bsd

#define VM_ADDRESSES_OS(declare_address, declare_preprocessor_address, declare_function) \
  declare_preprocessor_address("RTLD_DEFAULT", RTLD_DEFAULT)

#endif // TARGET_OS_FAMILY_bsd


#ifdef TARGET_ARCH_x86

#define VM_STRUCTS_CPU(nonstatic_field, static_field, unchecked_nonstatic_field, volatile_nonstatic_field, nonproduct_nonstatic_field, c2_nonstatic_field, unchecked_c1_static_field, unchecked_c2_static_field) \
  volatile_nonstatic_field(JavaFrameAnchor, _last_Java_fp, intptr_t*)

#define VM_INT_CONSTANTS_CPU(declare_constant, declare_preprocessor_constant, declare_c1_constant, declare_c2_constant, declare_c2_preprocessor_constant) \
  LP64_ONLY(declare_constant(frame::arg_reg_save_area_bytes))       \
  declare_constant(frame::interpreter_frame_sender_sp_offset)       \
  declare_constant(frame::interpreter_frame_last_sp_offset)         \
  declare_constant(VM_Version::CPU_CX8)                             \
  declare_constant(VM_Version::CPU_CMOV)                            \
  declare_constant(VM_Version::CPU_FXSR)                            \
  declare_constant(VM_Version::CPU_HT)                              \
  declare_constant(VM_Version::CPU_MMX)                             \
  declare_constant(VM_Version::CPU_3DNOW_PREFETCH)                  \
  declare_constant(VM_Version::CPU_SSE)                             \
  declare_constant(VM_Version::CPU_SSE2)                            \
  declare_constant(VM_Version::CPU_SSE3)                            \
  declare_constant(VM_Version::CPU_SSSE3)                           \
  declare_constant(VM_Version::CPU_SSE4A)                           \
  declare_constant(VM_Version::CPU_SSE4_1)                          \
  declare_constant(VM_Version::CPU_SSE4_2)                          \
  declare_constant(VM_Version::CPU_POPCNT)                          \
  declare_constant(VM_Version::CPU_LZCNT)                           \
  declare_constant(VM_Version::CPU_TSC)                             \
  declare_constant(VM_Version::CPU_TSCINV)                          \
  declare_constant(VM_Version::CPU_AVX)                             \
  declare_constant(VM_Version::CPU_AVX2)                            \
  declare_constant(VM_Version::CPU_AES)                             \
  declare_constant(VM_Version::CPU_ERMS)                            \
  declare_constant(VM_Version::CPU_CLMUL)                           \
  declare_constant(VM_Version::CPU_BMI1)                            \
  declare_constant(VM_Version::CPU_BMI2)                            \
  declare_constant(VM_Version::CPU_RTM)                             \
  declare_constant(VM_Version::CPU_ADX)                             \
  declare_constant(VM_Version::CPU_AVX512F)                         \
  declare_constant(VM_Version::CPU_AVX512DQ)                        \
  declare_constant(VM_Version::CPU_AVX512PF)                        \
  declare_constant(VM_Version::CPU_AVX512ER)                        \
  declare_constant(VM_Version::CPU_AVX512CD)                        \
  declare_constant(VM_Version::CPU_AVX512BW)

#define VM_LONG_CONSTANTS_CPU(declare_constant, declare_preprocessor_constant, declare_c1_constant, declare_c2_constant, declare_c2_preprocessor_constant) \
  declare_preprocessor_constant("VM_Version::CPU_AVX512VL", CPU_AVX512VL)

#endif // TARGET_ARCH_x86


#ifdef TARGET_ARCH_sparc

#define VM_STRUCTS_CPU(nonstatic_field, static_field, unchecked_nonstatic_field, volatile_nonstatic_field, nonproduct_nonstatic_field, c2_nonstatic_field, unchecked_c1_static_field, unchecked_c2_static_field) \
  volatile_nonstatic_field(JavaFrameAnchor, _flags, int)

#define VM_INT_CONSTANTS_CPU(declare_constant, declare_preprocessor_constant, declare_c1_constant, declare_c2_constant, declare_c2_preprocessor_constant) \
  declare_constant(VM_Version::vis1_instructions_m)                       \
  declare_constant(VM_Version::vis2_instructions_m)                       \
  declare_constant(VM_Version::vis3_instructions_m)                       \
  declare_constant(VM_Version::cbcond_instructions_m)                     \
  declare_constant(VM_Version::v8_instructions_m)                         \
  declare_constant(VM_Version::hardware_mul32_m)                          \
  declare_constant(VM_Version::hardware_div32_m)                          \
  declare_constant(VM_Version::hardware_fsmuld_m)                         \
  declare_constant(VM_Version::hardware_popc_m)                           \
  declare_constant(VM_Version::v9_instructions_m)                         \
  declare_constant(VM_Version::sun4v_m)                                   \
  declare_constant(VM_Version::blk_init_instructions_m)                   \
  declare_constant(VM_Version::fmaf_instructions_m)                       \
  declare_constant(VM_Version::fmau_instructions_m)                       \
  declare_constant(VM_Version::sparc64_family_m)                          \
  declare_constant(VM_Version::M_family_m)                                \
  declare_constant(VM_Version::T_family_m)                                \
  declare_constant(VM_Version::T1_model_m)                                \
  declare_constant(VM_Version::sparc5_instructions_m)                     \
  declare_constant(VM_Version::aes_instructions_m)                        \
  declare_constant(VM_Version::sha1_instruction_m)                        \
  declare_constant(VM_Version::sha256_instruction_m)                      \
  declare_constant(VM_Version::sha512_instruction_m)

#endif // TARGET_ARCH_sparc


/*
 * Dummy defines for architectures that don't use these.
 */
#ifndef VM_STRUCTS_CPU
#define VM_STRUCTS_CPU(nonstatic_field, static_field, unchecked_nonstatic_field, volatile_nonstatic_field, nonproduct_nonstatic_field, c2_nonstatic_field, unchecked_c1_static_field, unchecked_c2_static_field)
#endif

#ifndef VM_TYPES_CPU
#define VM_TYPES_CPU(declare_type, declare_toplevel_type, declare_oop_type, declare_integer_type, declare_unsigned_integer_type, declare_c1_toplevel_type, declare_c2_type, declare_c2_toplevel_type)
#endif

#ifndef VM_INT_CONSTANTS_CPU
#define VM_INT_CONSTANTS_CPU(declare_constant, declare_preprocessor_constant, declare_c1_constant, declare_c2_constant, declare_c2_preprocessor_constant)
#endif

#ifndef VM_LONG_CONSTANTS_CPU
#define VM_LONG_CONSTANTS_CPU(declare_constant, declare_preprocessor_constant, declare_c1_constant, declare_c2_constant, declare_c2_preprocessor_constant)
#endif

#ifndef VM_STRUCTS_OS
#define VM_STRUCTS_OS(nonstatic_field, static_field, unchecked_nonstatic_field, volatile_nonstatic_field, nonproduct_nonstatic_field, c2_nonstatic_field, unchecked_c1_static_field, unchecked_c2_static_field)
#endif

#ifndef VM_TYPES_OS
#define VM_TYPES_OS(declare_type, declare_toplevel_type, declare_oop_type, declare_integer_type, declare_unsigned_integer_type, declare_c1_toplevel_type, declare_c2_type, declare_c2_toplevel_type)
#endif

#ifndef VM_INT_CONSTANTS_OS
#define VM_INT_CONSTANTS_OS(declare_constant, declare_preprocessor_constant, declare_c1_constant, declare_c2_constant, declare_c2_preprocessor_constant)
#endif

#ifndef VM_LONG_CONSTANTS_OS
#define VM_LONG_CONSTANTS_OS(declare_constant, declare_preprocessor_constant, declare_c1_constant, declare_c2_constant, declare_c2_preprocessor_constant)
#endif

#ifndef VM_ADDRESSES_OS
#define VM_ADDRESSES_OS(declare_address, declare_preprocessor_address, declare_function)
#endif


// whole purpose of this function is to work around bug c++/27724 in gcc 4.1.1
// with optimization turned on it doesn't affect produced code
static inline uint64_t cast_uint64_t(size_t x)
{
  return x;
}

#define ASSIGN_CONST_TO_64BIT_VAR(var, expr) \
  JNIEXPORT uint64_t var = cast_uint64_t(expr);

#define ASSIGN_OFFSET_TO_64BIT_VAR(var, type, field)   \
  ASSIGN_CONST_TO_64BIT_VAR(var, offset_of(type, field))

#define ASSIGN_STRIDE_TO_64BIT_VAR(var, array) \
  ASSIGN_CONST_TO_64BIT_VAR(var, (char*)&array[1] - (char*)&array[0])

//
// Instantiation of VMStructEntries, VMTypeEntries and VMIntConstantEntries
//

// These initializers are allowed to access private fields in classes
// as long as class VMStructs is a friend
VMStructEntry JVMCIVMStructs::localHotSpotVMStructs[] = {
  VM_STRUCTS(GENERATE_NONSTATIC_VM_STRUCT_ENTRY,
             GENERATE_STATIC_VM_STRUCT_ENTRY,
             GENERATE_UNCHECKED_NONSTATIC_VM_STRUCT_ENTRY,
             GENERATE_NONSTATIC_VM_STRUCT_ENTRY)

  VM_STRUCTS_OS(GENERATE_NONSTATIC_VM_STRUCT_ENTRY,
                GENERATE_STATIC_VM_STRUCT_ENTRY,
                GENERATE_UNCHECKED_NONSTATIC_VM_STRUCT_ENTRY,
                GENERATE_NONSTATIC_VM_STRUCT_ENTRY,
                GENERATE_NONPRODUCT_NONSTATIC_VM_STRUCT_ENTRY,
                GENERATE_C2_NONSTATIC_VM_STRUCT_ENTRY,
                GENERATE_C1_UNCHECKED_STATIC_VM_STRUCT_ENTRY,
                GENERATE_C2_UNCHECKED_STATIC_VM_STRUCT_ENTRY)

  VM_STRUCTS_CPU(GENERATE_NONSTATIC_VM_STRUCT_ENTRY,
                 GENERATE_STATIC_VM_STRUCT_ENTRY,
                 GENERATE_UNCHECKED_NONSTATIC_VM_STRUCT_ENTRY,
                 GENERATE_NONSTATIC_VM_STRUCT_ENTRY,
                 GENERATE_NONPRODUCT_NONSTATIC_VM_STRUCT_ENTRY,
                 GENERATE_C2_NONSTATIC_VM_STRUCT_ENTRY,
                 GENERATE_C1_UNCHECKED_STATIC_VM_STRUCT_ENTRY,
                 GENERATE_C2_UNCHECKED_STATIC_VM_STRUCT_ENTRY)

#if INCLUDE_ALL_GCS
  VM_STRUCTS_G1(GENERATE_NONSTATIC_VM_STRUCT_ENTRY,
                GENERATE_STATIC_VM_STRUCT_ENTRY)
#endif

  GENERATE_VM_STRUCT_LAST_ENTRY()
};

VMTypeEntry JVMCIVMStructs::localHotSpotVMTypes[] = {
  VM_TYPES(GENERATE_VM_TYPE_ENTRY,
           GENERATE_TOPLEVEL_VM_TYPE_ENTRY,
           GENERATE_INTEGER_VM_TYPE_ENTRY,
           GENERATE_UNSIGNED_INTEGER_VM_TYPE_ENTRY)

  VM_TYPES_OS(GENERATE_VM_TYPE_ENTRY,
              GENERATE_TOPLEVEL_VM_TYPE_ENTRY,
              GENERATE_OOP_VM_TYPE_ENTRY,
              GENERATE_INTEGER_VM_TYPE_ENTRY,
              GENERATE_UNSIGNED_INTEGER_VM_TYPE_ENTRY,
              GENERATE_C1_TOPLEVEL_VM_TYPE_ENTRY,
              GENERATE_C2_VM_TYPE_ENTRY,
              GENERATE_C2_TOPLEVEL_VM_TYPE_ENTRY)

  VM_TYPES_CPU(GENERATE_VM_TYPE_ENTRY,
               GENERATE_TOPLEVEL_VM_TYPE_ENTRY,
               GENERATE_OOP_VM_TYPE_ENTRY,
               GENERATE_INTEGER_VM_TYPE_ENTRY,
               GENERATE_UNSIGNED_INTEGER_VM_TYPE_ENTRY,
               GENERATE_C1_TOPLEVEL_VM_TYPE_ENTRY,
               GENERATE_C2_VM_TYPE_ENTRY,
               GENERATE_C2_TOPLEVEL_VM_TYPE_ENTRY)

  GENERATE_VM_TYPE_LAST_ENTRY()
};

VMIntConstantEntry JVMCIVMStructs::localHotSpotVMIntConstants[] = {
  VM_INT_CONSTANTS(GENERATE_VM_INT_CONSTANT_ENTRY,
                   GENERATE_VM_INT_CONSTANT_WITH_VALUE_ENTRY,
                   GENERATE_PREPROCESSOR_VM_INT_CONSTANT_ENTRY)

  VM_INT_CONSTANTS_OS(GENERATE_VM_INT_CONSTANT_ENTRY,
                      GENERATE_PREPROCESSOR_VM_INT_CONSTANT_ENTRY,
                      GENERATE_C1_VM_INT_CONSTANT_ENTRY,
                      GENERATE_C2_VM_INT_CONSTANT_ENTRY,
                      GENERATE_C2_PREPROCESSOR_VM_INT_CONSTANT_ENTRY)

  VM_INT_CONSTANTS_CPU(GENERATE_VM_INT_CONSTANT_ENTRY,
                       GENERATE_PREPROCESSOR_VM_INT_CONSTANT_ENTRY,
                       GENERATE_C1_VM_INT_CONSTANT_ENTRY,
                       GENERATE_C2_VM_INT_CONSTANT_ENTRY,
                       GENERATE_C2_PREPROCESSOR_VM_INT_CONSTANT_ENTRY)

#if INCLUDE_ALL_GCS
  VM_INT_CONSTANTS_G1(GENERATE_VM_INT_CONSTANT_ENTRY,
                      GENERATE_VM_INT_CONSTANT_WITH_VALUE_ENTRY,
                      GENERATE_PREPROCESSOR_VM_INT_CONSTANT_ENTRY)
#endif

  GENERATE_VM_INT_CONSTANT_LAST_ENTRY()
};

VMLongConstantEntry JVMCIVMStructs::localHotSpotVMLongConstants[] = {
  VM_LONG_CONSTANTS(GENERATE_VM_LONG_CONSTANT_ENTRY,
                    GENERATE_PREPROCESSOR_VM_LONG_CONSTANT_ENTRY)

  VM_LONG_CONSTANTS_OS(GENERATE_VM_LONG_CONSTANT_ENTRY,
                       GENERATE_PREPROCESSOR_VM_LONG_CONSTANT_ENTRY,
                       GENERATE_C1_VM_LONG_CONSTANT_ENTRY,
                       GENERATE_C2_VM_LONG_CONSTANT_ENTRY,
                       GENERATE_C2_PREPROCESSOR_VM_LONG_CONSTANT_ENTRY)

  VM_LONG_CONSTANTS_CPU(GENERATE_VM_LONG_CONSTANT_ENTRY,
                        GENERATE_PREPROCESSOR_VM_LONG_CONSTANT_ENTRY,
                        GENERATE_C1_VM_LONG_CONSTANT_ENTRY,
                        GENERATE_C2_VM_LONG_CONSTANT_ENTRY,
                        GENERATE_C2_PREPROCESSOR_VM_LONG_CONSTANT_ENTRY)

  GENERATE_VM_LONG_CONSTANT_LAST_ENTRY()
};

VMAddressEntry JVMCIVMStructs::localHotSpotVMAddresses[] = {
  VM_ADDRESSES(GENERATE_VM_ADDRESS_ENTRY,
               GENERATE_PREPROCESSOR_VM_ADDRESS_ENTRY,
               GENERATE_VM_FUNCTION_ENTRY)

  VM_ADDRESSES_OS(GENERATE_VM_ADDRESS_ENTRY,
                  GENERATE_PREPROCESSOR_VM_ADDRESS_ENTRY,
                  GENERATE_VM_FUNCTION_ENTRY)

  GENERATE_VM_ADDRESS_LAST_ENTRY()
};

extern "C" {
JNIEXPORT VMStructEntry* jvmciHotSpotVMStructs = JVMCIVMStructs::localHotSpotVMStructs;
ASSIGN_OFFSET_TO_64BIT_VAR(jvmciHotSpotVMStructEntryTypeNameOffset,   VMStructEntry, typeName);
ASSIGN_OFFSET_TO_64BIT_VAR(jvmciHotSpotVMStructEntryFieldNameOffset,  VMStructEntry, fieldName);
ASSIGN_OFFSET_TO_64BIT_VAR(jvmciHotSpotVMStructEntryTypeStringOffset, VMStructEntry, typeString);
ASSIGN_OFFSET_TO_64BIT_VAR(jvmciHotSpotVMStructEntryIsStaticOffset,   VMStructEntry, isStatic);
ASSIGN_OFFSET_TO_64BIT_VAR(jvmciHotSpotVMStructEntryOffsetOffset,     VMStructEntry, offset);
ASSIGN_OFFSET_TO_64BIT_VAR(jvmciHotSpotVMStructEntryAddressOffset,    VMStructEntry, address);
ASSIGN_STRIDE_TO_64BIT_VAR(jvmciHotSpotVMStructEntryArrayStride, jvmciHotSpotVMStructs);

JNIEXPORT VMTypeEntry* jvmciHotSpotVMTypes = JVMCIVMStructs::localHotSpotVMTypes;
ASSIGN_OFFSET_TO_64BIT_VAR(jvmciHotSpotVMTypeEntryTypeNameOffset,       VMTypeEntry, typeName);
ASSIGN_OFFSET_TO_64BIT_VAR(jvmciHotSpotVMTypeEntrySuperclassNameOffset, VMTypeEntry, superclassName);
ASSIGN_OFFSET_TO_64BIT_VAR(jvmciHotSpotVMTypeEntryIsOopTypeOffset,      VMTypeEntry, isOopType);
ASSIGN_OFFSET_TO_64BIT_VAR(jvmciHotSpotVMTypeEntryIsIntegerTypeOffset,  VMTypeEntry, isIntegerType);
ASSIGN_OFFSET_TO_64BIT_VAR(jvmciHotSpotVMTypeEntryIsUnsignedOffset,     VMTypeEntry, isUnsigned);
ASSIGN_OFFSET_TO_64BIT_VAR(jvmciHotSpotVMTypeEntrySizeOffset,           VMTypeEntry, size);
ASSIGN_STRIDE_TO_64BIT_VAR(jvmciHotSpotVMTypeEntryArrayStride, jvmciHotSpotVMTypes);

JNIEXPORT VMIntConstantEntry* jvmciHotSpotVMIntConstants = JVMCIVMStructs::localHotSpotVMIntConstants;
ASSIGN_OFFSET_TO_64BIT_VAR(jvmciHotSpotVMIntConstantEntryNameOffset,  VMIntConstantEntry, name);
ASSIGN_OFFSET_TO_64BIT_VAR(jvmciHotSpotVMIntConstantEntryValueOffset, VMIntConstantEntry, value);
ASSIGN_STRIDE_TO_64BIT_VAR(jvmciHotSpotVMIntConstantEntryArrayStride, jvmciHotSpotVMIntConstants);

JNIEXPORT VMLongConstantEntry* jvmciHotSpotVMLongConstants = JVMCIVMStructs::localHotSpotVMLongConstants;
ASSIGN_OFFSET_TO_64BIT_VAR(jvmciHotSpotVMLongConstantEntryNameOffset,  VMLongConstantEntry, name);
ASSIGN_OFFSET_TO_64BIT_VAR(jvmciHotSpotVMLongConstantEntryValueOffset, VMLongConstantEntry, value);
ASSIGN_STRIDE_TO_64BIT_VAR(jvmciHotSpotVMLongConstantEntryArrayStride, jvmciHotSpotVMLongConstants);

JNIEXPORT VMAddressEntry* jvmciHotSpotVMAddresses = JVMCIVMStructs::localHotSpotVMAddresses;
ASSIGN_OFFSET_TO_64BIT_VAR(jvmciHotSpotVMAddressEntryNameOffset,  VMAddressEntry, name);
ASSIGN_OFFSET_TO_64BIT_VAR(jvmciHotSpotVMAddressEntryValueOffset, VMAddressEntry, value);
ASSIGN_STRIDE_TO_64BIT_VAR(jvmciHotSpotVMAddressEntryArrayStride, jvmciHotSpotVMAddresses);
}
