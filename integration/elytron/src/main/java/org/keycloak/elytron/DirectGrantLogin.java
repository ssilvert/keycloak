package org.keycloak.elytron;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.keycloak.OAuth2Constants;
import org.keycloak.constants.ServiceUrlConstants;
import org.keycloak.adapters.HttpClientBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.util.HostUtils;
import org.keycloak.util.JsonSerialization;
import org.keycloak.util.KeycloakUriBuilder;
import org.keycloak.util.UriUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.keycloak.util.BasicAuthHelper;
import org.wildfly.security.auth.provider.RealmUnavailableException;

/**
 * @author Stan Silvert ssilvert@redhat.com (C) 2015 Red Hat Inc.
 */
public class DirectGrantLogin {

    static class TypedList extends ArrayList<RoleRepresentation> {
    }

    public static class Failure extends Exception {
        private final int status;

        public Failure(int status) {
            this.status = status;
        }

        public int getStatus() {
            return status;
        }
    }

    private static String getContent(HttpEntity entity) throws IOException {
        if (entity == null) return null;
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

    /**
     * Login to Keycloak using direct access grant.
     *
     * @param config Configuration to use for the REST client.
     * @param username Username
     * @param password Password
     * @return An access token if login is successful.  <code>null</code> If login is not successful.
     * @throws RealmUnavailableException If any error occurs.
     */
    public static AccessTokenResponse login(KeycloakClientConfig config, String username, char[] password) throws RealmUnavailableException {

        HttpClient client = new HttpClientBuilder()
                .disableTrustManager().build();

        try {
            HttpPost post = new HttpPost(KeycloakUriBuilder.fromUri(config.getAuthServerUri())
                    .path(ServiceUrlConstants.TOKEN_PATH).build(config.getRealmName()));
            List <NameValuePair> formparams = new ArrayList <NameValuePair>();
            formparams.add(new BasicNameValuePair("username", username));
            formparams.add(new BasicNameValuePair("password", new String(password)));
            //formparams.add(new BasicNameValuePair(OAuth2Constants.CLIENT_ID, "elytron-client"));
            String authorization = BasicAuthHelper.createHeader(config.getClientName(), config.getClientSecret());
            post.setHeader("Authorization", authorization);
            formparams.add(new BasicNameValuePair(OAuth2Constants.GRANT_TYPE, "password"));
            UrlEncodedFormEntity form = new UrlEncodedFormEntity(formparams, "UTF-8");
            post.setEntity(form);

            HttpResponse response = client.execute(post);
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();

            if (status == 401) {
                return null;  // unauthorized
            }

            if (status != 200) {
                String json = getContent(entity);
                throw new RealmUnavailableException("Bad status: " + status + " response: " + json);
            }
            if (entity == null) {
                throw new RealmUnavailableException("No Entity in login response");
            }
            String json = getContent(entity);
            return JsonSerialization.readValue(json, AccessTokenResponse.class);
        } catch (IOException e) {
            throw new RealmUnavailableException(e);
        } finally {
            client.getConnectionManager().shutdown();
        }
    }

    public static void logout(KeycloakClientConfig config, AccessTokenResponse res) throws RealmUnavailableException {
        HttpClient client = new HttpClientBuilder()
                .disableTrustManager().build();
        try {
            URI logoutUri = KeycloakUriBuilder.fromUri(config.getAuthServerUri())
                    .path(ServiceUrlConstants.TOKEN_SERVICE_LOGOUT_PATH)
                    .build(config.getRealmName());
            HttpPost post = new HttpPost(logoutUri);

            String authorization = BasicAuthHelper.createHeader(config.getClientName(), config.getClientSecret());
            post.setHeader("Authorization", authorization);

            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair(OAuth2Constants.REFRESH_TOKEN, res.getRefreshToken()));

            UrlEncodedFormEntity form = new UrlEncodedFormEntity(formparams, "UTF-8");
            post.setEntity(form);

            HttpResponse response = client.execute(post);
            boolean status = response.getStatusLine().getStatusCode() != 204;
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return;
            }
            InputStream is = entity.getContent();
            String content = getContent(entity);
            if (is != null) {
                is.close();
            }
            if (status) {
                int statusCode = response.getStatusLine().getStatusCode();
                String reason = response.getStatusLine().getReasonPhrase();
                throw new RealmUnavailableException("Logout failed: " + statusCode + ": " + reason + " " + content);
            }
        } catch (IOException e) {
            throw new RealmUnavailableException(e);
        } finally {
            client.getConnectionManager().shutdown();
        }
    }

}
