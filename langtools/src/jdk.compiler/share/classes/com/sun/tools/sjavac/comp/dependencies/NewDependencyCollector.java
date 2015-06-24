/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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

package com.sun.tools.sjavac.comp.dependencies;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.DefinedBy;
import com.sun.tools.javac.util.DefinedBy.Api;
import com.sun.tools.javac.util.Dependencies.GraphDependencies;
import com.sun.tools.javac.util.Dependencies.GraphDependencies.CompletionNode;
import com.sun.tools.javac.util.GraphUtils.Node;
import com.sun.tools.sjavac.Util;
import com.sun.tools.sjavac.comp.JavaFileObjectWithLocation;
import com.sun.tools.sjavac.comp.PubAPIs;


public class NewDependencyCollector implements TaskListener {

    private final Context context;
    private final Collection<JavaFileObject> explicitJFOs;

    private Map<String, Map<String, Set<String>>> deps;
    private Map<String, Map<String, Set<String>>> cpDeps;

    public NewDependencyCollector(Context context,
                                  Collection<JavaFileObject> explicitJFOs) {
        this.context = context;
        this.explicitJFOs = explicitJFOs;
    }

    @Override
    @DefinedBy(Api.COMPILER_TREE)
    public void finished(TaskEvent e) {
        if (e.getKind() == TaskEvent.Kind.COMPILATION) {
            collectPubApisOfDependencies(context, explicitJFOs);
            deps = getDependencies(context, explicitJFOs, false);
            cpDeps = getDependencies(context, explicitJFOs, true);
        }
    }

    public Map<String, Map<String, Set<String>>> getDependencies(boolean cp) {
        return cp ? cpDeps : deps;
    }

    private Set<CompletionNode> getDependencyNodes(Context context,
                                                   Collection<JavaFileObject> explicitJFOs,
                                                   boolean explicits) {
        GraphDependencies deps = (GraphDependencies) GraphDependencies.instance(context);

        return deps.getNodes()
                   .stream()
                   .filter(n -> n instanceof CompletionNode)
                   .map(n -> (CompletionNode) n)
                   .filter(n -> n.getClassSymbol().fullname != null)
                   .filter(n -> explicits == explicitJFOs.contains(n.getClassSymbol().classfile))
                   .collect(Collectors.toSet());
    }

    private void collectPubApisOfDependencies(Context context,
                                              Collection<JavaFileObject> explicitJFOs) {
        PubAPIs pubApis = PubAPIs.instance(context);
        for (CompletionNode cDepNode : getDependencyNodes(context, explicitJFOs, false)) {
            ClassSymbol cs = cDepNode.getClassSymbol().outermostClass();
            Location loc = getLocationOf(cs);
            // We're completely ignorant of PLATFORM_CLASS_PATH classes
            if (loc == StandardLocation.CLASS_PATH || loc == StandardLocation.SOURCE_PATH)
                pubApis.visitPubapi(cs);
        }
    }

    private Location getLocationOf(ClassSymbol cs) {
        JavaFileObject jfo = cs.outermostClass().classfile;
        if (jfo instanceof JavaFileObjectWithLocation) {
            return ((JavaFileObjectWithLocation<?>) jfo).getLocation();
        }

        // jfo is most likely on PLATFORM_CLASS_PATH.
        // See notes in SmartFileManager::locWrap

        return null;
    }

    // :Package -> fully qualified class name [from] -> set of fully qualified class names [to]
    private Map<String, Map<String, Set<String>>> getDependencies(Context context,
                                                                  Collection<JavaFileObject> explicitJFOs,
                                                                  boolean cp) {
        Map<String, Map<String, Set<String>>> result = new HashMap<>();

        for (CompletionNode cnode : getDependencyNodes(context, explicitJFOs, true)) {

            String fqDep = cnode.getClassSymbol().outermostClass().flatname.toString();
            String depPkg = Util.pkgNameOfClassName(fqDep);

            Map<String, Set<String>> depsForThisClass = result.get(depPkg);
            if (depsForThisClass == null)
                result.put(depPkg, depsForThisClass = new HashMap<>());

            for (Node<?,?> depNode : cnode.getDependenciesByKind(GraphDependencies.Node.DependencyKind.REQUIRES)) {
                boolean isCompletionNode = depNode instanceof CompletionNode;
                if (isCompletionNode) {
                    CompletionNode cDepNode = (CompletionNode) depNode;
                    if (cDepNode == cnode)
                        continue;
                    if (cDepNode.getClassSymbol().fullname == null) // Anonymous class
                        continue;
                    Location depLoc = getLocationOf(cDepNode.getClassSymbol());
                    boolean relevant = (cp  && depLoc == StandardLocation.CLASS_PATH)
                                    || (!cp && depLoc == StandardLocation.SOURCE_PATH);
                    if (!relevant)
                        continue;

                    Set<String> fqDeps = depsForThisClass.get(fqDep);
                    if (fqDeps == null)
                        depsForThisClass.put(fqDep, fqDeps = new HashSet<>());
                    fqDeps.add(cDepNode.getClassSymbol().outermostClass().flatname.toString());
                }
            }
        }
        return result;
    }

}
