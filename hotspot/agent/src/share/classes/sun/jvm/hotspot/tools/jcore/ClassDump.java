/*
 * Copyright (c) 2002, 2011, Oracle and/or its affiliates. All rights reserved.
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

package sun.jvm.hotspot.tools.jcore;

import java.io.*;
import java.util.jar.JarOutputStream;
import java.util.jar.JarEntry;
import java.util.jar.Manifest;
import sun.jvm.hotspot.memory.*;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.tools.*;

public class ClassDump extends Tool {
    private ClassFilter classFilter;
    private String      outputDirectory;
    private JarOutputStream jarStream;

    public void setClassFilter(ClassFilter cf) {
        classFilter = cf;
    }

    public void setOutputDirectory(String od) {
        outputDirectory = od;
        if (jarStream != null) {
            try {
                jarStream.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        jarStream = null;
    }

    public void setJarOutput(String jarFileName) throws IOException {
        jarStream = new JarOutputStream(new FileOutputStream(jarFileName), new Manifest());
        outputDirectory = null;
    }

    public void run() {
        // Ready to go with the database...
        try {

            // walk through the system dictionary
            SystemDictionary dict = VM.getVM().getSystemDictionary();
            dict.classesDo(new SystemDictionary.ClassVisitor() {
                    public void visit(Klass k) {
                        if (k instanceof InstanceKlass) {
                            try {
                                dumpKlass((InstanceKlass) k);
                            } catch (Exception e) {
                                System.out.println(k.getName().asString());
                                e.printStackTrace();
                            }
                        }
                    }
                });
        }
        catch (AddressException e) {
            System.err.println("Error accessing address 0x"
                               + Long.toHexString(e.getAddress()));
            e.printStackTrace();
        }
        if (jarStream != null) {
            try {
                jarStream.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            jarStream = null;
        }
    }

    public String getName() {
        return "jcore";
    }

    private void dumpKlass(InstanceKlass kls) {
        if (classFilter != null && ! classFilter.canInclude(kls) ) {
            return;
        }

        String klassName = kls.getName().asString();
        klassName = klassName.replace('/', File.separatorChar);
        try {
            OutputStream os = null;
            if (jarStream != null) {
                jarStream.putNextEntry(new JarEntry(klassName + ".class"));
                os = jarStream;
            } else {
                int index = klassName.lastIndexOf(File.separatorChar);
                File dir = null;
                if (index != -1) {
                    String dirName = klassName.substring(0, index);
                    dir = new File(outputDirectory,  dirName);
                } else {
                    dir = new File(outputDirectory);
                }

                dir.mkdirs();
                File f = new File(dir, klassName.substring(index + 1) + ".class");
                f.createNewFile();
                os = new BufferedOutputStream(new FileOutputStream(f));
            }
            try {
                ClassWriter cw = new ClassWriter(kls, os);
                cw.write();
            } finally {
                if (os != jarStream) {
                    os.close();
                }
            }
        } catch(IOException exp) {
            exp.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // load class filters
        ClassFilter classFilter = null;
        String filterClassName = System.getProperty("sun.jvm.hotspot.tools.jcore.filter");
        if (filterClassName != null) {
            try {
                Class filterClass = Class.forName(filterClassName);
                classFilter = (ClassFilter) filterClass.newInstance();
            } catch(Exception exp) {
                System.err.println("Warning: Can not create class filter!");
            }
        }

        String outputDirectory = System.getProperty("sun.jvm.hotspot.tools.jcore.outputDir");
        if (outputDirectory == null)
            outputDirectory = ".";


        ClassDump cd = new ClassDump();
        cd.setClassFilter(classFilter);
        cd.setOutputDirectory(outputDirectory);
        cd.start(args);
        cd.stop();
    }
}
