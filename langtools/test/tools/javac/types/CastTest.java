/*
 * Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved.
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

/*
 * @test
 * @bug 7006109
 * @summary Add test library to simplify the task of writing automated type-system tests
 * @author mcimadamore
 * @library .
 * @run main CastTest
 */

import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.*;
import com.sun.tools.javac.code.Symbol.*;
import java.lang.reflect.Array;

import static com.sun.tools.javac.code.Flags.*;

/**
 * Check invariants in cast conversion involving primitive types and arrays
 */
public class CastTest extends TypeHarness {

    Type[] allTypes;

    static final boolean T = true;
    static final boolean F = false;

    boolean[][] cast_result = {
                //byte, short, int, long, float, double, char, bool, C, +C, I, T, byte[], short[], int[], long[], float[], double[], char[], bool[], C[], +C[], I[], T[]
    /*byte*/    { T   , T    , T  , T   , T    , T     , T   , F   , F, F , F, F, F     , F      , F    , F     , F      , F       , F     , F     , F  , F   , F  , F },
    /*short*/   { T   , T    , T  , T   , T    , T     , T   , F   , F, F , F, F, F     , F      , F    , F     , F      , F       , F     , F     , F  , F   , F  , F },
    /*int*/     { T   , T    , T  , T   , T    , T     , T   , F   , F, F , F, F, F     , F      , F    , F     , F      , F       , F     , F     , F  , F   , F  , F },
    /*long*/    { T   , T    , T  , T   , T    , T     , T   , F   , F, F , F, F, F     , F      , F    , F     , F      , F       , F     , F     , F  , F   , F  , F },
    /*float*/   { T   , T    , T  , T   , T    , T     , T   , F   , F, F , F, F, F     , F      , F    , F     , F      , F       , F     , F     , F  , F   , F  , F },
    /*double*/  { T   , T    , T  , T   , T    , T     , T   , F   , F, F , F, F, F     , F      , F    , F     , F      , F       , F     , F     , F  , F   , F  , F },
    /*char*/    { T   , T    , T  , T   , T    , T     , T   , F   , F, F , F, F, F     , F      , F    , F     , F      , F       , F     , F     , F  , F   , F  , F },
    /*bool*/    { F   , F    , F  , F   , F    , F     , F   , T   , F, F , F, F, F     , F      , F    , F     , F      , F       , F     , F     , F  , F   , F  , F },
    /*C*/       { F   , F    , F  , F   , F    , F     , F   , F   , T, F , T, T, F     , F      , F    , F     , F      , F       , F     , F     , F  , F   , F  , F },
    /*+C*/      { F   , F    , F  , F   , F    , F     , F   , F   , F, T , F, T, F     , F      , F    , F     , F      , F       , F     , F     , F  , F   , F  , F },
    /*I*/       { F   , F    , F  , F   , F    , F     , F   , F   , T, F , T, T, F     , F      , F    , F     , F      , F       , F     , F     , F  , F   , F  , F },
    /*T*/       { F   , F    , F  , F   , F    , F     , F   , F   , T, T , T, T, T     , T      , T    , T     , T      , T       , T     , T     , T  , T   , T  , T },
    /*byte[]*/  { F   , F    , F  , F   , F    , F     , F   , F   , F, F , F, T, T     , F      , F    , F     , F      , F       , F     , F     , F  , F   , F  , F },
    /*short[]*/ { F   , F    , F  , F   , F    , F     , F   , F   , F, F , F, T, F     , T      , F    , F     , F      , F       , F     , F     , F  , F   , F  , F },
    /*int[]*/   { F   , F    , F  , F   , F    , F     , F   , F   , F, F , F, T, F     , F      , T    , F     , F      , F       , F     , F     , F  , F   , F  , F },
    /*long[]*/  { F   , F    , F  , F   , F    , F     , F   , F   , F, F , F, T, F     , F      , F    , T     , F      , F       , F     , F     , F  , F   , F  , F },
    /*float[]*/ { F   , F    , F  , F   , F    , F     , F   , F   , F, F , F, T, F     , F      , F    , F     , T      , F       , F     , F     , F  , F   , F  , F },
    /*double[]*/{ F   , F    , F  , F   , F    , F     , F   , F   , F, F , F, T, F     , F      , F    , F     , F      , T       , F     , F     , F  , F   , F  , F },
    /*char[]*/  { F   , F    , F  , F   , F    , F     , F   , F   , F, F , F, T, F     , F      , F    , F     , F      , F       , T     , F     , F  , F   , F  , F },
    /*bool[]*/  { F   , F    , F  , F   , F    , F     , F   , F   , F, F , F, T, F     , F      , F    , F     , F      , F       , F     , T     , F  , F   , F  , F },
    /*C[]*/     { F   , F    , F  , F   , F    , F     , F   , F   , F, F , F, T, F     , F      , F    , F     , F      , F       , F     , F     , T  , F   , T  , T },
    /*+C[]*/    { F   , F    , F  , F   , F    , F     , F   , F   , F, F , F, T, F     , F      , F    , F     , F      , F       , F     , F     , F  , T   , F  , T },
    /*I[]*/     { F   , F    , F  , F   , F    , F     , F   , F   , F, F , F, T, F     , F      , F    , F     , F      , F       , F     , F     , T  , F   , T  , T },
    /*T[]*/     { F   , F    , F  , F   , F    , F     , F   , F   , F, F , F, T, F     , F      , F    , F     , F      , F       , F     , F     , T  , T   , T  , T }};

    CastTest() {
        Type[] primitiveTypes = {
            predef.byteType,
            predef.shortType,
            predef.intType,
            predef.longType,
            predef.floatType,
            predef.doubleType,
            predef.charType,
            predef.booleanType };

        Type[] referenceTypes = {
            fac.Class(),
            fac.Class(FINAL),
            fac.Interface(),
            fac.TypeVariable() };

        Type[] arrayTypes = new Type[primitiveTypes.length + referenceTypes.length];
        int idx = 0;
        for (Type t : join(Type.class, primitiveTypes, referenceTypes)) {
            arrayTypes[idx++] = fac.Array(t);
        }

        allTypes = join(Type.class, primitiveTypes, referenceTypes, arrayTypes);
    }

    void test() {
        for (int i = 0; i < allTypes.length ; i++) {
            for (int j = 0; j < allTypes.length ; j++) {
                assertCastable(allTypes[i], allTypes[j], cast_result[i][j]);
            }
        }
    }

    @SuppressWarnings("unchecked")
    <T> T[] join(Class<T> type, T[]... args) {
        int totalLength = 0;
        for (T[] arr : args) {
            totalLength += arr.length;
        }
        T[] new_arr = (T[])Array.newInstance(type, totalLength);
        int idx = 0;
        for (T[] arr : args) {
            System.arraycopy(arr, 0, new_arr, idx, arr.length);
            idx += arr.length;
        }
        return new_arr;
    }

    public static void main(String[] args) {
        new CastTest().test();
    }
}
