/*
 * Copyright 2005-2006 Sun Microsystems, Inc.  All Rights Reserved.
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
package com.sun.xml.internal.ws.api.message.stream;

import com.sun.xml.internal.ws.api.message.AttachmentSet;
import com.sun.xml.internal.ws.api.message.Packet;
import java.io.InputStream;

/**
 * Low level representation of an XML or SOAP message as an {@link InputStream}.
 *
 */
public class InputStreamMessage extends StreamBasedMessage {
    /**
     * The MIME content-type of the encoding.
     */
    public final String contentType;

    /**
     * The message represented as an {@link InputStream}.
     */
    public final InputStream msg;

    /**
     * Create a new message.
     *
     * @param properties
     *      the properties of the message.
     *
     * @param contentType
     *      the MIME content-type of the encoding.
     *
     * @param msg
     *      always a non-null unconsumed {@link InputStream} that
     *      represents a request.
     */
    public InputStreamMessage(Packet properties, String contentType, InputStream msg) {
        super(properties);

        this.contentType = contentType;
        this.msg = msg;
    }

    /**
     * Create a new message.
     *
     * @param properties
     *      the properties of the message.
     *
     * @param attachments
     *      the attachments of the message.
     *
     * @param contentType
     *      the MIME content-type of the encoding.
     *
     * @param msg
     *      always a non-null unconsumed {@link InputStream} that
     *      represents a request.
     */
    public InputStreamMessage(Packet properties, AttachmentSet attachments,
            String contentType, InputStream msg) {
        super(properties, attachments);

        this.contentType = contentType;
        this.msg = msg;
    }
}
