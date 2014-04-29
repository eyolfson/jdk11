/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.sjavac.options;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.tools.sjavac.Module;
import com.sun.tools.sjavac.ProblemException;
import com.sun.tools.sjavac.Source;

/**
 * Represents a directory to be used for input to sjavac. (For instance a
 * sourcepath or classpath.)
 */
public class SourceLocation {

    // Path to the root directory
    private Path path;

    // Package include / exclude patterns and file includes / excludes.
    List<String> includes, excludes, includedFiles, excludedFiles;

    public SourceLocation(Path path,
                          List<String> includes,
                          List<String> excludes,
                          List<String> includedFiles,
                          List<String> excludedFiles) {
        this.path = path;
        this.includes = includes;
        this.excludes = excludes;
        this.includedFiles = includedFiles;
        this.excludedFiles = excludedFiles;
    }


    /**
     * Finds all files with the given suffix that pass the include / exclude
     * filters in this source location.
     *
     * @param suffixes The set of suffixes to search for
     * @param foundFiles The map in which to store the found files
     * @param foundModules The map in which to store the found modules
     * @param currentModule The current module
     * @param permitSourcesInDefaultPackage true if sources in default package
     *                                      are to be permitted
     * @param inLinksrc true if in link source
     */
    public void findSourceFiles(Set<String> suffixes,
                                Map<String, Source> foundFiles,
                                Map<String, Module> foundModules,
                                Module currentModule,
                                boolean permitSourcesInDefaultPackage,
                                boolean inLinksrc) {
        try {
            Source.scanRoot(path.toFile(), suffixes, excludes, includes,
                    excludedFiles, includedFiles, foundFiles, foundModules,
                    currentModule, permitSourcesInDefaultPackage, false,
                    inLinksrc);
        } catch (ProblemException e) {
            e.printStackTrace();
        }
    }

    /** Get the root directory of this source location */
    public Path getPath() {
        return path;
    }

    /** Get the package include patterns */
    public List<String> getIncludes() {
        return includes;
    }

    /** Get the package exclude patterns */
    public List<String> getExcludes() {
        return excludes;
    }

    /** Get the file include patterns */
    public List<String> getIncludedFiles() {
        return includedFiles;
    }

    /** Get the file exclude patterns */
    public List<String> getExcludedFiles() {
        return excludedFiles;
    }

}
