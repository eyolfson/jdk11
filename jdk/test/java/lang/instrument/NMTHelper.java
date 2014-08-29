/*
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sun.management.ManagementFactoryHelper;
import com.sun.management.DiagnosticCommandMBean;

public class NMTHelper
{
    public static void baseline() {
        executeDcmd("vmNativeMemory", "baseline");
    }

    // Total: reserved=3484685KB +293KB, committed=266629KB +293KB
    private static Pattern totalLine = Pattern.compile("^Total: reserved=\\d+KB .*KB, committed=\\d+KB (.*)KB$");

    public static long committedDiff() throws Exception {
        String res = (String) executeDcmd("vmNativeMemory", "detail.diff");
        String[] lines = res.split("\n");
        for (String line : lines) {
            Matcher matcher = totalLine.matcher(line);
            if (matcher.matches()) {
                String committed = matcher.group(1);
                return Long.parseLong(committed);
            }
        }
        throw new Exception("Could not find the Total line in the NMT output.");
    }

    private static String executeDcmd(String cmd, String ... args) {
        DiagnosticCommandMBean dcmd = ManagementFactoryHelper.getDiagnosticCommandMBean();
        Object[] dcmdArgs = {args};
        String[] signature = {String[].class.getName()};

        try {
            System.out.print("> " + cmd + " ");
            for (String s : args) {
                System.out.print(s + " ");
            }
            System.out.println(":");
            String result = (String) dcmd.invoke(cmd, dcmdArgs, signature);
            System.out.println(result);
            return result;
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
