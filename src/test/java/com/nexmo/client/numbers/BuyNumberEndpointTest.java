/*
 * Copyright (c) 2011-2017 Nexmo Inc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.nexmo.client.numbers;

import com.nexmo.client.HttpConfig;
import com.nexmo.client.HttpWrapper;
import com.nexmo.client.NexmoBadRequestException;
import com.nexmo.client.TestUtils;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;


public class BuyNumberEndpointTest {
    private BuyNumberEndpoint endpoint;

    @Before
    public void setUp() throws Exception {
        this.endpoint = new BuyNumberEndpoint(new HttpWrapper());
    }

    @Test
    public void makeRequest() throws Exception {
        RequestBuilder request = endpoint.makeRequest(new BuyNumberRequest("AA", "447700900000"));

        assertEquals("POST", request.getMethod());
        Map<String, String> params = TestUtils.makeParameterMap(request.getParameters());
        assertEquals("AA", params.get("country"));
        assertEquals("447700900000", params.get("msisdn"));
    }

    @Test
    public void parseResponse() throws Exception {
        HttpResponse stubResponse = new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("1.1", 1, 1),
                200,
                "OK"
        ));

        String json = "{\n" + "  \"error-code\":\"200\",\n" + "  \"error-code-label\":\"success\"\n" + "}";
        InputStream jsonStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(jsonStream);
        stubResponse.setEntity(entity);

        endpoint.parseResponse(stubResponse);
    }

    @Test
    public void parseBadRequestResponse() throws Exception {
        HttpResponse stubResponse = new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("1.1", 1, 1),
                400,
                "OK"
        ));

        String json = "{\n" + "    \"error_title\": \"Bad Request\",\n" + "    \"invalid_parameters\": {\n"
                + "        \"country\": \"Is required.\"\n" + "    },\n" + "    \"type\": \"BAD_REQUEST\"\n" + "}";
        InputStream jsonStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(jsonStream);
        stubResponse.setEntity(entity);

        try {
            endpoint.parseResponse(stubResponse);
            fail("A 400 response should raise a NexmoBadRequestException");
        } catch (NexmoBadRequestException e) {
            // This is expected
        }
    }

    @Test
    public void testDefaultUri() throws Exception {
        BuyNumberRequest request = new BuyNumberRequest("AA", "447700900000");

        RequestBuilder builder = endpoint.makeRequest(request);
        assertEquals("POST", builder.getMethod());
        assertEquals("https://rest.nexmo.com/number/buy",
                builder.build().getURI().toString()
        );
    }

    @Test
    public void testCustomUri() throws Exception {
        HttpWrapper wrapper = new HttpWrapper(new HttpConfig.Builder().baseUri("https://example.com").build());
        BuyNumberEndpoint endpoint = new BuyNumberEndpoint(wrapper);
        BuyNumberRequest request = new BuyNumberRequest("AA", "447700900000");

        RequestBuilder builder = endpoint.makeRequest(request);
        assertEquals("POST", builder.getMethod());
        assertEquals("https://example.com/number/buy", builder.build().getURI().toString());
    }
}
