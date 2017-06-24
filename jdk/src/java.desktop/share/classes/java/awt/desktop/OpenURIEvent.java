/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
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

package java.awt.desktop;

import java.net.URI;


/**
 * Event sent when the app is asked to open a {@code URI}.
 *
 * @see OpenURIHandler#openURI(OpenURIEvent)
 *
 * @since 9
 */
public final class OpenURIEvent extends AppEvent {
    private static final long serialVersionUID = 221209100935933476L;
    final URI uri;

    /**
     * Constructs an {@code OpenURIEvent}
     * @param uri {@code URI}
     */
    public OpenURIEvent(final URI uri) {
        this.uri = uri;
    }

    /**
     * Get the {@code URI} the app was asked to open
     * @return the {@code URI}
     */
    public URI getURI() {
        return uri;
    }

}
