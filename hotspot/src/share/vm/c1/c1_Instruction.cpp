/*
 * Copyright 1999-2006 Sun Microsystems, Inc.  All Rights Reserved.
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

#include "incls/_precompiled.incl"
#include "incls/_c1_Instruction.cpp.incl"


// Implementation of Instruction


int Instruction::_next_id = 0;

#ifdef ASSERT
void Instruction::create_hi_word() {
  assert(type()->is_double_word() && _hi_word == NULL, "only double word has high word");
  _hi_word = new HiWord(this);
}
#endif

Instruction::Condition Instruction::mirror(Condition cond) {
  switch (cond) {
    case eql: return eql;
    case neq: return neq;
    case lss: return gtr;
    case leq: return geq;
    case gtr: return lss;
    case geq: return leq;
  }
  ShouldNotReachHere();
  return eql;
}


Instruction::Condition Instruction::negate(Condition cond) {
  switch (cond) {
    case eql: return neq;
    case neq: return eql;
    case lss: return geq;
    case leq: return gtr;
    case gtr: return leq;
    case geq: return lss;
  }
  ShouldNotReachHere();
  return eql;
}


Instruction* Instruction::prev(BlockBegin* block) {
  Instruction* p = NULL;
  Instruction* q = block;
  while (q != this) {
    assert(q != NULL, "this is not in the block's instruction list");
    p = q; q = q->next();
  }
  return p;
}


#ifndef PRODUCT
void Instruction::print() {
  InstructionPrinter ip;
  print(ip);
}


void Instruction::print_line() {
  InstructionPrinter ip;
  ip.print_line(this);
}


void Instruction::print(InstructionPrinter& ip) {
  ip.print_head();
  ip.print_line(this);
  tty->cr();
}
#endif // PRODUCT


// perform constant and interval tests on index value
bool AccessIndexed::compute_needs_range_check() {
  Constant* clength = length()->as_Constant();
  Constant* cindex = index()->as_Constant();
  if (clength && cindex) {
    IntConstant* l = clength->type()->as_IntConstant();
    IntConstant* i = cindex->type()->as_IntConstant();
    if (l && i && i->value() < l->value() && i->value() >= 0) {
      return false;
    }
  }
  return true;
}


ciType* LoadIndexed::exact_type() const {
  ciType* array_type = array()->exact_type();
  if (array_type == NULL) {
    return NULL;
  }
  assert(array_type->is_array_klass(), "what else?");
  ciArrayKlass* ak = (ciArrayKlass*)array_type;

  if (ak->element_type()->is_instance_klass()) {
    ciInstanceKlass* ik = (ciInstanceKlass*)ak->element_type();
    if (ik->is_loaded() && ik->is_final()) {
      return ik;
    }
  }
  return NULL;
}


ciType* LoadIndexed::declared_type() const {
  ciType* array_type = array()->declared_type();
  if (array_type == NULL) {
    return NULL;
  }
  assert(array_type->is_array_klass(), "what else?");
  ciArrayKlass* ak = (ciArrayKlass*)array_type;
  return ak->element_type();
}


ciType* LoadField::declared_type() const {
  return field()->type();
}


ciType* LoadField::exact_type() const {
  ciType* type = declared_type();
  // for primitive arrays, the declared type is the exact type
  if (type->is_type_array_klass()) {
    return type;
  }
  if (type->is_instance_klass()) {
    ciInstanceKlass* ik = (ciInstanceKlass*)type;
    if (ik->is_loaded() && ik->is_final()) {
      return type;
    }
  }
  return NULL;
}


ciType* NewTypeArray::exact_type() const {
  return ciTypeArrayKlass::make(elt_type());
}


ciType* NewObjectArray::exact_type() const {
  return ciObjArrayKlass::make(klass());
}


ciType* NewInstance::exact_type() const {
  return klass();
}


ciType* CheckCast::declared_type() const {
  return klass();
}

ciType* CheckCast::exact_type() const {
  if (klass()->is_instance_klass()) {
    ciInstanceKlass* ik = (ciInstanceKlass*)klass();
    if (ik->is_loaded() && ik->is_final()) {
      return ik;
    }
  }
  return NULL;
}


void ArithmeticOp::other_values_do(void f(Value*)) {
  if (lock_stack() != NULL) lock_stack()->values_do(f);
}

void NullCheck::other_values_do(void f(Value*)) {
  lock_stack()->values_do(f);
}

void AccessArray::other_values_do(void f(Value*)) {
  if (lock_stack() != NULL) lock_stack()->values_do(f);
}


// Implementation of AccessField

void AccessField::other_values_do(void f(Value*)) {
  if (state_before() != NULL) state_before()->values_do(f);
  if (lock_stack() != NULL) lock_stack()->values_do(f);
}


// Implementation of StoreIndexed

IRScope* StoreIndexed::scope() const {
  return lock_stack()->scope();
}


// Implementation of ArithmeticOp

bool ArithmeticOp::is_commutative() const {
  switch (op()) {
    case Bytecodes::_iadd: // fall through
    case Bytecodes::_ladd: // fall through
    case Bytecodes::_fadd: // fall through
    case Bytecodes::_dadd: // fall through
    case Bytecodes::_imul: // fall through
    case Bytecodes::_lmul: // fall through
    case Bytecodes::_fmul: // fall through
    case Bytecodes::_dmul: return true;
  }
  return false;
}


bool ArithmeticOp::can_trap() const {
  switch (op()) {
    case Bytecodes::_idiv: // fall through
    case Bytecodes::_ldiv: // fall through
    case Bytecodes::_irem: // fall through
    case Bytecodes::_lrem: return true;
  }
  return false;
}


// Implementation of LogicOp

bool LogicOp::is_commutative() const {
#ifdef ASSERT
  switch (op()) {
    case Bytecodes::_iand: // fall through
    case Bytecodes::_land: // fall through
    case Bytecodes::_ior : // fall through
    case Bytecodes::_lor : // fall through
    case Bytecodes::_ixor: // fall through
    case Bytecodes::_lxor: break;
    default              : ShouldNotReachHere();
  }
#endif
  // all LogicOps are commutative
  return true;
}


// Implementation of CompareOp

void CompareOp::other_values_do(void f(Value*)) {
  if (state_before() != NULL) state_before()->values_do(f);
}


// Implementation of IfOp

bool IfOp::is_commutative() const {
  return cond() == eql || cond() == neq;
}


// Implementation of StateSplit

void StateSplit::substitute(BlockList& list, BlockBegin* old_block, BlockBegin* new_block) {
  NOT_PRODUCT(bool assigned = false;)
  for (int i = 0; i < list.length(); i++) {
    BlockBegin** b = list.adr_at(i);
    if (*b == old_block) {
      *b = new_block;
      NOT_PRODUCT(assigned = true;)
    }
  }
  assert(assigned == true, "should have assigned at least once");
}


IRScope* StateSplit::scope() const {
  return _state->scope();
}


void StateSplit::state_values_do(void f(Value*)) {
  if (state() != NULL) state()->values_do(f);
}


void BlockBegin::state_values_do(void f(Value*)) {
  StateSplit::state_values_do(f);

  if (is_set(BlockBegin::exception_entry_flag)) {
    for (int i = 0; i < number_of_exception_states(); i++) {
      exception_state_at(i)->values_do(f);
    }
  }
}


void MonitorEnter::state_values_do(void f(Value*)) {
  StateSplit::state_values_do(f);
  _lock_stack_before->values_do(f);
}


void Intrinsic::state_values_do(void f(Value*)) {
  StateSplit::state_values_do(f);
  if (lock_stack() != NULL) lock_stack()->values_do(f);
}


// Implementation of Invoke


Invoke::Invoke(Bytecodes::Code code, ValueType* result_type, Value recv, Values* args,
               int vtable_index, ciMethod* target)
  : StateSplit(result_type)
  , _code(code)
  , _recv(recv)
  , _args(args)
  , _vtable_index(vtable_index)
  , _target(target)
{
  set_flag(TargetIsLoadedFlag,   target->is_loaded());
  set_flag(TargetIsFinalFlag,    target_is_loaded() && target->is_final_method());
  set_flag(TargetIsStrictfpFlag, target_is_loaded() && target->is_strict());

  assert(args != NULL, "args must exist");
#ifdef ASSERT
  values_do(assert_value);
#endif // ASSERT

  // provide an initial guess of signature size.
  _signature = new BasicTypeList(number_of_arguments() + (has_receiver() ? 1 : 0));
  if (has_receiver()) {
    _signature->append(as_BasicType(receiver()->type()));
  }
  for (int i = 0; i < number_of_arguments(); i++) {
    ValueType* t = argument_at(i)->type();
    BasicType bt = as_BasicType(t);
    _signature->append(bt);
  }
}


// Implementation of Contant
intx Constant::hash() const {
  if (_state == NULL) {
    switch (type()->tag()) {
    case intTag:
      return HASH2(name(), type()->as_IntConstant()->value());
    case longTag:
      {
        jlong temp = type()->as_LongConstant()->value();
        return HASH3(name(), high(temp), low(temp));
      }
    case floatTag:
      return HASH2(name(), jint_cast(type()->as_FloatConstant()->value()));
    case doubleTag:
      {
        jlong temp = jlong_cast(type()->as_DoubleConstant()->value());
        return HASH3(name(), high(temp), low(temp));
      }
    case objectTag:
      assert(type()->as_ObjectType()->is_loaded(), "can't handle unloaded values");
      return HASH2(name(), type()->as_ObjectType()->constant_value());
    }
  }
  return 0;
}

bool Constant::is_equal(Value v) const {
  if (v->as_Constant() == NULL) return false;

  switch (type()->tag()) {
    case intTag:
      {
        IntConstant* t1 =    type()->as_IntConstant();
        IntConstant* t2 = v->type()->as_IntConstant();
        return (t1 != NULL && t2 != NULL &&
                t1->value() == t2->value());
      }
    case longTag:
      {
        LongConstant* t1 =    type()->as_LongConstant();
        LongConstant* t2 = v->type()->as_LongConstant();
        return (t1 != NULL && t2 != NULL &&
                t1->value() == t2->value());
      }
    case floatTag:
      {
        FloatConstant* t1 =    type()->as_FloatConstant();
        FloatConstant* t2 = v->type()->as_FloatConstant();
        return (t1 != NULL && t2 != NULL &&
                jint_cast(t1->value()) == jint_cast(t2->value()));
      }
    case doubleTag:
      {
        DoubleConstant* t1 =    type()->as_DoubleConstant();
        DoubleConstant* t2 = v->type()->as_DoubleConstant();
        return (t1 != NULL && t2 != NULL &&
                jlong_cast(t1->value()) == jlong_cast(t2->value()));
      }
    case objectTag:
      {
        ObjectType* t1 =    type()->as_ObjectType();
        ObjectType* t2 = v->type()->as_ObjectType();
        return (t1 != NULL && t2 != NULL &&
                t1->is_loaded() && t2->is_loaded() &&
                t1->constant_value() == t2->constant_value());
      }
  }
  return false;
}


BlockBegin* Constant::compare(Instruction::Condition cond, Value right,
                              BlockBegin* true_sux, BlockBegin* false_sux) {
  Constant* rc = right->as_Constant();
  // other is not a constant
  if (rc == NULL) return NULL;

  ValueType* lt = type();
  ValueType* rt = rc->type();
  // different types
  if (lt->base() != rt->base()) return NULL;
  switch (lt->tag()) {
  case intTag: {
    int x = lt->as_IntConstant()->value();
    int y = rt->as_IntConstant()->value();
    switch (cond) {
    case If::eql: return x == y ? true_sux : false_sux;
    case If::neq: return x != y ? true_sux : false_sux;
    case If::lss: return x <  y ? true_sux : false_sux;
    case If::leq: return x <= y ? true_sux : false_sux;
    case If::gtr: return x >  y ? true_sux : false_sux;
    case If::geq: return x >= y ? true_sux : false_sux;
    }
    break;
  }
  case longTag: {
    jlong x = lt->as_LongConstant()->value();
    jlong y = rt->as_LongConstant()->value();
    switch (cond) {
    case If::eql: return x == y ? true_sux : false_sux;
    case If::neq: return x != y ? true_sux : false_sux;
    case If::lss: return x <  y ? true_sux : false_sux;
    case If::leq: return x <= y ? true_sux : false_sux;
    case If::gtr: return x >  y ? true_sux : false_sux;
    case If::geq: return x >= y ? true_sux : false_sux;
    }
    break;
  }
  case objectTag: {
    ciObject* xvalue = lt->as_ObjectType()->constant_value();
    ciObject* yvalue = rt->as_ObjectType()->constant_value();
    assert(xvalue != NULL && yvalue != NULL, "not constants");
    if (xvalue->is_loaded() && yvalue->is_loaded()) {
      switch (cond) {
      case If::eql: return xvalue == yvalue ? true_sux : false_sux;
      case If::neq: return xvalue != yvalue ? true_sux : false_sux;
      }
    }
    break;
  }
  }
  return NULL;
}


void Constant::other_values_do(void f(Value*)) {
  if (state() != NULL) state()->values_do(f);
}


// Implementation of NewArray

void NewArray::other_values_do(void f(Value*)) {
  if (state_before() != NULL) state_before()->values_do(f);
}


// Implementation of TypeCheck

void TypeCheck::other_values_do(void f(Value*)) {
  if (state_before() != NULL) state_before()->values_do(f);
}


// Implementation of BlockBegin

int BlockBegin::_next_block_id = 0;


void BlockBegin::set_end(BlockEnd* end) {
  assert(end != NULL, "should not reset block end to NULL");
  BlockEnd* old_end = _end;
  if (end == old_end) {
    return;
  }
  // Must make the predecessors/successors match up with the
  // BlockEnd's notion.
  int i, n;
  if (old_end != NULL) {
    // disconnect from the old end
    old_end->set_begin(NULL);

    // disconnect this block from it's current successors
    for (i = 0; i < _successors.length(); i++) {
      _successors.at(i)->remove_predecessor(this);
    }
  }
  _end = end;

  _successors.clear();
  // Now reset successors list based on BlockEnd
  n = end->number_of_sux();
  for (i = 0; i < n; i++) {
    BlockBegin* sux = end->sux_at(i);
    _successors.append(sux);
    sux->_predecessors.append(this);
  }
  _end->set_begin(this);
}


void BlockBegin::disconnect_edge(BlockBegin* from, BlockBegin* to) {
  // disconnect any edges between from and to
#ifndef PRODUCT
  if (PrintIR && Verbose) {
    tty->print_cr("Disconnected edge B%d -> B%d", from->block_id(), to->block_id());
  }
#endif
  for (int s = 0; s < from->number_of_sux();) {
    BlockBegin* sux = from->sux_at(s);
    if (sux == to) {
      int index = sux->_predecessors.index_of(from);
      if (index >= 0) {
        sux->_predecessors.remove_at(index);
      }
      from->_successors.remove_at(s);
    } else {
      s++;
    }
  }
}


void BlockBegin::disconnect_from_graph() {
  // disconnect this block from all other blocks
  for (int p = 0; p < number_of_preds(); p++) {
    pred_at(p)->remove_successor(this);
  }
  for (int s = 0; s < number_of_sux(); s++) {
    sux_at(s)->remove_predecessor(this);
  }
}

void BlockBegin::substitute_sux(BlockBegin* old_sux, BlockBegin* new_sux) {
  // modify predecessors before substituting successors
  for (int i = 0; i < number_of_sux(); i++) {
    if (sux_at(i) == old_sux) {
      // remove old predecessor before adding new predecessor
      // otherwise there is a dead predecessor in the list
      new_sux->remove_predecessor(old_sux);
      new_sux->add_predecessor(this);
    }
  }
  old_sux->remove_predecessor(this);
  end()->substitute_sux(old_sux, new_sux);
}



// In general it is not possible to calculate a value for the field "depth_first_number"
// of the inserted block, without recomputing the values of the other blocks
// in the CFG. Therefore the value of "depth_first_number" in BlockBegin becomes meaningless.
BlockBegin* BlockBegin::insert_block_between(BlockBegin* sux) {
  // Try to make the bci close to a block with a single pred or sux,
  // since this make the block layout algorithm work better.
  int bci = -1;
  if (sux->number_of_preds() == 1) {
    bci = sux->bci();
  } else {
    bci = end()->bci();
  }

  BlockBegin* new_sux = new BlockBegin(bci);

  // mark this block (special treatment when block order is computed)
  new_sux->set(critical_edge_split_flag);

  // This goto is not a safepoint.
  Goto* e = new Goto(sux, false);
  new_sux->set_next(e, bci);
  new_sux->set_end(e);
  // setup states
  ValueStack* s = end()->state();
  new_sux->set_state(s->copy());
  e->set_state(s->copy());
  assert(new_sux->state()->locals_size() == s->locals_size(), "local size mismatch!");
  assert(new_sux->state()->stack_size() == s->stack_size(), "stack size mismatch!");
  assert(new_sux->state()->locks_size() == s->locks_size(), "locks size mismatch!");

  // link predecessor to new block
  end()->substitute_sux(sux, new_sux);

  // The ordering needs to be the same, so remove the link that the
  // set_end call above added and substitute the new_sux for this
  // block.
  sux->remove_predecessor(new_sux);

  // the successor could be the target of a switch so it might have
  // multiple copies of this predecessor, so substitute the new_sux
  // for the first and delete the rest.
  bool assigned = false;
  BlockList& list = sux->_predecessors;
  for (int i = 0; i < list.length(); i++) {
    BlockBegin** b = list.adr_at(i);
    if (*b == this) {
      if (assigned) {
        list.remove_at(i);
        // reprocess this index
        i--;
      } else {
        assigned = true;
        *b = new_sux;
      }
      // link the new block back to it's predecessors.
      new_sux->add_predecessor(this);
    }
  }
  assert(assigned == true, "should have assigned at least once");
  return new_sux;
}


void BlockBegin::remove_successor(BlockBegin* pred) {
  int idx;
  while ((idx = _successors.index_of(pred)) >= 0) {
    _successors.remove_at(idx);
  }
}


void BlockBegin::add_predecessor(BlockBegin* pred) {
  _predecessors.append(pred);
}


void BlockBegin::remove_predecessor(BlockBegin* pred) {
  int idx;
  while ((idx = _predecessors.index_of(pred)) >= 0) {
    _predecessors.remove_at(idx);
  }
}


void BlockBegin::add_exception_handler(BlockBegin* b) {
  assert(b != NULL && (b->is_set(exception_entry_flag)), "exception handler must exist");
  // add only if not in the list already
  if (!_exception_handlers.contains(b)) _exception_handlers.append(b);
}

int BlockBegin::add_exception_state(ValueStack* state) {
  assert(is_set(exception_entry_flag), "only for xhandlers");
  if (_exception_states == NULL) {
    _exception_states = new ValueStackStack(4);
  }
  _exception_states->append(state);
  return _exception_states->length() - 1;
}


void BlockBegin::iterate_preorder(boolArray& mark, BlockClosure* closure) {
  if (!mark.at(block_id())) {
    mark.at_put(block_id(), true);
    closure->block_do(this);
    BlockEnd* e = end(); // must do this after block_do because block_do may change it!
    { for (int i = number_of_exception_handlers() - 1; i >= 0; i--) exception_handler_at(i)->iterate_preorder(mark, closure); }
    { for (int i = e->number_of_sux            () - 1; i >= 0; i--) e->sux_at           (i)->iterate_preorder(mark, closure); }
  }
}


void BlockBegin::iterate_postorder(boolArray& mark, BlockClosure* closure) {
  if (!mark.at(block_id())) {
    mark.at_put(block_id(), true);
    BlockEnd* e = end();
    { for (int i = number_of_exception_handlers() - 1; i >= 0; i--) exception_handler_at(i)->iterate_postorder(mark, closure); }
    { for (int i = e->number_of_sux            () - 1; i >= 0; i--) e->sux_at           (i)->iterate_postorder(mark, closure); }
    closure->block_do(this);
  }
}


void BlockBegin::iterate_preorder(BlockClosure* closure) {
  boolArray mark(number_of_blocks(), false);
  iterate_preorder(mark, closure);
}


void BlockBegin::iterate_postorder(BlockClosure* closure) {
  boolArray mark(number_of_blocks(), false);
  iterate_postorder(mark, closure);
}


void BlockBegin::block_values_do(void f(Value*)) {
  for (Instruction* n = this; n != NULL; n = n->next()) n->values_do(f);
}


#ifndef PRODUCT
  #define TRACE_PHI(code) if (PrintPhiFunctions) { code; }
#else
  #define TRACE_PHI(coce)
#endif


bool BlockBegin::try_merge(ValueStack* new_state) {
  TRACE_PHI(tty->print_cr("********** try_merge for block B%d", block_id()));

  // local variables used for state iteration
  int index;
  Value new_value, existing_value;

  ValueStack* existing_state = state();
  if (existing_state == NULL) {
    TRACE_PHI(tty->print_cr("first call of try_merge for this block"));

    if (is_set(BlockBegin::was_visited_flag)) {
      // this actually happens for complicated jsr/ret structures
      return false; // BAILOUT in caller
    }

    // copy state because it is altered
    new_state = new_state->copy();

    // Use method liveness to invalidate dead locals
    MethodLivenessResult liveness = new_state->scope()->method()->liveness_at_bci(bci());
    if (liveness.is_valid()) {
      assert((int)liveness.size() == new_state->locals_size(), "error in use of liveness");

      for_each_local_value(new_state, index, new_value) {
        if (!liveness.at(index) || new_value->type()->is_illegal()) {
          new_state->invalidate_local(index);
          TRACE_PHI(tty->print_cr("invalidating dead local %d", index));
        }
      }
    }

    if (is_set(BlockBegin::parser_loop_header_flag)) {
      TRACE_PHI(tty->print_cr("loop header block, initializing phi functions"));

      for_each_stack_value(new_state, index, new_value) {
        new_state->setup_phi_for_stack(this, index);
        TRACE_PHI(tty->print_cr("creating phi-function %c%d for stack %d", new_state->stack_at(index)->type()->tchar(), new_state->stack_at(index)->id(), index));
      }

      BitMap requires_phi_function = new_state->scope()->requires_phi_function();

      for_each_local_value(new_state, index, new_value) {
        bool requires_phi = requires_phi_function.at(index) || (new_value->type()->is_double_word() && requires_phi_function.at(index + 1));
        if (requires_phi || !SelectivePhiFunctions) {
          new_state->setup_phi_for_local(this, index);
          TRACE_PHI(tty->print_cr("creating phi-function %c%d for local %d", new_state->local_at(index)->type()->tchar(), new_state->local_at(index)->id(), index));
        }
      }
    }

    // initialize state of block
    set_state(new_state);

  } else if (existing_state->is_same_across_scopes(new_state)) {
    TRACE_PHI(tty->print_cr("exisiting state found"));

    // Inlining may cause the local state not to match up, so walk up
    // the new state until we get to the same scope as the
    // existing and then start processing from there.
    while (existing_state->scope() != new_state->scope()) {
      new_state = new_state->caller_state();
      assert(new_state != NULL, "could not match up scopes");

      assert(false, "check if this is necessary");
    }

    assert(existing_state->scope() == new_state->scope(), "not matching");
    assert(existing_state->locals_size() == new_state->locals_size(), "not matching");
    assert(existing_state->stack_size() == new_state->stack_size(), "not matching");

    if (is_set(BlockBegin::was_visited_flag)) {
      TRACE_PHI(tty->print_cr("loop header block, phis must be present"));

      if (!is_set(BlockBegin::parser_loop_header_flag)) {
        // this actually happens for complicated jsr/ret structures
        return false; // BAILOUT in caller
      }

      for_each_local_value(existing_state, index, existing_value) {
        Value new_value = new_state->local_at(index);
        if (new_value == NULL || new_value->type()->tag() != existing_value->type()->tag()) {
          // The old code invalidated the phi function here
          // Because dead locals are replaced with NULL, this is a very rare case now, so simply bail out
          return false; // BAILOUT in caller
        }
      }

#ifdef ASSERT
      // check that all necessary phi functions are present
      for_each_stack_value(existing_state, index, existing_value) {
        assert(existing_value->as_Phi() != NULL && existing_value->as_Phi()->block() == this, "phi function required");
      }
      for_each_local_value(existing_state, index, existing_value) {
        assert(existing_value == new_state->local_at(index) || (existing_value->as_Phi() != NULL && existing_value->as_Phi()->as_Phi()->block() == this), "phi function required");
      }
#endif

    } else {
      TRACE_PHI(tty->print_cr("creating phi functions on demand"));

      // create necessary phi functions for stack
      for_each_stack_value(existing_state, index, existing_value) {
        Value new_value = new_state->stack_at(index);
        Phi* existing_phi = existing_value->as_Phi();

        if (new_value != existing_value && (existing_phi == NULL || existing_phi->block() != this)) {
          existing_state->setup_phi_for_stack(this, index);
          TRACE_PHI(tty->print_cr("creating phi-function %c%d for stack %d", existing_state->stack_at(index)->type()->tchar(), existing_state->stack_at(index)->id(), index));
        }
      }

      // create necessary phi functions for locals
      for_each_local_value(existing_state, index, existing_value) {
        Value new_value = new_state->local_at(index);
        Phi* existing_phi = existing_value->as_Phi();

        if (new_value == NULL || new_value->type()->tag() != existing_value->type()->tag()) {
          existing_state->invalidate_local(index);
          TRACE_PHI(tty->print_cr("invalidating local %d because of type mismatch", index));
        } else if (new_value != existing_value && (existing_phi == NULL || existing_phi->block() != this)) {
          existing_state->setup_phi_for_local(this, index);
          TRACE_PHI(tty->print_cr("creating phi-function %c%d for local %d", existing_state->local_at(index)->type()->tchar(), existing_state->local_at(index)->id(), index));
        }
      }
    }

    assert(existing_state->caller_state() == new_state->caller_state(), "caller states must be equal");

  } else {
    assert(false, "stack or locks not matching (invalid bytecodes)");
    return false;
  }

  TRACE_PHI(tty->print_cr("********** try_merge for block B%d successful", block_id()));

  return true;
}


#ifndef PRODUCT
void BlockBegin::print_block() {
  InstructionPrinter ip;
  print_block(ip, false);
}


void BlockBegin::print_block(InstructionPrinter& ip, bool live_only) {
  ip.print_instr(this); tty->cr();
  ip.print_stack(this->state()); tty->cr();
  ip.print_inline_level(this);
  ip.print_head();
  for (Instruction* n = next(); n != NULL; n = n->next()) {
    if (!live_only || n->is_pinned() || n->use_count() > 0) {
      ip.print_line(n);
    }
  }
  tty->cr();
}
#endif // PRODUCT


// Implementation of BlockList

void BlockList::iterate_forward (BlockClosure* closure) {
  const int l = length();
  for (int i = 0; i < l; i++) closure->block_do(at(i));
}


void BlockList::iterate_backward(BlockClosure* closure) {
  for (int i = length() - 1; i >= 0; i--) closure->block_do(at(i));
}


void BlockList::blocks_do(void f(BlockBegin*)) {
  for (int i = length() - 1; i >= 0; i--) f(at(i));
}


void BlockList::values_do(void f(Value*)) {
  for (int i = length() - 1; i >= 0; i--) at(i)->block_values_do(f);
}


#ifndef PRODUCT
void BlockList::print(bool cfg_only, bool live_only) {
  InstructionPrinter ip;
  for (int i = 0; i < length(); i++) {
    BlockBegin* block = at(i);
    if (cfg_only) {
      ip.print_instr(block); tty->cr();
    } else {
      block->print_block(ip, live_only);
    }
  }
}
#endif // PRODUCT


// Implementation of BlockEnd

void BlockEnd::set_begin(BlockBegin* begin) {
  BlockList* sux = NULL;
  if (begin != NULL) {
    sux = begin->successors();
  } else if (_begin != NULL) {
    // copy our sux list
    BlockList* sux = new BlockList(_begin->number_of_sux());
    for (int i = 0; i < _begin->number_of_sux(); i++) {
      sux->append(_begin->sux_at(i));
    }
  }
  _sux = sux;
  _begin = begin;
}


void BlockEnd::substitute_sux(BlockBegin* old_sux, BlockBegin* new_sux) {
  substitute(*_sux, old_sux, new_sux);
}


void BlockEnd::other_values_do(void f(Value*)) {
  if (state_before() != NULL) state_before()->values_do(f);
}


// Implementation of Phi

// Normal phi functions take their operands from the last instruction of the
// predecessor. Special handling is needed for xhanlder entries because there
// the state of arbitrary instructions are needed.

Value Phi::operand_at(int i) const {
  ValueStack* state;
  if (_block->is_set(BlockBegin::exception_entry_flag)) {
    state = _block->exception_state_at(i);
  } else {
    state = _block->pred_at(i)->end()->state();
  }
  assert(state != NULL, "");

  if (is_local()) {
    return state->local_at(local_index());
  } else {
    return state->stack_at(stack_index());
  }
}


int Phi::operand_count() const {
  if (_block->is_set(BlockBegin::exception_entry_flag)) {
    return _block->number_of_exception_states();
  } else {
    return _block->number_of_preds();
  }
}


// Implementation of Throw

void Throw::state_values_do(void f(Value*)) {
  BlockEnd::state_values_do(f);
}
