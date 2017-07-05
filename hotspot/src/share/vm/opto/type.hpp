/*
 * Copyright (c) 1997, 2009, Oracle and/or its affiliates. All rights reserved.
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

// Portions of code courtesy of Clifford Click

// Optimization - Graph Style


// This class defines a Type lattice.  The lattice is used in the constant
// propagation algorithms, and for some type-checking of the iloc code.
// Basic types include RSD's (lower bound, upper bound, stride for integers),
// float & double precision constants, sets of data-labels and code-labels.
// The complete lattice is described below.  Subtypes have no relationship to
// up or down in the lattice; that is entirely determined by the behavior of
// the MEET/JOIN functions.

class Dict;
class Type;
class   TypeD;
class   TypeF;
class   TypeInt;
class   TypeLong;
class   TypeNarrowOop;
class   TypeAry;
class   TypeTuple;
class   TypePtr;
class     TypeRawPtr;
class     TypeOopPtr;
class       TypeInstPtr;
class       TypeAryPtr;
class       TypeKlassPtr;

//------------------------------Type-------------------------------------------
// Basic Type object, represents a set of primitive Values.
// Types are hash-cons'd into a private class dictionary, so only one of each
// different kind of Type exists.  Types are never modified after creation, so
// all their interesting fields are constant.
class Type {
public:
  enum TYPES {
    Bad=0,                      // Type check
    Control,                    // Control of code (not in lattice)
    Top,                        // Top of the lattice
    Int,                        // Integer range (lo-hi)
    Long,                       // Long integer range (lo-hi)
    Half,                       // Placeholder half of doubleword
    NarrowOop,                  // Compressed oop pointer

    Tuple,                      // Method signature or object layout
    Array,                      // Array types

    AnyPtr,                     // Any old raw, klass, inst, or array pointer
    RawPtr,                     // Raw (non-oop) pointers
    OopPtr,                     // Any and all Java heap entities
    InstPtr,                    // Instance pointers (non-array objects)
    AryPtr,                     // Array pointers
    KlassPtr,                   // Klass pointers
    // (Ptr order matters:  See is_ptr, isa_ptr, is_oopptr, isa_oopptr.)

    Function,                   // Function signature
    Abio,                       // Abstract I/O
    Return_Address,             // Subroutine return address
    Memory,                     // Abstract store
    FloatTop,                   // No float value
    FloatCon,                   // Floating point constant
    FloatBot,                   // Any float value
    DoubleTop,                  // No double value
    DoubleCon,                  // Double precision constant
    DoubleBot,                  // Any double value
    Bottom,                     // Bottom of lattice
    lastype                     // Bogus ending type (not in lattice)
  };

  // Signal values for offsets from a base pointer
  enum OFFSET_SIGNALS {
    OffsetTop = -2000000000,    // undefined offset
    OffsetBot = -2000000001     // any possible offset
  };

  // Min and max WIDEN values.
  enum WIDEN {
    WidenMin = 0,
    WidenMax = 3
  };

private:
  // Dictionary of types shared among compilations.
  static Dict* _shared_type_dict;

  static int uhash( const Type *const t );
  // Structural equality check.  Assumes that cmp() has already compared
  // the _base types and thus knows it can cast 't' appropriately.
  virtual bool eq( const Type *t ) const;

  // Top-level hash-table of types
  static Dict *type_dict() {
    return Compile::current()->type_dict();
  }

  // DUAL operation: reflect around lattice centerline.  Used instead of
  // join to ensure my lattice is symmetric up and down.  Dual is computed
  // lazily, on demand, and cached in _dual.
  const Type *_dual;            // Cached dual value
  // Table for efficient dualing of base types
  static const TYPES dual_type[lastype];

protected:
  // Each class of type is also identified by its base.
  const TYPES _base;            // Enum of Types type

  Type( TYPES t ) : _dual(NULL),  _base(t) {} // Simple types
  // ~Type();                   // Use fast deallocation
  const Type *hashcons();       // Hash-cons the type

public:

  inline void* operator new( size_t x ) {
    Compile* compile = Compile::current();
    compile->set_type_last_size(x);
    void *temp = compile->type_arena()->Amalloc_D(x);
    compile->set_type_hwm(temp);
    return temp;
  }
  inline void operator delete( void* ptr ) {
    Compile* compile = Compile::current();
    compile->type_arena()->Afree(ptr,compile->type_last_size());
  }

  // Initialize the type system for a particular compilation.
  static void Initialize(Compile* compile);

  // Initialize the types shared by all compilations.
  static void Initialize_shared(Compile* compile);

  TYPES base() const {
    assert(_base > Bad && _base < lastype, "sanity");
    return _base;
  }

  // Create a new hash-consd type
  static const Type *make(enum TYPES);
  // Test for equivalence of types
  static int cmp( const Type *const t1, const Type *const t2 );
  // Test for higher or equal in lattice
  int higher_equal( const Type *t ) const { return !cmp(meet(t),t); }

  // MEET operation; lower in lattice.
  const Type *meet( const Type *t ) const;
  // WIDEN: 'widens' for Ints and other range types
  virtual const Type *widen( const Type *old, const Type* limit ) const { return this; }
  // NARROW: complement for widen, used by pessimistic phases
  virtual const Type *narrow( const Type *old ) const { return this; }

  // DUAL operation: reflect around lattice centerline.  Used instead of
  // join to ensure my lattice is symmetric up and down.
  const Type *dual() const { return _dual; }

  // Compute meet dependent on base type
  virtual const Type *xmeet( const Type *t ) const;
  virtual const Type *xdual() const;    // Compute dual right now.

  // JOIN operation; higher in lattice.  Done by finding the dual of the
  // meet of the dual of the 2 inputs.
  const Type *join( const Type *t ) const {
    return dual()->meet(t->dual())->dual(); }

  // Modified version of JOIN adapted to the needs Node::Value.
  // Normalizes all empty values to TOP.  Does not kill _widen bits.
  // Currently, it also works around limitations involving interface types.
  virtual const Type *filter( const Type *kills ) const;

#ifdef ASSERT
  // One type is interface, the other is oop
  virtual bool interface_vs_oop(const Type *t) const;
#endif

  // Returns true if this pointer points at memory which contains a
  // compressed oop references.
  bool is_ptr_to_narrowoop() const;

  // Convenience access
  float getf() const;
  double getd() const;

  const TypeInt    *is_int() const;
  const TypeInt    *isa_int() const;             // Returns NULL if not an Int
  const TypeLong   *is_long() const;
  const TypeLong   *isa_long() const;            // Returns NULL if not a Long
  const TypeD      *is_double_constant() const;  // Asserts it is a DoubleCon
  const TypeD      *isa_double_constant() const; // Returns NULL if not a DoubleCon
  const TypeF      *is_float_constant() const;   // Asserts it is a FloatCon
  const TypeF      *isa_float_constant() const;  // Returns NULL if not a FloatCon
  const TypeTuple  *is_tuple() const;            // Collection of fields, NOT a pointer
  const TypeAry    *is_ary() const;              // Array, NOT array pointer
  const TypePtr    *is_ptr() const;              // Asserts it is a ptr type
  const TypePtr    *isa_ptr() const;             // Returns NULL if not ptr type
  const TypeRawPtr *isa_rawptr() const;          // NOT Java oop
  const TypeRawPtr *is_rawptr() const;           // Asserts is rawptr
  const TypeNarrowOop  *is_narrowoop() const;    // Java-style GC'd pointer
  const TypeNarrowOop  *isa_narrowoop() const;   // Returns NULL if not oop ptr type
  const TypeOopPtr   *isa_oopptr() const;        // Returns NULL if not oop ptr type
  const TypeOopPtr   *is_oopptr() const;         // Java-style GC'd pointer
  const TypeKlassPtr *isa_klassptr() const;      // Returns NULL if not KlassPtr
  const TypeKlassPtr *is_klassptr() const;       // assert if not KlassPtr
  const TypeInstPtr  *isa_instptr() const;       // Returns NULL if not InstPtr
  const TypeInstPtr  *is_instptr() const;        // Instance
  const TypeAryPtr   *isa_aryptr() const;        // Returns NULL if not AryPtr
  const TypeAryPtr   *is_aryptr() const;         // Array oop
  virtual bool      is_finite() const;           // Has a finite value
  virtual bool      is_nan()    const;           // Is not a number (NaN)

  // Returns this ptr type or the equivalent ptr type for this compressed pointer.
  const TypePtr* make_ptr() const;

  // Returns this oopptr type or the equivalent oopptr type for this compressed pointer.
  // Asserts if the underlying type is not an oopptr or narrowoop.
  const TypeOopPtr* make_oopptr() const;

  // Returns this compressed pointer or the equivalent compressed version
  // of this pointer type.
  const TypeNarrowOop* make_narrowoop() const;

  // Special test for register pressure heuristic
  bool is_floatingpoint() const;        // True if Float or Double base type

  // Do you have memory, directly or through a tuple?
  bool has_memory( ) const;

  // Are you a pointer type or not?
  bool isa_oop_ptr() const;

  // TRUE if type is a singleton
  virtual bool singleton(void) const;

  // TRUE if type is above the lattice centerline, and is therefore vacuous
  virtual bool empty(void) const;

  // Return a hash for this type.  The hash function is public so ConNode
  // (constants) can hash on their constant, which is represented by a Type.
  virtual int hash() const;

  // Map ideal registers (machine types) to ideal types
  static const Type *mreg2type[];

  // Printing, statistics
  static const char * const msg[lastype]; // Printable strings
#ifndef PRODUCT
  void         dump_on(outputStream *st) const;
  void         dump() const {
    dump_on(tty);
  }
  virtual void dump2( Dict &d, uint depth, outputStream *st ) const;
  static  void dump_stats();
  static  void verify_lastype();          // Check that arrays match type enum
#endif
  void typerr(const Type *t) const; // Mixing types error

  // Create basic type
  static const Type* get_const_basic_type(BasicType type) {
    assert((uint)type <= T_CONFLICT && _const_basic_type[type] != NULL, "bad type");
    return _const_basic_type[type];
  }

  // Mapping to the array element's basic type.
  BasicType array_element_basic_type() const;

  // Create standard type for a ciType:
  static const Type* get_const_type(ciType* type);

  // Create standard zero value:
  static const Type* get_zero_type(BasicType type) {
    assert((uint)type <= T_CONFLICT && _zero_type[type] != NULL, "bad type");
    return _zero_type[type];
  }

  // Report if this is a zero value (not top).
  bool is_zero_type() const {
    BasicType type = basic_type();
    if (type == T_VOID || type >= T_CONFLICT)
      return false;
    else
      return (this == _zero_type[type]);
  }

  // Convenience common pre-built types.
  static const Type *ABIO;
  static const Type *BOTTOM;
  static const Type *CONTROL;
  static const Type *DOUBLE;
  static const Type *FLOAT;
  static const Type *HALF;
  static const Type *MEMORY;
  static const Type *MULTI;
  static const Type *RETURN_ADDRESS;
  static const Type *TOP;

  // Mapping from compiler type to VM BasicType
  BasicType basic_type() const { return _basic_type[_base]; }

  // Mapping from CI type system to compiler type:
  static const Type* get_typeflow_type(ciType* type);

private:
  // support arrays
  static const BasicType _basic_type[];
  static const Type*        _zero_type[T_CONFLICT+1];
  static const Type* _const_basic_type[T_CONFLICT+1];
};

//------------------------------TypeF------------------------------------------
// Class of Float-Constant Types.
class TypeF : public Type {
  TypeF( float f ) : Type(FloatCon), _f(f) {};
public:
  virtual bool eq( const Type *t ) const;
  virtual int  hash() const;             // Type specific hashing
  virtual bool singleton(void) const;    // TRUE if type is a singleton
  virtual bool empty(void) const;        // TRUE if type is vacuous
public:
  const float _f;               // Float constant

  static const TypeF *make(float f);

  virtual bool        is_finite() const;  // Has a finite value
  virtual bool        is_nan()    const;  // Is not a number (NaN)

  virtual const Type *xmeet( const Type *t ) const;
  virtual const Type *xdual() const;    // Compute dual right now.
  // Convenience common pre-built types.
  static const TypeF *ZERO; // positive zero only
  static const TypeF *ONE;
#ifndef PRODUCT
  virtual void dump2( Dict &d, uint depth, outputStream *st ) const;
#endif
};

//------------------------------TypeD------------------------------------------
// Class of Double-Constant Types.
class TypeD : public Type {
  TypeD( double d ) : Type(DoubleCon), _d(d) {};
public:
  virtual bool eq( const Type *t ) const;
  virtual int  hash() const;             // Type specific hashing
  virtual bool singleton(void) const;    // TRUE if type is a singleton
  virtual bool empty(void) const;        // TRUE if type is vacuous
public:
  const double _d;              // Double constant

  static const TypeD *make(double d);

  virtual bool        is_finite() const;  // Has a finite value
  virtual bool        is_nan()    const;  // Is not a number (NaN)

  virtual const Type *xmeet( const Type *t ) const;
  virtual const Type *xdual() const;    // Compute dual right now.
  // Convenience common pre-built types.
  static const TypeD *ZERO; // positive zero only
  static const TypeD *ONE;
#ifndef PRODUCT
  virtual void dump2( Dict &d, uint depth, outputStream *st ) const;
#endif
};

//------------------------------TypeInt----------------------------------------
// Class of integer ranges, the set of integers between a lower bound and an
// upper bound, inclusive.
class TypeInt : public Type {
  TypeInt( jint lo, jint hi, int w );
public:
  virtual bool eq( const Type *t ) const;
  virtual int  hash() const;             // Type specific hashing
  virtual bool singleton(void) const;    // TRUE if type is a singleton
  virtual bool empty(void) const;        // TRUE if type is vacuous
public:
  const jint _lo, _hi;          // Lower bound, upper bound
  const short _widen;           // Limit on times we widen this sucker

  static const TypeInt *make(jint lo);
  // must always specify w
  static const TypeInt *make(jint lo, jint hi, int w);

  // Check for single integer
  int is_con() const { return _lo==_hi; }
  bool is_con(int i) const { return is_con() && _lo == i; }
  jint get_con() const { assert( is_con(), "" );  return _lo; }

  virtual bool        is_finite() const;  // Has a finite value

  virtual const Type *xmeet( const Type *t ) const;
  virtual const Type *xdual() const;    // Compute dual right now.
  virtual const Type *widen( const Type *t, const Type* limit_type ) const;
  virtual const Type *narrow( const Type *t ) const;
  // Do not kill _widen bits.
  virtual const Type *filter( const Type *kills ) const;
  // Convenience common pre-built types.
  static const TypeInt *MINUS_1;
  static const TypeInt *ZERO;
  static const TypeInt *ONE;
  static const TypeInt *BOOL;
  static const TypeInt *CC;
  static const TypeInt *CC_LT;  // [-1]  == MINUS_1
  static const TypeInt *CC_GT;  // [1]   == ONE
  static const TypeInt *CC_EQ;  // [0]   == ZERO
  static const TypeInt *CC_LE;  // [-1,0]
  static const TypeInt *CC_GE;  // [0,1] == BOOL (!)
  static const TypeInt *BYTE;
  static const TypeInt *UBYTE;
  static const TypeInt *CHAR;
  static const TypeInt *SHORT;
  static const TypeInt *POS;
  static const TypeInt *POS1;
  static const TypeInt *INT;
  static const TypeInt *SYMINT; // symmetric range [-max_jint..max_jint]
#ifndef PRODUCT
  virtual void dump2( Dict &d, uint depth, outputStream *st ) const;
#endif
};


//------------------------------TypeLong---------------------------------------
// Class of long integer ranges, the set of integers between a lower bound and
// an upper bound, inclusive.
class TypeLong : public Type {
  TypeLong( jlong lo, jlong hi, int w );
public:
  virtual bool eq( const Type *t ) const;
  virtual int  hash() const;             // Type specific hashing
  virtual bool singleton(void) const;    // TRUE if type is a singleton
  virtual bool empty(void) const;        // TRUE if type is vacuous
public:
  const jlong _lo, _hi;         // Lower bound, upper bound
  const short _widen;           // Limit on times we widen this sucker

  static const TypeLong *make(jlong lo);
  // must always specify w
  static const TypeLong *make(jlong lo, jlong hi, int w);

  // Check for single integer
  int is_con() const { return _lo==_hi; }
  bool is_con(int i) const { return is_con() && _lo == i; }
  jlong get_con() const { assert( is_con(), "" ); return _lo; }

  virtual bool        is_finite() const;  // Has a finite value

  virtual const Type *xmeet( const Type *t ) const;
  virtual const Type *xdual() const;    // Compute dual right now.
  virtual const Type *widen( const Type *t, const Type* limit_type ) const;
  virtual const Type *narrow( const Type *t ) const;
  // Do not kill _widen bits.
  virtual const Type *filter( const Type *kills ) const;
  // Convenience common pre-built types.
  static const TypeLong *MINUS_1;
  static const TypeLong *ZERO;
  static const TypeLong *ONE;
  static const TypeLong *POS;
  static const TypeLong *LONG;
  static const TypeLong *INT;    // 32-bit subrange [min_jint..max_jint]
  static const TypeLong *UINT;   // 32-bit unsigned [0..max_juint]
#ifndef PRODUCT
  virtual void dump2( Dict &d, uint, outputStream *st  ) const;// Specialized per-Type dumping
#endif
};

//------------------------------TypeTuple--------------------------------------
// Class of Tuple Types, essentially type collections for function signatures
// and class layouts.  It happens to also be a fast cache for the HotSpot
// signature types.
class TypeTuple : public Type {
  TypeTuple( uint cnt, const Type **fields ) : Type(Tuple), _cnt(cnt), _fields(fields) { }
public:
  virtual bool eq( const Type *t ) const;
  virtual int  hash() const;             // Type specific hashing
  virtual bool singleton(void) const;    // TRUE if type is a singleton
  virtual bool empty(void) const;        // TRUE if type is vacuous

public:
  const uint          _cnt;              // Count of fields
  const Type ** const _fields;           // Array of field types

  // Accessors:
  uint cnt() const { return _cnt; }
  const Type* field_at(uint i) const {
    assert(i < _cnt, "oob");
    return _fields[i];
  }
  void set_field_at(uint i, const Type* t) {
    assert(i < _cnt, "oob");
    _fields[i] = t;
  }

  static const TypeTuple *make( uint cnt, const Type **fields );
  static const TypeTuple *make_range(ciSignature *sig);
  static const TypeTuple *make_domain(ciInstanceKlass* recv, ciSignature *sig);

  // Subroutine call type with space allocated for argument types
  static const Type **fields( uint arg_cnt );

  virtual const Type *xmeet( const Type *t ) const;
  virtual const Type *xdual() const;    // Compute dual right now.
  // Convenience common pre-built types.
  static const TypeTuple *IFBOTH;
  static const TypeTuple *IFFALSE;
  static const TypeTuple *IFTRUE;
  static const TypeTuple *IFNEITHER;
  static const TypeTuple *LOOPBODY;
  static const TypeTuple *MEMBAR;
  static const TypeTuple *STORECONDITIONAL;
  static const TypeTuple *START_I2C;
  static const TypeTuple *INT_PAIR;
  static const TypeTuple *LONG_PAIR;
#ifndef PRODUCT
  virtual void dump2( Dict &d, uint, outputStream *st  ) const; // Specialized per-Type dumping
#endif
};

//------------------------------TypeAry----------------------------------------
// Class of Array Types
class TypeAry : public Type {
  TypeAry( const Type *elem, const TypeInt *size) : Type(Array),
    _elem(elem), _size(size) {}
public:
  virtual bool eq( const Type *t ) const;
  virtual int  hash() const;             // Type specific hashing
  virtual bool singleton(void) const;    // TRUE if type is a singleton
  virtual bool empty(void) const;        // TRUE if type is vacuous

private:
  const Type *_elem;            // Element type of array
  const TypeInt *_size;         // Elements in array
  friend class TypeAryPtr;

public:
  static const TypeAry *make(  const Type *elem, const TypeInt *size);

  virtual const Type *xmeet( const Type *t ) const;
  virtual const Type *xdual() const;    // Compute dual right now.
  bool ary_must_be_exact() const;  // true if arrays of such are never generic
#ifdef ASSERT
  // One type is interface, the other is oop
  virtual bool interface_vs_oop(const Type *t) const;
#endif
#ifndef PRODUCT
  virtual void dump2( Dict &d, uint, outputStream *st  ) const; // Specialized per-Type dumping
#endif
};

//------------------------------TypePtr----------------------------------------
// Class of machine Pointer Types: raw data, instances or arrays.
// If the _base enum is AnyPtr, then this refers to all of the above.
// Otherwise the _base will indicate which subset of pointers is affected,
// and the class will be inherited from.
class TypePtr : public Type {
  friend class TypeNarrowOop;
public:
  enum PTR { TopPTR, AnyNull, Constant, Null, NotNull, BotPTR, lastPTR };
protected:
  TypePtr( TYPES t, PTR ptr, int offset ) : Type(t), _ptr(ptr), _offset(offset) {}
  virtual bool eq( const Type *t ) const;
  virtual int  hash() const;             // Type specific hashing
  static const PTR ptr_meet[lastPTR][lastPTR];
  static const PTR ptr_dual[lastPTR];
  static const char * const ptr_msg[lastPTR];

public:
  const int _offset;            // Offset into oop, with TOP & BOT
  const PTR _ptr;               // Pointer equivalence class

  const int offset() const { return _offset; }
  const PTR ptr()    const { return _ptr; }

  static const TypePtr *make( TYPES t, PTR ptr, int offset );

  // Return a 'ptr' version of this type
  virtual const Type *cast_to_ptr_type(PTR ptr) const;

  virtual intptr_t get_con() const;

  int xadd_offset( intptr_t offset ) const;
  virtual const TypePtr *add_offset( intptr_t offset ) const;

  virtual bool singleton(void) const;    // TRUE if type is a singleton
  virtual bool empty(void) const;        // TRUE if type is vacuous
  virtual const Type *xmeet( const Type *t ) const;
  int meet_offset( int offset ) const;
  int dual_offset( ) const;
  virtual const Type *xdual() const;    // Compute dual right now.

  // meet, dual and join over pointer equivalence sets
  PTR meet_ptr( const PTR in_ptr ) const { return ptr_meet[in_ptr][ptr()]; }
  PTR dual_ptr()                   const { return ptr_dual[ptr()];      }

  // This is textually confusing unless one recalls that
  // join(t) == dual()->meet(t->dual())->dual().
  PTR join_ptr( const PTR in_ptr ) const {
    return ptr_dual[ ptr_meet[ ptr_dual[in_ptr] ] [ dual_ptr() ] ];
  }

  // Tests for relation to centerline of type lattice:
  static bool above_centerline(PTR ptr) { return (ptr <= AnyNull); }
  static bool below_centerline(PTR ptr) { return (ptr >= NotNull); }
  // Convenience common pre-built types.
  static const TypePtr *NULL_PTR;
  static const TypePtr *NOTNULL;
  static const TypePtr *BOTTOM;
#ifndef PRODUCT
  virtual void dump2( Dict &d, uint depth, outputStream *st  ) const;
#endif
};

//------------------------------TypeRawPtr-------------------------------------
// Class of raw pointers, pointers to things other than Oops.  Examples
// include the stack pointer, top of heap, card-marking area, handles, etc.
class TypeRawPtr : public TypePtr {
protected:
  TypeRawPtr( PTR ptr, address bits ) : TypePtr(RawPtr,ptr,0), _bits(bits){}
public:
  virtual bool eq( const Type *t ) const;
  virtual int  hash() const;     // Type specific hashing

  const address _bits;          // Constant value, if applicable

  static const TypeRawPtr *make( PTR ptr );
  static const TypeRawPtr *make( address bits );

  // Return a 'ptr' version of this type
  virtual const Type *cast_to_ptr_type(PTR ptr) const;

  virtual intptr_t get_con() const;

  virtual const TypePtr *add_offset( intptr_t offset ) const;

  virtual const Type *xmeet( const Type *t ) const;
  virtual const Type *xdual() const;    // Compute dual right now.
  // Convenience common pre-built types.
  static const TypeRawPtr *BOTTOM;
  static const TypeRawPtr *NOTNULL;
#ifndef PRODUCT
  virtual void dump2( Dict &d, uint depth, outputStream *st  ) const;
#endif
};

//------------------------------TypeOopPtr-------------------------------------
// Some kind of oop (Java pointer), either klass or instance or array.
class TypeOopPtr : public TypePtr {
protected:
  TypeOopPtr( TYPES t, PTR ptr, ciKlass* k, bool xk, ciObject* o, int offset, int instance_id );
public:
  virtual bool eq( const Type *t ) const;
  virtual int  hash() const;             // Type specific hashing
  virtual bool singleton(void) const;    // TRUE if type is a singleton
  enum {
   InstanceTop = -1,   // undefined instance
   InstanceBot = 0     // any possible instance
  };
protected:

  // Oop is NULL, unless this is a constant oop.
  ciObject*     _const_oop;   // Constant oop
  // If _klass is NULL, then so is _sig.  This is an unloaded klass.
  ciKlass*      _klass;       // Klass object
  // Does the type exclude subclasses of the klass?  (Inexact == polymorphic.)
  bool          _klass_is_exact;
  bool          _is_ptr_to_narrowoop;

  // If not InstanceTop or InstanceBot, indicates that this is
  // a particular instance of this type which is distinct.
  // This is the the node index of the allocation node creating this instance.
  int           _instance_id;

  static const TypeOopPtr* make_from_klass_common(ciKlass* klass, bool klass_change, bool try_for_exact);

  int dual_instance_id() const;
  int meet_instance_id(int uid) const;

public:
  // Creates a type given a klass. Correctly handles multi-dimensional arrays
  // Respects UseUniqueSubclasses.
  // If the klass is final, the resulting type will be exact.
  static const TypeOopPtr* make_from_klass(ciKlass* klass) {
    return make_from_klass_common(klass, true, false);
  }
  // Same as before, but will produce an exact type, even if
  // the klass is not final, as long as it has exactly one implementation.
  static const TypeOopPtr* make_from_klass_unique(ciKlass* klass) {
    return make_from_klass_common(klass, true, true);
  }
  // Same as before, but does not respects UseUniqueSubclasses.
  // Use this only for creating array element types.
  static const TypeOopPtr* make_from_klass_raw(ciKlass* klass) {
    return make_from_klass_common(klass, false, false);
  }
  // Creates a singleton type given an object.
  // If the object cannot be rendered as a constant,
  // may return a non-singleton type.
  // If require_constant, produce a NULL if a singleton is not possible.
  static const TypeOopPtr* make_from_constant(ciObject* o, bool require_constant = false);

  // Make a generic (unclassed) pointer to an oop.
  static const TypeOopPtr* make(PTR ptr, int offset, int instance_id);

  ciObject* const_oop()    const { return _const_oop; }
  virtual ciKlass* klass() const { return _klass;     }
  bool klass_is_exact()    const { return _klass_is_exact; }

  // Returns true if this pointer points at memory which contains a
  // compressed oop references.
  bool is_ptr_to_narrowoop_nv() const { return _is_ptr_to_narrowoop; }

  bool is_known_instance()       const { return _instance_id > 0; }
  int  instance_id()             const { return _instance_id; }
  bool is_known_instance_field() const { return is_known_instance() && _offset >= 0; }

  virtual intptr_t get_con() const;

  virtual const Type *cast_to_ptr_type(PTR ptr) const;

  virtual const Type *cast_to_exactness(bool klass_is_exact) const;

  virtual const TypeOopPtr *cast_to_instance_id(int instance_id) const;

  // corresponding pointer to klass, for a given instance
  const TypeKlassPtr* as_klass_type() const;

  virtual const TypePtr *add_offset( intptr_t offset ) const;

  virtual const Type *xmeet( const Type *t ) const;
  virtual const Type *xdual() const;    // Compute dual right now.

  // Do not allow interface-vs.-noninterface joins to collapse to top.
  virtual const Type *filter( const Type *kills ) const;

  // Convenience common pre-built type.
  static const TypeOopPtr *BOTTOM;
#ifndef PRODUCT
  virtual void dump2( Dict &d, uint depth, outputStream *st ) const;
#endif
};

//------------------------------TypeInstPtr------------------------------------
// Class of Java object pointers, pointing either to non-array Java instances
// or to a klassOop (including array klasses).
class TypeInstPtr : public TypeOopPtr {
  TypeInstPtr( PTR ptr, ciKlass* k, bool xk, ciObject* o, int offset, int instance_id );
  virtual bool eq( const Type *t ) const;
  virtual int  hash() const;             // Type specific hashing

  ciSymbol*  _name;        // class name

 public:
  ciSymbol* name()         const { return _name; }

  bool  is_loaded() const { return _klass->is_loaded(); }

  // Make a pointer to a constant oop.
  static const TypeInstPtr *make(ciObject* o) {
    return make(TypePtr::Constant, o->klass(), true, o, 0);
  }

  // Make a pointer to a constant oop with offset.
  static const TypeInstPtr *make(ciObject* o, int offset) {
    return make(TypePtr::Constant, o->klass(), true, o, offset);
  }

  // Make a pointer to some value of type klass.
  static const TypeInstPtr *make(PTR ptr, ciKlass* klass) {
    return make(ptr, klass, false, NULL, 0);
  }

  // Make a pointer to some non-polymorphic value of exactly type klass.
  static const TypeInstPtr *make_exact(PTR ptr, ciKlass* klass) {
    return make(ptr, klass, true, NULL, 0);
  }

  // Make a pointer to some value of type klass with offset.
  static const TypeInstPtr *make(PTR ptr, ciKlass* klass, int offset) {
    return make(ptr, klass, false, NULL, offset);
  }

  // Make a pointer to an oop.
  static const TypeInstPtr *make(PTR ptr, ciKlass* k, bool xk, ciObject* o, int offset, int instance_id = InstanceBot );

  // If this is a java.lang.Class constant, return the type for it or NULL.
  // Pass to Type::get_const_type to turn it to a type, which will usually
  // be a TypeInstPtr, but may also be a TypeInt::INT for int.class, etc.
  ciType* java_mirror_type() const;

  virtual const Type *cast_to_ptr_type(PTR ptr) const;

  virtual const Type *cast_to_exactness(bool klass_is_exact) const;

  virtual const TypeOopPtr *cast_to_instance_id(int instance_id) const;

  virtual const TypePtr *add_offset( intptr_t offset ) const;

  virtual const Type *xmeet( const Type *t ) const;
  virtual const TypeInstPtr *xmeet_unloaded( const TypeInstPtr *t ) const;
  virtual const Type *xdual() const;    // Compute dual right now.

  // Convenience common pre-built types.
  static const TypeInstPtr *NOTNULL;
  static const TypeInstPtr *BOTTOM;
  static const TypeInstPtr *MIRROR;
  static const TypeInstPtr *MARK;
  static const TypeInstPtr *KLASS;
#ifndef PRODUCT
  virtual void dump2( Dict &d, uint depth, outputStream *st ) const; // Specialized per-Type dumping
#endif
};

//------------------------------TypeAryPtr-------------------------------------
// Class of Java array pointers
class TypeAryPtr : public TypeOopPtr {
  TypeAryPtr( PTR ptr, ciObject* o, const TypeAry *ary, ciKlass* k, bool xk, int offset, int instance_id ) : TypeOopPtr(AryPtr,ptr,k,xk,o,offset, instance_id), _ary(ary) {};
  virtual bool eq( const Type *t ) const;
  virtual int hash() const;     // Type specific hashing
  const TypeAry *_ary;          // Array we point into

public:
  // Accessors
  ciKlass* klass() const;
  const TypeAry* ary() const  { return _ary; }
  const Type*    elem() const { return _ary->_elem; }
  const TypeInt* size() const { return _ary->_size; }

  static const TypeAryPtr *make( PTR ptr, const TypeAry *ary, ciKlass* k, bool xk, int offset, int instance_id = InstanceBot);
  // Constant pointer to array
  static const TypeAryPtr *make( PTR ptr, ciObject* o, const TypeAry *ary, ciKlass* k, bool xk, int offset, int instance_id = InstanceBot);

  // Return a 'ptr' version of this type
  virtual const Type *cast_to_ptr_type(PTR ptr) const;

  virtual const Type *cast_to_exactness(bool klass_is_exact) const;

  virtual const TypeOopPtr *cast_to_instance_id(int instance_id) const;

  virtual const TypeAryPtr* cast_to_size(const TypeInt* size) const;
  virtual const TypeInt* narrow_size_type(const TypeInt* size) const;

  virtual bool empty(void) const;        // TRUE if type is vacuous
  virtual const TypePtr *add_offset( intptr_t offset ) const;

  virtual const Type *xmeet( const Type *t ) const;
  virtual const Type *xdual() const;    // Compute dual right now.

  // Convenience common pre-built types.
  static const TypeAryPtr *RANGE;
  static const TypeAryPtr *OOPS;
  static const TypeAryPtr *NARROWOOPS;
  static const TypeAryPtr *BYTES;
  static const TypeAryPtr *SHORTS;
  static const TypeAryPtr *CHARS;
  static const TypeAryPtr *INTS;
  static const TypeAryPtr *LONGS;
  static const TypeAryPtr *FLOATS;
  static const TypeAryPtr *DOUBLES;
  // selects one of the above:
  static const TypeAryPtr *get_array_body_type(BasicType elem) {
    assert((uint)elem <= T_CONFLICT && _array_body_type[elem] != NULL, "bad elem type");
    return _array_body_type[elem];
  }
  static const TypeAryPtr *_array_body_type[T_CONFLICT+1];
  // sharpen the type of an int which is used as an array size
#ifdef ASSERT
  // One type is interface, the other is oop
  virtual bool interface_vs_oop(const Type *t) const;
#endif
#ifndef PRODUCT
  virtual void dump2( Dict &d, uint depth, outputStream *st ) const; // Specialized per-Type dumping
#endif
};

//------------------------------TypeKlassPtr-----------------------------------
// Class of Java Klass pointers
class TypeKlassPtr : public TypeOopPtr {
  TypeKlassPtr( PTR ptr, ciKlass* klass, int offset );

  virtual bool eq( const Type *t ) const;
  virtual int hash() const;             // Type specific hashing

public:
  ciSymbol* name()  const { return _klass->name(); }

  bool  is_loaded() const { return _klass->is_loaded(); }

  // ptr to klass 'k'
  static const TypeKlassPtr *make( ciKlass* k ) { return make( TypePtr::Constant, k, 0); }
  // ptr to klass 'k' with offset
  static const TypeKlassPtr *make( ciKlass* k, int offset ) { return make( TypePtr::Constant, k, offset); }
  // ptr to klass 'k' or sub-klass
  static const TypeKlassPtr *make( PTR ptr, ciKlass* k, int offset);

  virtual const Type *cast_to_ptr_type(PTR ptr) const;

  virtual const Type *cast_to_exactness(bool klass_is_exact) const;

  // corresponding pointer to instance, for a given class
  const TypeOopPtr* as_instance_type() const;

  virtual const TypePtr *add_offset( intptr_t offset ) const;
  virtual const Type    *xmeet( const Type *t ) const;
  virtual const Type    *xdual() const;      // Compute dual right now.

  // Convenience common pre-built types.
  static const TypeKlassPtr* OBJECT; // Not-null object klass or below
  static const TypeKlassPtr* OBJECT_OR_NULL; // Maybe-null version of same
#ifndef PRODUCT
  virtual void dump2( Dict &d, uint depth, outputStream *st ) const; // Specialized per-Type dumping
#endif
};

//------------------------------TypeNarrowOop----------------------------------
// A compressed reference to some kind of Oop.  This type wraps around
// a preexisting TypeOopPtr and forwards most of it's operations to
// the underlying type.  It's only real purpose is to track the
// oopness of the compressed oop value when we expose the conversion
// between the normal and the compressed form.
class TypeNarrowOop : public Type {
protected:
  const TypePtr* _ptrtype; // Could be TypePtr::NULL_PTR

  TypeNarrowOop( const TypePtr* ptrtype): Type(NarrowOop),
    _ptrtype(ptrtype) {
    assert(ptrtype->offset() == 0 ||
           ptrtype->offset() == OffsetBot ||
           ptrtype->offset() == OffsetTop, "no real offsets");
  }
public:
  virtual bool eq( const Type *t ) const;
  virtual int  hash() const;             // Type specific hashing
  virtual bool singleton(void) const;    // TRUE if type is a singleton

  virtual const Type *xmeet( const Type *t ) const;
  virtual const Type *xdual() const;    // Compute dual right now.

  virtual intptr_t get_con() const;

  // Do not allow interface-vs.-noninterface joins to collapse to top.
  virtual const Type *filter( const Type *kills ) const;

  virtual bool empty(void) const;        // TRUE if type is vacuous

  static const TypeNarrowOop *make( const TypePtr* type);

  static const TypeNarrowOop* make_from_constant(ciObject* con) {
    return make(TypeOopPtr::make_from_constant(con));
  }

  // returns the equivalent ptr type for this compressed pointer
  const TypePtr *get_ptrtype() const {
    return _ptrtype;
  }

  static const TypeNarrowOop *BOTTOM;
  static const TypeNarrowOop *NULL_PTR;

#ifndef PRODUCT
  virtual void dump2( Dict &d, uint depth, outputStream *st ) const;
#endif
};

//------------------------------TypeFunc---------------------------------------
// Class of Array Types
class TypeFunc : public Type {
  TypeFunc( const TypeTuple *domain, const TypeTuple *range ) : Type(Function),  _domain(domain), _range(range) {}
  virtual bool eq( const Type *t ) const;
  virtual int  hash() const;             // Type specific hashing
  virtual bool singleton(void) const;    // TRUE if type is a singleton
  virtual bool empty(void) const;        // TRUE if type is vacuous
public:
  // Constants are shared among ADLC and VM
  enum { Control    = AdlcVMDeps::Control,
         I_O        = AdlcVMDeps::I_O,
         Memory     = AdlcVMDeps::Memory,
         FramePtr   = AdlcVMDeps::FramePtr,
         ReturnAdr  = AdlcVMDeps::ReturnAdr,
         Parms      = AdlcVMDeps::Parms
  };

  const TypeTuple* const _domain;     // Domain of inputs
  const TypeTuple* const _range;      // Range of results

  // Accessors:
  const TypeTuple* domain() const { return _domain; }
  const TypeTuple* range()  const { return _range; }

  static const TypeFunc *make(ciMethod* method);
  static const TypeFunc *make(ciSignature signature, const Type* extra);
  static const TypeFunc *make(const TypeTuple* domain, const TypeTuple* range);

  virtual const Type *xmeet( const Type *t ) const;
  virtual const Type *xdual() const;    // Compute dual right now.

  BasicType return_type() const;

#ifndef PRODUCT
  virtual void dump2( Dict &d, uint depth, outputStream *st ) const; // Specialized per-Type dumping
  void print_flattened() const; // Print a 'flattened' signature
#endif
  // Convenience common pre-built types.
};

//------------------------------accessors--------------------------------------
inline bool Type::is_ptr_to_narrowoop() const {
#ifdef _LP64
  return (isa_oopptr() != NULL && is_oopptr()->is_ptr_to_narrowoop_nv());
#else
  return false;
#endif
}

inline float Type::getf() const {
  assert( _base == FloatCon, "Not a FloatCon" );
  return ((TypeF*)this)->_f;
}

inline double Type::getd() const {
  assert( _base == DoubleCon, "Not a DoubleCon" );
  return ((TypeD*)this)->_d;
}

inline const TypeF *Type::is_float_constant() const {
  assert( _base == FloatCon, "Not a Float" );
  return (TypeF*)this;
}

inline const TypeF *Type::isa_float_constant() const {
  return ( _base == FloatCon ? (TypeF*)this : NULL);
}

inline const TypeD *Type::is_double_constant() const {
  assert( _base == DoubleCon, "Not a Double" );
  return (TypeD*)this;
}

inline const TypeD *Type::isa_double_constant() const {
  return ( _base == DoubleCon ? (TypeD*)this : NULL);
}

inline const TypeInt *Type::is_int() const {
  assert( _base == Int, "Not an Int" );
  return (TypeInt*)this;
}

inline const TypeInt *Type::isa_int() const {
  return ( _base == Int ? (TypeInt*)this : NULL);
}

inline const TypeLong *Type::is_long() const {
  assert( _base == Long, "Not a Long" );
  return (TypeLong*)this;
}

inline const TypeLong *Type::isa_long() const {
  return ( _base == Long ? (TypeLong*)this : NULL);
}

inline const TypeTuple *Type::is_tuple() const {
  assert( _base == Tuple, "Not a Tuple" );
  return (TypeTuple*)this;
}

inline const TypeAry *Type::is_ary() const {
  assert( _base == Array , "Not an Array" );
  return (TypeAry*)this;
}

inline const TypePtr *Type::is_ptr() const {
  // AnyPtr is the first Ptr and KlassPtr the last, with no non-ptrs between.
  assert(_base >= AnyPtr && _base <= KlassPtr, "Not a pointer");
  return (TypePtr*)this;
}

inline const TypePtr *Type::isa_ptr() const {
  // AnyPtr is the first Ptr and KlassPtr the last, with no non-ptrs between.
  return (_base >= AnyPtr && _base <= KlassPtr) ? (TypePtr*)this : NULL;
}

inline const TypeOopPtr *Type::is_oopptr() const {
  // OopPtr is the first and KlassPtr the last, with no non-oops between.
  assert(_base >= OopPtr && _base <= KlassPtr, "Not a Java pointer" ) ;
  return (TypeOopPtr*)this;
}

inline const TypeOopPtr *Type::isa_oopptr() const {
  // OopPtr is the first and KlassPtr the last, with no non-oops between.
  return (_base >= OopPtr && _base <= KlassPtr) ? (TypeOopPtr*)this : NULL;
}

inline const TypeRawPtr *Type::isa_rawptr() const {
  return (_base == RawPtr) ? (TypeRawPtr*)this : NULL;
}

inline const TypeRawPtr *Type::is_rawptr() const {
  assert( _base == RawPtr, "Not a raw pointer" );
  return (TypeRawPtr*)this;
}

inline const TypeInstPtr *Type::isa_instptr() const {
  return (_base == InstPtr) ? (TypeInstPtr*)this : NULL;
}

inline const TypeInstPtr *Type::is_instptr() const {
  assert( _base == InstPtr, "Not an object pointer" );
  return (TypeInstPtr*)this;
}

inline const TypeAryPtr *Type::isa_aryptr() const {
  return (_base == AryPtr) ? (TypeAryPtr*)this : NULL;
}

inline const TypeAryPtr *Type::is_aryptr() const {
  assert( _base == AryPtr, "Not an array pointer" );
  return (TypeAryPtr*)this;
}

inline const TypeNarrowOop *Type::is_narrowoop() const {
  // OopPtr is the first and KlassPtr the last, with no non-oops between.
  assert(_base == NarrowOop, "Not a narrow oop" ) ;
  return (TypeNarrowOop*)this;
}

inline const TypeNarrowOop *Type::isa_narrowoop() const {
  // OopPtr is the first and KlassPtr the last, with no non-oops between.
  return (_base == NarrowOop) ? (TypeNarrowOop*)this : NULL;
}

inline const TypeKlassPtr *Type::isa_klassptr() const {
  return (_base == KlassPtr) ? (TypeKlassPtr*)this : NULL;
}

inline const TypeKlassPtr *Type::is_klassptr() const {
  assert( _base == KlassPtr, "Not a klass pointer" );
  return (TypeKlassPtr*)this;
}

inline const TypePtr* Type::make_ptr() const {
  return (_base == NarrowOop) ? is_narrowoop()->get_ptrtype() :
                                (isa_ptr() ? is_ptr() : NULL);
}

inline const TypeOopPtr* Type::make_oopptr() const {
  return (_base == NarrowOop) ? is_narrowoop()->get_ptrtype()->is_oopptr() : is_oopptr();
}

inline const TypeNarrowOop* Type::make_narrowoop() const {
  return (_base == NarrowOop) ? is_narrowoop() :
                                (isa_ptr() ? TypeNarrowOop::make(is_ptr()) : NULL);
}

inline bool Type::is_floatingpoint() const {
  if( (_base == FloatCon)  || (_base == FloatBot) ||
      (_base == DoubleCon) || (_base == DoubleBot) )
    return true;
  return false;
}


// ===============================================================
// Things that need to be 64-bits in the 64-bit build but
// 32-bits in the 32-bit build.  Done this way to get full
// optimization AND strong typing.
#ifdef _LP64

// For type queries and asserts
#define is_intptr_t  is_long
#define isa_intptr_t isa_long
#define find_intptr_t_type find_long_type
#define find_intptr_t_con  find_long_con
#define TypeX        TypeLong
#define Type_X       Type::Long
#define TypeX_X      TypeLong::LONG
#define TypeX_ZERO   TypeLong::ZERO
// For 'ideal_reg' machine registers
#define Op_RegX      Op_RegL
// For phase->intcon variants
#define MakeConX     longcon
#define ConXNode     ConLNode
// For array index arithmetic
#define MulXNode     MulLNode
#define AndXNode     AndLNode
#define OrXNode      OrLNode
#define CmpXNode     CmpLNode
#define SubXNode     SubLNode
#define LShiftXNode  LShiftLNode
// For object size computation:
#define AddXNode     AddLNode
#define RShiftXNode  RShiftLNode
// For card marks and hashcodes
#define URShiftXNode URShiftLNode
// UseOptoBiasInlining
#define XorXNode     XorLNode
#define StoreXConditionalNode StoreLConditionalNode
// Opcodes
#define Op_LShiftX   Op_LShiftL
#define Op_AndX      Op_AndL
#define Op_AddX      Op_AddL
#define Op_SubX      Op_SubL
#define Op_XorX      Op_XorL
#define Op_URShiftX  Op_URShiftL
// conversions
#define ConvI2X(x)   ConvI2L(x)
#define ConvL2X(x)   (x)
#define ConvX2I(x)   ConvL2I(x)
#define ConvX2L(x)   (x)

#else

// For type queries and asserts
#define is_intptr_t  is_int
#define isa_intptr_t isa_int
#define find_intptr_t_type find_int_type
#define find_intptr_t_con  find_int_con
#define TypeX        TypeInt
#define Type_X       Type::Int
#define TypeX_X      TypeInt::INT
#define TypeX_ZERO   TypeInt::ZERO
// For 'ideal_reg' machine registers
#define Op_RegX      Op_RegI
// For phase->intcon variants
#define MakeConX     intcon
#define ConXNode     ConINode
// For array index arithmetic
#define MulXNode     MulINode
#define AndXNode     AndINode
#define OrXNode      OrINode
#define CmpXNode     CmpINode
#define SubXNode     SubINode
#define LShiftXNode  LShiftINode
// For object size computation:
#define AddXNode     AddINode
#define RShiftXNode  RShiftINode
// For card marks and hashcodes
#define URShiftXNode URShiftINode
// UseOptoBiasInlining
#define XorXNode     XorINode
#define StoreXConditionalNode StoreIConditionalNode
// Opcodes
#define Op_LShiftX   Op_LShiftI
#define Op_AndX      Op_AndI
#define Op_AddX      Op_AddI
#define Op_SubX      Op_SubI
#define Op_XorX      Op_XorI
#define Op_URShiftX  Op_URShiftI
// conversions
#define ConvI2X(x)   (x)
#define ConvL2X(x)   ConvL2I(x)
#define ConvX2I(x)   (x)
#define ConvX2L(x)   ConvI2L(x)

#endif
