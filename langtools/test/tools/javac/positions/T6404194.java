/*
 * Copyright 2006 Sun Microsystems, Inc.  All Rights Reserved.
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
 * @bug     6404194
 * @summary javac parser generates incorrect end position for annotations with parentheses.
 * @author  Peter von der Ah\u00e9
 */

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import static javax.tools.JavaFileObject.Kind.SOURCE;
import javax.tools.ToolProvider;

public class T6404194 {
    public static void main(String... args) throws IOException {
        class MyFileObject extends SimpleJavaFileObject {
            MyFileObject() {
                super(URI.create("myfo:///Test.java"), SOURCE);
            }
            @Override
            public String getCharContent(boolean ignoreEncodingErrors) {
                //      0         1          2          3
                //      01234567890123456 7890 123456789012345
                return "@SuppressWarning(\"foo\") @Deprecated class Test { Test() { } }";
            }
        }
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        List<JavaFileObject> compilationUnits =
                Collections.<JavaFileObject>singletonList(new MyFileObject());
        JavacTask task = (JavacTask)javac.getTask(null, null, null, null, null, compilationUnits);
        Trees trees = Trees.instance(task);
        CompilationUnitTree toplevel = task.parse().iterator().next();
        ClassTree classTree = (ClassTree)toplevel.getTypeDecls().get(0);
        List<? extends Tree> annotations = classTree.getModifiers().getAnnotations();
        Tree tree1 = annotations.get(0);
        Tree tree2 = annotations.get(1);
        long pos = trees.getSourcePositions().getStartPosition(toplevel, tree1);
        if (pos != 0)
            throw new AssertionError(String.format("Start pos for %s is incorrect (%s)!",
                                                   tree1, pos));
        pos = trees.getSourcePositions().getEndPosition(toplevel, tree1);
        if (pos != 23)
            throw new AssertionError(String.format("End pos for %s is incorrect (%s)!",
                                                   tree1, pos));
        pos = trees.getSourcePositions().getStartPosition(toplevel, tree2);
        if (pos != 24)
            throw new AssertionError(String.format("Start pos for %s is incorrect (%s)!",
                                                   tree2, pos));
        pos = trees.getSourcePositions().getEndPosition(toplevel, tree2);
        if (pos != 35)
            throw new AssertionError(String.format("End pos for %s is incorrect (%s)!",
                                                   tree2, pos));
    }
}
