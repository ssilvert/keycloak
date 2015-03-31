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

import java.security.Principal;
import java.util.Map;
import org.wildfly.security.auth.principal.NamePrincipal;
import org.wildfly.security.auth.provider.CredentialSupport;
import org.wildfly.security.auth.provider.RealmIdentity;
import org.wildfly.security.auth.provider.RealmUnavailableException;
import org.wildfly.security.auth.provider.SecurityRealm;

/**
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2015 Red Hat Inc.
 */
public class KeycloakSecurityRealm implements SecurityRealm {

    private final KeycloakClientConfig config;
    private final Map<Class<?>, CredentialSupport> credentialSupportMap;

    KeycloakSecurityRealm(KeycloakClientConfig config, Map<Class<?>, CredentialSupport> credentialSupportMap) {
        this.config = config;
        this.credentialSupportMap = credentialSupportMap;
    }

    public KeycloakClientConfig getConfig() {
        return this.config;
    }

    @Override
    public RealmIdentity createRealmIdentity(String name) throws RealmUnavailableException {
        return createRealmIdentity(new NamePrincipal(name));
    }

    @Override
    public RealmIdentity createRealmIdentity(Principal principal) throws RealmUnavailableException {
        if (principal == null) throw new NullPointerException("Principal can not be null");
        if (principal instanceof NamePrincipal == false) {
            throw new IllegalArgumentException("Invalid Principal type");
        }
        return new KeycloakRealmIdentity(config, principal, this.credentialSupportMap);
    }

    @Override
    public CredentialSupport getCredentialSupport(Class<?> credentialType) throws RealmUnavailableException {
        if (!credentialSupportMap.containsKey(credentialType)) return CredentialSupport.UNKNOWN;
        return credentialSupportMap.get(credentialType);
    }

}
