/*
 * Copyright 2008-2009 Sun Microsystems, Inc.  All Rights Reserved.
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

package sun.nio.fs;

import static sun.nio.fs.WindowsNativeDispatcher.*;
import static sun.nio.fs.WindowsConstants.*;

/**
 * Security related utility methods.
 */

class WindowsSecurity {
    private WindowsSecurity() { }

    // opens process token for given access
    private static long openProcessToken(int access) {
        try {
            return OpenProcessToken(GetCurrentProcess(), access);
        } catch (WindowsException x) {
            return 0L;
        }
    }

    /**
     * Returns the access token for this process with TOKEN_DUPLICATE access
     */
    static final long processTokenWithDuplicateAccess =
        openProcessToken(TOKEN_DUPLICATE);

    /**
     * Returns the access token for this process with TOKEN_QUERY access
     */
    static final long processTokenWithQueryAccess =
        openProcessToken(TOKEN_QUERY);

    /**
     * Returned by enablePrivilege when code may require a given privilege.
     * The drop method should be invoked after the operation completes so as
     * to revert the privilege.
     */
    static interface Privilege {
        void drop();
    }

    /**
     * Attempts to enable the given privilege for this method.
     */
    static Privilege enablePrivilege(String priv) {
        final long pLuid;
        try {
            pLuid = LookupPrivilegeValue(priv);
        } catch (WindowsException x) {
            // indicates bug in caller
            throw new AssertionError(x);
        }

        long hToken = 0L;
        boolean impersontating = false;
        boolean elevated = false;
        try {
            hToken = OpenThreadToken(GetCurrentThread(),
                                     TOKEN_ADJUST_PRIVILEGES, false);
            if (hToken == 0L && processTokenWithDuplicateAccess != 0L) {
                hToken = DuplicateTokenEx(processTokenWithDuplicateAccess,
                    (TOKEN_ADJUST_PRIVILEGES|TOKEN_IMPERSONATE));
                SetThreadToken(0L, hToken);
                impersontating = true;
            }

            if (hToken != 0L) {
                AdjustTokenPrivileges(hToken, pLuid, SE_PRIVILEGE_ENABLED);
                elevated = true;
            }
        } catch (WindowsException x) {
            // nothing to do, privilege not enabled
        }

        final long token = hToken;
        final boolean stopImpersontating = impersontating;
        final boolean needToRevert = elevated;

        return new Privilege() {
            @Override
            public void drop() {
                try {
                    if (stopImpersontating) {
                        SetThreadToken(0L, 0L);
                    } else {
                        if (needToRevert) {
                            AdjustTokenPrivileges(token, pLuid, 0);
                        }
                    }
                } catch (WindowsException x) {
                    // should not happen
                    throw new AssertionError(x);
                }
            }
        };
    }
}
