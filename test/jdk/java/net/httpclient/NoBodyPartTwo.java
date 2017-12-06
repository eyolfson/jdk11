/*
 * Copyright (c) 2015, 2017, Oracle and/or its affiliates. All rights reserved.
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
 * @bug 8161157
 * @summary Test response body handlers/subscribers when there is no body
 * @library /lib/testlibrary http2/server
 * @build jdk.testlibrary.SimpleSSLContext
 * @modules java.base/sun.net.www.http
 *          jdk.incubator.httpclient/jdk.incubator.http.internal.common
 *          jdk.incubator.httpclient/jdk.incubator.http.internal.frame
 *          jdk.incubator.httpclient/jdk.incubator.http.internal.hpack
 * @run testng/othervm -Djdk.internal.httpclient.debug=true -Djdk.httpclient.HttpClient.log=all NoBodyPartTwo
 */

import java.io.InputStream;
import java.net.URI;
import java.util.Optional;
import java.util.function.Consumer;
import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpResponse;
import org.testng.annotations.Test;
import static jdk.incubator.http.HttpRequest.BodyPublisher.fromString;
import static jdk.incubator.http.HttpResponse.BodyHandler.asByteArray;
import static jdk.incubator.http.HttpResponse.BodyHandler.asByteArrayConsumer;
import static jdk.incubator.http.HttpResponse.BodyHandler.asInputStream;
import static jdk.incubator.http.HttpResponse.BodyHandler.buffering;
import static jdk.incubator.http.HttpResponse.BodyHandler.discard;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class NoBodyPartTwo extends AbstractNoBody {

    volatile boolean consumerHasBeenCalled;
    @Test(dataProvider = "variants")
    public void testAsByteArrayConsumer(String uri, boolean sameClient) throws Exception {
        printStamp(START, "testAsByteArrayConsumer(\"%s\", %s)", uri, sameClient);
        HttpClient client = null;
        for (int i=0; i< ITERATION_COUNT; i++) {
            if (!sameClient || client == null)
                client = newHttpClient();

            HttpRequest req = HttpRequest.newBuilder(URI.create(uri))
                    .PUT(fromString(SIMPLE_STRING))
                    .build();
            Consumer<Optional<byte[]>>  consumer = oba -> {
                consumerHasBeenCalled = true;
                oba.ifPresent(ba -> fail("Unexpected non-empty optional:" + ba));
            };
            consumerHasBeenCalled = false;
            client.send(req, asByteArrayConsumer(consumer));
            assertTrue(consumerHasBeenCalled);
        }
        // We have created many clients here. Try to speed up their release.
        if (!sameClient) System.gc();
    }

    @Test(dataProvider = "variants")
    public void testAsInputStream(String uri, boolean sameClient) throws Exception {
        printStamp(START, "testAsInputStream(\"%s\", %s)", uri, sameClient);
        HttpClient client = null;
        for (int i=0; i< ITERATION_COUNT; i++) {
            if (!sameClient || client == null)
                client = newHttpClient();

            HttpRequest req = HttpRequest.newBuilder(URI.create(uri))
                    .PUT(fromString(SIMPLE_STRING))
                    .build();
            HttpResponse<InputStream> response = client.send(req, asInputStream());
            byte[] body = response.body().readAllBytes();
            assertEquals(body.length, 0);
        }
        // We have created many clients here. Try to speed up their release.
        if (!sameClient) System.gc();
    }

    @Test(dataProvider = "variants")
    public void testBuffering(String uri, boolean sameClient) throws Exception {
        printStamp(START, "testBuffering(\"%s\", %s)", uri, sameClient);
        HttpClient client = null;
        for (int i=0; i< ITERATION_COUNT; i++) {
            if (!sameClient || client == null)
                client = newHttpClient();

            HttpRequest req = HttpRequest.newBuilder(URI.create(uri))
                    .PUT(fromString(SIMPLE_STRING))
                    .build();
            HttpResponse<byte[]> response = client.send(req, buffering(asByteArray(), 1024));
            byte[] body = response.body();
            assertEquals(body.length, 0);
        }
        // We have created many clients here. Try to speed up their release.
        if (!sameClient) System.gc();
    }

    @Test(dataProvider = "variants")
    public void testDiscard(String uri, boolean sameClient) throws Exception {
        printStamp(START, "testDiscard(\"%s\", %s)", uri, sameClient);
        HttpClient client = null;
        for (int i=0; i< ITERATION_COUNT; i++) {
            if (!sameClient || client == null)
                client = newHttpClient();

            HttpRequest req = HttpRequest.newBuilder(URI.create(uri))
                    .PUT(fromString(SIMPLE_STRING))
                    .build();
            Object obj = new Object();
            HttpResponse<Object> response = client.send(req, discard(obj));
            assertEquals(response.body(), obj);
        }
        // We have created many clients here. Try to speed up their release.
        if (!sameClient) System.gc();
    }
}
