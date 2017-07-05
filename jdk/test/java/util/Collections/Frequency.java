/*
 * Copyright 2003 Sun Microsystems, Inc.  All Rights Reserved.
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
 */

/*
 * @test
 * @bug     4193200
 * @summary Basic test for Collections.frequency
 * @author  Josh Bloch
 */

import java.util.*;

public class Frequency {
    final static int N = 100;
    public static void main(String args[]) {
        test(new ArrayList<Integer>());
        test(new LinkedList<Integer>());
    }

    static void test(List<Integer> list) {
        for (int i = 0; i < N; i++)
            for (int j = 0; j < i; j++)
                list.add(i);
        Collections.shuffle(list);

        for (int i = 0; i < N; i++)
            if (Collections.frequency(list, i) != i)
                throw new RuntimeException(list.getClass() + ": " + i);
    }
}
