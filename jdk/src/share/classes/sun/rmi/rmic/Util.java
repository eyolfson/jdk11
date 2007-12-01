/*
 * Copyright 1999-2007 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
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
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 */

package sun.rmi.rmic;

import java.io.File;
import sun.tools.java.Identifier;

/**
 * Util provides static utility methods used by other rmic classes.
 *
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 *
 * @author Bryan Atsatt
 */

public class Util implements sun.rmi.rmic.Constants {

    /**
     * Return the directory that should be used for output for a given
     * class.
     * @param theClass The fully qualified name of the class.
     * @param rootDir The directory to use as the root of the
     * package heirarchy.  May be null, in which case the current
     * working directory is used as the root.
     */
    public static File getOutputDirectoryFor(Identifier theClass,
                                             File rootDir,
                                             BatchEnvironment env) {

        File outputDir = null;
        String className = theClass.getFlatName().toString().replace('.', SIGC_INNERCLASS);
        String qualifiedClassName = className;
        String packagePath = null;
        String packageName = theClass.getQualifier().toString();

        if (packageName.length() > 0) {
            qualifiedClassName = packageName + "." + className;
            packagePath = packageName.replace('.', File.separatorChar);
        }

        // Do we have a root directory?

        if (rootDir != null) {

            // Yes, do we have a package name?

            if (packagePath != null) {

                // Yes, so use it as the root. Open the directory...

                outputDir = new File(rootDir, packagePath);

                // Make sure the directory exists...

                ensureDirectory(outputDir,env);

            } else {

                // Default package, so use root as output dir...

                outputDir = rootDir;
            }
        } else {

            // No root directory. Get the current working directory...

            String workingDirPath = System.getProperty("user.dir");
            File workingDir = new File(workingDirPath);

            // Do we have a package name?

            if (packagePath == null) {

                // No, so use working directory...

                outputDir = workingDir;

            } else {

                // Yes, so use working directory as the root...

                outputDir = new File(workingDir, packagePath);

                // Make sure the directory exists...

                ensureDirectory(outputDir,env);
            }
        }

        // Finally, return the directory...

        return outputDir;
    }

    private static void ensureDirectory (File dir, BatchEnvironment env) {
        if (!dir.exists()) {
            dir.mkdirs();
            if (!dir.exists()) {
                env.error(0,"rmic.cannot.create.dir",dir.getAbsolutePath());
                throw new InternalError();
            }
        }
    }
}
