/*
 * Copyright (c) 2013, 2015, Oracle and/or its affiliates. All rights reserved.
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



package org.graalvm.compiler.lir.aarch64;

import static jdk.vm.ci.code.ValueUtil.asRegister;

import org.graalvm.compiler.asm.aarch64.AArch64MacroAssembler;
import org.graalvm.compiler.lir.LIRInstructionClass;
import org.graalvm.compiler.lir.asm.CompilationResultBuilder;

import jdk.vm.ci.aarch64.AArch64Kind;
import jdk.vm.ci.code.Register;
import jdk.vm.ci.meta.AllocatableValue;

/**
 * Instruction that reinterprets some bit pattern as a different type. It is possible to reinterpret
 * the following: - int <-> float - long <-> double
 */
public class AArch64ReinterpretOp extends AArch64LIRInstruction {
    private static final LIRInstructionClass<AArch64ReinterpretOp> TYPE = LIRInstructionClass.create(AArch64ReinterpretOp.class);

    @Def protected AllocatableValue resultValue;
    @Use protected AllocatableValue inputValue;

    public AArch64ReinterpretOp(AllocatableValue resultValue, AllocatableValue inputValue) {
        super(TYPE);
        AArch64Kind from = (AArch64Kind) inputValue.getPlatformKind();
        AArch64Kind to = (AArch64Kind) resultValue.getPlatformKind();
        assert from.getSizeInBytes() == to.getSizeInBytes() && from.isInteger() ^ to.isInteger();
        this.resultValue = resultValue;
        this.inputValue = inputValue;
    }

    @Override
    public void emitCode(CompilationResultBuilder crb, AArch64MacroAssembler masm) {
        Register result = asRegister(resultValue);
        Register input = asRegister(inputValue);
        AArch64Kind to = (AArch64Kind) resultValue.getPlatformKind();
        final int size = to.getSizeInBytes() * Byte.SIZE;
        masm.fmov(size, result, input);
    }
}
