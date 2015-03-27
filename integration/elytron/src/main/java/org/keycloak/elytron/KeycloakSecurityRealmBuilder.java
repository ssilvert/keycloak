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
package org.keycloak.elytron;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.wildfly.security.auth.provider.CredentialSupport;

/**
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2015 Red Hat Inc.
 */
public class KeycloakSecurityRealmBuilder {
    public static final String DEFAULT_CLIENT_NAME = "elytron-client";

    private boolean built = false;
    private final Map<Class<?>, CredentialSupport> credentialSupportMap = new HashMap<Class<?>, CredentialSupport>();
    private final KeycloakClientConfig clientConfig = new KeycloakClientConfig();

    private KeycloakSecurityRealmBuilder() {
    }

    public static KeycloakSecurityRealmBuilder builder() {
        return new KeycloakSecurityRealmBuilder();
    }

    public KeycloakSecurityRealmBuilder setRealmName(final String realmName) {
        assertNotBuilt();
        clientConfig.setRealmName(realmName);

        return this;
    }

    public KeycloakSecurityRealmBuilder setAuthServerUri(URI uri) {
        assertNotBuilt();

        clientConfig.setAuthServerUri(uri);

        return this;
    }

    public KeycloakSecurityRealmBuilder setElytronClientName(String clientName) {
        assertNotBuilt();

        clientConfig.setClientName(clientName);

        return this;
    }

    public KeycloakSecurityRealmBuilder setElytronClientSecret(String clientSecret) {
        assertNotBuilt();

        clientConfig.setClientSecret(clientSecret);

        return this;
    }

    public KeycloakSecurityRealmBuilder addCredentialSupport(final Class<?> credentialType, final CredentialSupport support) {
        assertNotBuilt();
        credentialSupportMap.put(credentialType, support);

        return this;
    }

    public KeycloakSecurityRealm build() {
        assertNotBuilt();

        if (this.clientConfig.getRealmName() == null) throw new NullPointerException("realmName");
        if (this.clientConfig.getClientName() == null) this.clientConfig.setClientName(DEFAULT_CLIENT_NAME);
        if (this.clientConfig.getClientSecret() == null) throw new NullPointerException("elytronClientSecret");
        if (this.clientConfig.getAuthServerUri() == null) throw new NullPointerException("authServerUri");
        if (this.credentialSupportMap.isEmpty()) throw new IllegalStateException("No supported credential types");

        KeycloakSecurityRealm realm = new KeycloakSecurityRealm(clientConfig, credentialSupportMap);

        built = true;
        return realm;
    }

    private void assertNotBuilt() {
        if (built) {
            throw new IllegalStateException("Builder has already been built.");
        }
    }
}
