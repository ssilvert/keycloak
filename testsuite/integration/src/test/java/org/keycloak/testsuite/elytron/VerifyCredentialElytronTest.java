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
package org.keycloak.testsuite.elytron;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.keycloak.elytron.KeycloakSecurityRealm;
import org.keycloak.elytron.KeycloakSecurityRealmBuilder;
import org.keycloak.models.OAuthClientModel;
import org.keycloak.models.RealmModel;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.testsuite.rule.KeycloakRule;
import org.wildfly.security.auth.provider.CredentialSupport;
import org.wildfly.security.auth.provider.RealmIdentity;
import org.wildfly.security.auth.provider.RealmUnavailableException;
import org.wildfly.security.password.Password;
import org.wildfly.security.password.PasswordFactory;
import org.wildfly.security.password.impl.WildFlyElytronPasswordProvider;
import org.wildfly.security.password.interfaces.ClearPassword;
import static org.wildfly.security.password.interfaces.ClearPassword.ALGORITHM_CLEAR;
import org.wildfly.security.password.spec.ClearPasswordSpec;
import org.wildfly.security.password.spec.PasswordSpec;

/**
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2015 Red Hat Inc.
 */
public class VerifyCredentialElytronTest extends AbstractElytronTest {

    @ClassRule
    public static KeycloakRule kc = new KeycloakRule(new KeycloakRule.KeycloakSetup() {
        @Override                                                                         // "test"
        public void config(RealmManager manager, RealmModel adminstrationRealm, RealmModel testRealm) {
            OAuthClientModel oauthClient = testRealm.addOAuthClient("elytron-client");
            oauthClient.setEnabled(true);
            oauthClient.setPublicClient(false);
            oauthClient.setSecret("elytron");
            oauthClient.setDirectGrantsOnly(true);
        }
    });

    static final Charset UTF_8 = Charset.forName("UTF-8");

    @Test
    public void testVerifyCredential() throws Exception {
        RealmIdentity realmIdentity = keycloakRealm.createRealmIdentity("test-user@localhost");

        Class credentialType = ClearPassword.class;
        CredentialSupport support = keycloakRealm.getCredentialSupport(credentialType);
        assertEquals("Realm level support", CredentialSupport.VERIFIABLE_ONLY, support);

        verifyPasswordSupport(realmIdentity, credentialType);
        verifyPassword(realmIdentity, credentialType, "password");
    }

    @Test
    public void testVerifyInvalidCredential() throws Exception {
        //TODO
    }

    private void verifyPasswordSupport(RealmIdentity identity, Class<?> credentialType) throws RealmUnavailableException {
        CredentialSupport credentialSupport = identity.getCredentialSupport(credentialType);
        assertEquals("Identity level support", CredentialSupport.VERIFIABLE_ONLY, credentialSupport);
    }

    private void verifyPassword(RealmIdentity identity, Class<?> credentialType, String password)throws RealmUnavailableException {
        // Always null for now.  Might change for local keycloak.
        Assert.assertNull(identity.getCredential(credentialType));
        Assert.assertTrue(identity.verifyCredential(generatePassword(password)));
    }

}
