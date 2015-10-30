/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
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
 * @modules java.base/jdk.internal.jimage
 * @run testng JImageReadTest
 * @summary Unit test for libjimage JIMAGE_Open/Read/Close
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import jdk.internal.jimage.BasicImageReader;
import jdk.internal.jimage.ImageNativeSubstrate;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.Assert;
import org.testng.TestNG;

@Test
public class JImageReadTest {

    static String javaHome = System.getProperty("java.home");
    static String imageFile = javaHome + File.separator + "lib"
            + File.separator + "modules" + File.separator
            + "bootmodules.jimage";

    @DataProvider(name="classes")
    static Object[][] loadClasses() {
        return new Object[][] {
                {"java.base", "java/lang/String.class"},
                {"java.base", "java/lang/Object.class"},
                {"java.base", "sun/reflect/generics/tree/TypeArgument.class"},
                {"java.base", "sun/net/www/content-types.properties"},
                {"java.logging", "java/util/logging/Logger.class"},
                {"java.base", "java/NOSUCHCLASS/yyy.class"},    // non-existent
                {"NOSUCHMODULE", "java/lang/Class.class"},    // non-existent
        };
    }


    @DataProvider(name="packages")
    static Object[][] loadPackages() {
        return new Object[][] {
                {"java.base", "java/lang"},
                {"java.base", "java/io"},
                {"java.logging", "java/util/logging"},
        };
    }

    /**
     * Test a class is correctly accessible from the image in a module.
     *
     * @param moduleName the module name
     * @param className the classname
     * @throws Exception is thrown if there is a test error
     */
    @Test(dataProvider="classes")
    public static void test1_ReadClasses(String moduleName, String className) throws Exception {
        final int classMagic = 0xCAFEBABE;
        final long NOT_FOUND = 0L;

        if (!(new File(imageFile)).exists()) {
            System.out.printf("Test skipped; no jimage file");
            return;
        }

        long jimageHandle = ImageNativeSubstrate.JIMAGE_Open(imageFile);
        Assert.assertTrue(jimageHandle != 0, "JIMAGE_Open failed: id: " + jimageHandle);

        long[] sizeArray = new long[1];
        long locationHandle =
                ImageNativeSubstrate.JIMAGE_FindResource(jimageHandle,
                moduleName, "9.0", className, sizeArray);
        long size = sizeArray[0];
        System.out.printf("reading: module: %s, path: %s, handle: %16x, " +
                        "location: %d, size: %d%n",
                moduleName, className, jimageHandle, locationHandle, size);
        if (moduleName.contains("NOSUCH") || className.contains("NOSUCH")) {
            Assert.assertEquals(locationHandle, NOT_FOUND,
                    "location found for non-existing module: "
                    + moduleName
                    + ", or class: " + className);
            return;         // no more to test for non-existing class
        } else {
            Assert.assertTrue(locationHandle != NOT_FOUND, "location not found: " + className);
            Assert.assertTrue(size > 0, "size of should be > 0: " + className);
        }

        // positive: read whole class
        ByteBuffer buffer = ByteBuffer.allocate((int)size);
        long actual = ImageNativeSubstrate.JIMAGE_GetResource(jimageHandle,
                locationHandle, buffer.array(), size);
        Assert.assertEquals(actual, size, "bytes read not equal bytes requested");

        if (className.endsWith(".class")) {
            int m = buffer.getInt();
            Assert.assertEquals(m, classMagic, "Classfile has bad magic number");
        }

        // Read less than the size of the artifact
        buffer.rewind();
        Arrays.fill(buffer.array(), (byte)0xc0);
        long sizeExpected = size - 10;
        actual = ImageNativeSubstrate.JIMAGE_GetResource(jimageHandle,
                locationHandle, buffer.array(), sizeExpected);
        Assert.assertEquals(actual, sizeExpected, "bytes read not equal bytes requested");

        if (className.endsWith(".class")) {
            int m1 = buffer.getInt();
            Assert.assertEquals(m1, classMagic, "Read operation succeeded but has bad magic number");
        }

        ImageNativeSubstrate.JIMAGE_Close(jimageHandle);
    }

    /**
     * For all the resource names, check the name and approximate count.
     *
     * @throws IOException thrown if an error occurs
     */
    @Test
    static void test2_ImageResources() throws IOException {
        if (!(new File(imageFile)).exists()) {
            System.out.printf("Test skipped; no jimage file");
            return;
        }

        long jimageHandle = ImageNativeSubstrate.JIMAGE_Open(imageFile);
        Assert.assertTrue(jimageHandle != 0, "JIMAGE_Open failed: id: " + jimageHandle);

        String[] names = new String[4096];
        int max = ImageNativeSubstrate.JIMAGE_Resources(jimageHandle,
                names);

        // Repeat with count available
        names = new String[max + 1];
        int count = ImageNativeSubstrate.JIMAGE_Resources(jimageHandle,
                names);
        System.out.printf(" count: %d, a class: %s\n", count, names[0]);
        int minEntryCount = 16000;
        Assert.assertTrue(max > minEntryCount,
                "missing entries, should be more than " + minEntryCount +
                ", reported: " + count);
        Assert.assertTrue(count == max,
                "unexpected count of entries, count: " + count
                        + ", max: " + max);
        for (int i = 0; i < count; i++) {
            checkFullName(names[i]);
        }

        ImageNativeSubstrate.JIMAGE_Close(jimageHandle);
    }

    /**
     * Tests that a package exists and correctly mapped to the module.
     *
     * @param moduleName the module name
     * @param packageName the package name
     * @throws IOException thrown if an error occurs
     */
    @Test(dataProvider="packages")
    static void test3_PackageToModule(String moduleName, String packageName) throws IOException {
        if (!(new File(imageFile)).exists()) {
            System.out.printf("Test skipped; no jimage file");
            return;
        }

        long jimageHandle = ImageNativeSubstrate.JIMAGE_Open(imageFile);
        Assert.assertTrue(jimageHandle != 0, "JIMAGE_Open failed: id: " + jimageHandle);

        String result = ImageNativeSubstrate.JIMAGE_PackageToModule(jimageHandle, packageName);
        System.out.printf(" package: %s, module: %s%n", packageName, result);
        Assert.assertEquals(result, moduleName, "wrong module for package: " + packageName);

        ImageNativeSubstrate.JIMAGE_Close(jimageHandle);
    }


    static void checkFullName(String path) {
        int next = 0;
        String m = null;
        String p = null;
        String b = null;
        String e = null;
        if (path.startsWith("/")) {
            next = path.indexOf('/', 1);
            m = path.substring(1, next);
            next = next + 1;
        }
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash > next) {
            // has a parent
            p = path.substring(next, lastSlash);
            next = lastSlash + 1;
        }
        int period = path.indexOf('.', next);
        if (period > next) {
            b = path.substring(next, period);
            e = path.substring(period + 1);
        } else {
            b = path.substring(next);
        }
        Assert.assertNotNull(m, "module must be non-empty");
        Assert.assertNotNull(b, "base name must be non-empty");
    }

    /**
     * Verify that all of the resource names from BasicImageReader
     * match those returned from the native JIMAGE_Resources iterator.
     * Names that start with /modules, /packages, and bootmodules.jdata
     * must appear in the names from JIMAGE_Resource iterator;
     * from the BasicImageReader they are ignored.
     */
    @Test
    static void test4_verifyNames() {
        if (!(new File(imageFile)).exists()) {
            System.out.printf("Test skipped; no jimage file");
            return;
        }

        try {
            String[] names = BasicImageReader_Names();
            //writeNames("/tmp/basic-names.txt", names);              // debug

            // Read all the names from the native JIMAGE API
            String[] nativeNames = JIMAGE_Names();
            //writeNames("/tmp/native-names.txt", nativeNames);       // debug


            int modCount = 0;
            int pkgCount = 0;
            int otherCount = 0;
            for (String n : nativeNames) {
                if (n.startsWith("/modules/")) {
                    modCount++;
                } else if (n.startsWith("/packages/")) {
                    pkgCount++;
                } else {
                    otherCount++;
                }
            }
            System.out.printf("native name count: %d, modCount: %d, pkgCount: %d, otherCount: %d%n",
                    names.length, modCount, pkgCount, otherCount);

            Assert.assertEquals(modCount, 0, "JIMAGE_ResourceIterator should not return any '/modules' paths");
            Assert.assertEquals(pkgCount, 0, "JIMAGE_ResourceIterator should not return any '/packages' paths");

            // Sort and merge the two arrays.  Every name should appear exactly twice.
            Arrays.sort(names);
            Arrays.sort(nativeNames);
            String[] combined = Arrays.copyOf(names, nativeNames.length + names.length);
            System.arraycopy(nativeNames,0, combined, names.length, nativeNames.length);
            Arrays.sort(combined);
            int missing = 0;
            for (int i = 0; i < combined.length; i++) {
                String s = combined[i];
                if (isMetaName(s)) {
                    // Ignore /modules and /packages in BasicImageReader names
                    continue;
                }

                if (i < combined.length - 1 && s.equals(combined[i + 1])) {
                    i++;        // string appears in both java and native
                    continue;
                }

                missing++;
                int ndx = Arrays.binarySearch(names, s);
                String which = (ndx >= 0) ? "java BasicImageReader" : "native JIMAGE_Resources";
                System.out.printf("Missing Resource: %s found only via %s%n", s, which);
            }
            Assert.assertEquals(missing, 0, "Resources missing");

        } catch (IOException ioe) {
            Assert.fail("I/O exception", ioe);
        }
    }

    /**
     * Return true if the name is one of the meta-data names
     * @param name a name
     * @return return true if starts with either /packages or /modules
     */
    static boolean isMetaName(String name) {
        return name.startsWith("/modules")
                || name.startsWith("/packages")
                || name.startsWith("META-INF/services")
                || name.equals("bootmodules.jdata");
    }

    /**
     * Return all of the names from BasicImageReader.
     * @return the names returned from BasicImageReader
     */
    static String[] BasicImageReader_Names() throws IOException {
        String[] names = null;
        try (BasicImageReader reader = BasicImageReader.open(imageFile)) {
            names = reader.getEntryNames();
        } catch (IOException ioe) {
            Assert.fail("I/O exception", ioe);
        }
        return names;
    }

    /**
     * Returns an array of all of the names returned from JIMAGE_Resources
     */
    static String[] JIMAGE_Names() throws IOException {

        long jimageHandle = ImageNativeSubstrate.JIMAGE_Open(imageFile);
        Assert.assertTrue(jimageHandle != 0, "JIMAGE_Open failed: id: " + jimageHandle);

        String[] names = new String[50000];
        int max = ImageNativeSubstrate.JIMAGE_Resources(jimageHandle, names);

        if (max > names.length) {
            // Not all names fit, repeat with correct size
            names = new String[max];
            max = ImageNativeSubstrate.JIMAGE_Resources(jimageHandle, names);
        } else {
            names = Arrays.copyOf(names, max);
        }

        ImageNativeSubstrate.JIMAGE_Close(jimageHandle);
        return names;
    }

    // Write an array of names to a file for debugging
    static void writeNames(String fname, String[] names) throws IOException {
        try (FileWriter wr = new FileWriter(new File(fname))) {
            for (String s : names) {
                wr.write(s);
                wr.write("\n");
            }

        }
        System.out.printf(" %s: %d names%n", fname, names.length);
    }

    @Test
    static void test5_nameTooLong() throws IOException {
        long[] size = new long[1];
        String moduleName = "FictiousModuleName";
        String className = String.format("A%09999d", 1);

        long jimageHandle = ImageNativeSubstrate.JIMAGE_Open(imageFile);
        Assert.assertTrue(jimageHandle != 0, "JIMAGE_Open failed: id: " + jimageHandle);

        long locationHandle =
                ImageNativeSubstrate.JIMAGE_FindResource(jimageHandle,
                        moduleName, "9.0", className, size);

        Assert.assertEquals(0, locationHandle, "Too long name should have failed");

        ImageNativeSubstrate.JIMAGE_Close(jimageHandle);
    }

    // main method to run standalone from jtreg

    @Test(enabled=false)
    @Parameters({"x"})
    @SuppressWarnings("raw_types")
    public static void main(@Optional String[] args) {
        Class<?>[] testclass = { JImageReadTest.class};
        TestNG testng = new TestNG();
        testng.setTestClasses(testclass);
        testng.run();
    }

}
