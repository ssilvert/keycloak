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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.keycloak.OAuth2Constants;
import org.keycloak.adapters.HttpClientBuilder;
import org.keycloak.constants.ServiceUrlConstants;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.util.JsonSerialization;
import org.keycloak.util.KeycloakUriBuilder;

/**
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2015 Red Hat Inc.
 */
public class KeycloakREST {

    public static class Failure extends Exception {

        private int status;

        public Failure(int status) {
            this.status = status;
        }

        public int getStatus() {
            return status;
        }
    }

    public static String getContent(HttpEntity entity) throws IOException {
        if (entity == null) {
            return null;
        }
        InputStream is = entity.getContent();
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            int c;
            while ((c = is.read()) != -1) {
                os.write(c);
            }
            byte[] bytes = os.toByteArray();
            String data = new String(bytes);
            return data;
        } finally {
            try {
                is.close();
            } catch (IOException ignored) {

            }
        }
    }

    public static AccessTokenResponse getToken() throws IOException {
        HttpClient client = new HttpClientBuilder()
                .disableTrustManager().build();

        try {
            HttpPost post = new HttpPost(KeycloakUriBuilder.fromUri(getBaseUrl())
                    .path(ServiceUrlConstants.TOKEN_PATH).build("demo"));
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("username", "admin"));
            formparams.add(new BasicNameValuePair("password", "password"));
            formparams.add(new BasicNameValuePair(OAuth2Constants.GRANT_TYPE, "password"));
            formparams.add(new BasicNameValuePair(OAuth2Constants.CLIENT_ID, "admin-client"));
            UrlEncodedFormEntity form = new UrlEncodedFormEntity(formparams, "UTF-8");
            post.setEntity(form);

            HttpResponse response = client.execute(post);
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            if (status != 200) {
                String json = getContent(entity);
                throw new IOException("Bad status: " + status + " response: " + json);
            }
            if (entity == null) {
                throw new IOException("No Entity");
            }
            String json = getContent(entity);
            return JsonSerialization.readValue(json, AccessTokenResponse.class);
        } finally {
            client.getConnectionManager().shutdown();
        }
    }

    public static void logout(AccessTokenResponse res) throws IOException {

        HttpClient client = new HttpClientBuilder()
                .disableTrustManager().build();

        try {
            HttpPost post = new HttpPost(KeycloakUriBuilder.fromUri(getBaseUrl())
                    .path(ServiceUrlConstants.TOKEN_SERVICE_LOGOUT_PATH)
                    .build("demo"));
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair(OAuth2Constants.REFRESH_TOKEN, res.getRefreshToken()));
            formparams.add(new BasicNameValuePair(OAuth2Constants.CLIENT_ID, "admin-client"));
            UrlEncodedFormEntity form = new UrlEncodedFormEntity(formparams, "UTF-8");
            post.setEntity(form);
            HttpResponse response = client.execute(post);
            boolean status = response.getStatusLine().getStatusCode() != 204;
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return;
            }
            InputStream is = entity.getContent();
            if (is != null) {
                is.close();
            }
            if (status) {
                throw new RuntimeException("failed to logout");
            }
        } finally {
            client.getConnectionManager().shutdown();
        }
    }

    public static String submit(AccessTokenResponse res, HttpMethod method, String path) throws Failure {

        HttpClient client = new HttpClientBuilder()
                .disableTrustManager().build();
        try {
            //HttpGet get = new HttpGet();
            //get.setURI(new URI(getRealmsBaseUrl() + path));
            //get.addHeader("Authorization", "Bearer " + res.getToken());
            URI uri = new URI(getRealmsBaseUrl() + path);
            HttpResponse response = client.execute(method.makeRequest(res, uri, null));
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new Failure(response.getStatusLine().getStatusCode());
            }
            HttpEntity entity = response.getEntity();
            return getContent(entity);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        } finally {
            client.getConnectionManager().shutdown();
        }
        }



    public static String getBaseUrl() {
        return "http://localhost:8080/auth/";
    }

    public static String getRealmsBaseUrl() {
        return getBaseUrl() + "admin/realms";
    }
}
