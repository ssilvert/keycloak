/*
 * Copyright 2015 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.keycloak.subsystem.cli.extension;

import java.net.URI;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.jboss.dmr.ModelNode;
import org.keycloak.representations.AccessTokenResponse;

/**
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2015 Red Hat Inc.
 */
public enum HttpMethod {
    GET("GET", new GetRequestBuilder()),
    PUT("PUT", new PutRequestBuilder()),
    POST("POST", new PostRequestBuilder()),
    DELETE("DELETE", new DeleteRequestBuilder()),
    ;

    private final String localName;
    private final RequestBuilder requestBuilder;

    HttpMethod(final String localName, RequestBuilder requestBuilder) {
        this.localName = localName;
        this.requestBuilder = requestBuilder;
    }

    public HttpRequestBase makeRequest(AccessTokenResponse res, URI uri, ModelNode params) {
        return requestBuilder.makeRequest(res, uri, params);
    }

    @Override
    public String toString() {
        return localName;
    }


    private abstract static class RequestBuilder<T extends HttpRequestBase> {
        abstract T newRequest();

        T makeRequest(AccessTokenResponse res, URI uri, ModelNode params) {
            T request = newRequest();
            request.setURI(uri);
            request.addHeader("Authorization", "Bearer " + res.getToken());
            return request;
        }
    }

    private static class GetRequestBuilder extends RequestBuilder {
        @Override
        HttpGet newRequest() {
            return new HttpGet();
        }
    }

    private static class PutRequestBuilder extends RequestBuilder {
        @Override
        HttpPut newRequest() {
            return new HttpPut();
        }
    }

    private static class PostRequestBuilder extends RequestBuilder {
        @Override
        HttpPost newRequest() {
            return new HttpPost();
        }
    }

    private static class DeleteRequestBuilder extends RequestBuilder {
        @Override
        HttpDelete newRequest() {
            return new HttpDelete();
        }
    }
}
