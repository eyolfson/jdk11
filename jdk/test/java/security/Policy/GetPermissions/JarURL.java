/*
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
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
 * @bug 7044443
 * @summary Permissions resolved incorrectly for jar protocol
 */

import java.net.URL;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.cert.Certificate;

public class JarURL {
    public static void main(String[] args) throws Exception {
        URL codeSourceURL
            = new URL("jar:file:"
                      + System.getProperty("java.ext.dirs").split(":")[0]
                      + "/foo.jar!/");
        CodeSource cs = new CodeSource(codeSourceURL, new Certificate[0]);
        PermissionCollection perms = Policy.getPolicy().getPermissions(cs);
        if (!perms.implies(new AllPermission()))
            throw new Exception("FAILED: " + codeSourceURL
                                + " not granted AllPermission");
    }
}
