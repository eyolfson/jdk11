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
 * @bug 8075526
 * @key intermittent
 * @summary Test timestamp via ZipEntry.get/setTimeLocal()
 */

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.zip.*;

public class TestLocalTime {
    private static TimeZone tz0 = TimeZone.getDefault();

    public static void main(String[] args) throws Throwable{
        try {
            LocalDateTime ldt = LocalDateTime.now();
            test(getBytes(ldt), ldt);    // now
            ldt = ldt.withYear(1968); test(getBytes(ldt), ldt);
            ldt = ldt.withYear(1970); test(getBytes(ldt), ldt);
            ldt = ldt.withYear(1982); test(getBytes(ldt), ldt);
            ldt = ldt.withYear(2037); test(getBytes(ldt), ldt);
            ldt = ldt.withYear(2100); test(getBytes(ldt), ldt);
            ldt = ldt.withYear(2106); test(getBytes(ldt), ldt);

            TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
            // dos time does not support < 1980, have to use
            // utc in mtime.
            testWithTZ(tz, ldt.withYear(1982));
            testWithTZ(tz, ldt.withYear(2037));
            testWithTZ(tz, ldt.withYear(2100));
            testWithTZ(tz, ldt.withYear(2106));
        } finally {
            TimeZone.setDefault(tz0);
        }
    }

    static byte[] getBytes(LocalDateTime mtime) throws Throwable {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        ZipEntry ze = new ZipEntry("TestLocalTime.java");
        ze.setTimeLocal(mtime);
        check(ze, mtime);

        zos.putNextEntry(ze);
        zos.write(new byte[] { 1, 2, 3, 4});
        zos.close();
        return baos.toByteArray();
    }

    static void testWithTZ(TimeZone tz, LocalDateTime ldt) throws Throwable {
       TimeZone.setDefault(tz);
       byte[] zbytes = getBytes(ldt);
       TimeZone.setDefault(tz0);
       test(zbytes, ldt);
    }

    static void test(byte[] zbytes, LocalDateTime expected) throws Throwable {
        System.out.printf("--------------------%nTesting: [%s]%n", expected);
        // ZipInputStream
        ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zbytes));
        ZipEntry ze = zis.getNextEntry();
        zis.close();
        check(ze, expected);

        // ZipFile
        Path zpath = Paths.get(System.getProperty("test.dir", "."),
                               "TestLocalTime.zip");
        try {
            Files.copy(new ByteArrayInputStream(zbytes), zpath);
            ZipFile zf = new ZipFile(zpath.toFile());
            ze = zf.getEntry("TestLocalTime.java");
            check(ze, expected);
            zf.close();
        } finally {
            Files.deleteIfExists(zpath);
        }
    }

    static void check(ZipEntry ze, LocalDateTime expected) {
        LocalDateTime ldt = ze.getTimeLocal();
        if (ldt.atOffset(ZoneOffset.UTC).toEpochSecond() >> 1
            != expected.atOffset(ZoneOffset.UTC).toEpochSecond() >> 1) {
            throw new RuntimeException("Timestamp: storing mtime failed!");
        }
    }
}
