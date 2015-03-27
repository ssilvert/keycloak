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
import java.util.HashMap;
import java.util.Map;
import org.keycloak.representations.AccessTokenResponse;
import org.wildfly.security.auth.provider.AuthenticatedRealmIdentity;
import org.wildfly.security.auth.provider.CredentialSupport;
import org.wildfly.security.auth.provider.RealmIdentity;
import org.wildfly.security.auth.provider.RealmUnavailableException;
import org.wildfly.security.password.Password;
import org.wildfly.security.password.interfaces.ClearPassword;

/**
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2015 Red Hat Inc.
 */
public class KeycloakRealmIdentity implements RealmIdentity {

    private final KeycloakClientConfig config;
    private final Principal principal;
    private final Map<Class<?>, CredentialSupport> credentialSupportMap;

    KeycloakRealmIdentity(KeycloakClientConfig config, Principal principal, Map<Class<?>, CredentialSupport> credentialSupportMap) {
        this.config = config;
        this.principal = principal;
        this.credentialSupportMap = credentialSupportMap;
    }

    @Override
    public Principal getPrincipal() throws RealmUnavailableException {
        return this.principal;
    }

    @Override
    public CredentialSupport getCredentialSupport(Class<?> credentialType) throws RealmUnavailableException {
        if (!credentialSupportMap.containsKey(credentialType)) {
            return CredentialSupport.UNSUPPORTED;
        }
        return credentialSupportMap.get(credentialType);
    }

    @Override
    public <C> C getCredential(Class<C> credentialType) throws RealmUnavailableException {
        return null;
    }

    @Override
    public boolean verifyCredential(Object credential) throws RealmUnavailableException {
        if (credential instanceof ClearPassword == false) {
            return false;
        }
        ClearPassword password = (ClearPassword) credential;

        AccessTokenResponse response = DirectGrantLogin.login(this.config,
                principal.getName(),
                password.getPassword());
        
        return true;
    }

    @Override
    public void dispose() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AuthenticatedRealmIdentity getAuthenticatedRealmIdentity() throws RealmUnavailableException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
