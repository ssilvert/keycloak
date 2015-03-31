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

import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.keycloak.testsuite.rule.KeycloakRule;
import org.wildfly.security.auth.provider.CredentialSupport;
import org.wildfly.security.auth.provider.RealmIdentity;
import org.wildfly.security.auth.provider.RealmUnavailableException;
import org.wildfly.security.password.interfaces.ClearPassword;

/**
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2015 Red Hat Inc.
 */
public class VerifyCredentialElytronTest extends AbstractElytronTest {

    private static final Class CREDENTIAL_TYPE = ClearPassword.class;

    @ClassRule
    public static KeycloakRule kc = new SetupElytronClientRule();

    private RealmIdentity realmIdentity;

    @Before
    public void createRealmIdentity() throws RealmUnavailableException {
        realmIdentity = keycloakRealm.createRealmIdentity("test-user@localhost");
    }

    @Test
    public void testRealmLevelSupport() throws RealmUnavailableException {
        CredentialSupport support = keycloakRealm.getCredentialSupport(CREDENTIAL_TYPE);
        assertEquals("Realm level support", CredentialSupport.VERIFIABLE_ONLY, support);
    }

    @Test
    public void testIdentityLevelSupport() throws RealmUnavailableException {
        CredentialSupport credentialSupport = realmIdentity.getCredentialSupport(CREDENTIAL_TYPE);
        assertEquals("Identity level support", CredentialSupport.VERIFIABLE_ONLY, credentialSupport);
    }

    @Test
    public void testGetCredential() throws RealmUnavailableException {
        // Always null for now.  Might change for local keycloak.
        Assert.assertNull(realmIdentity.getCredential(CREDENTIAL_TYPE));
    }

    @Test
    public void testValidCredential() throws RealmUnavailableException {
        Assert.assertTrue(realmIdentity.verifyCredential(generatePassword("password")));
    }

    @Test
    public void testInvalidCredential() throws Exception {
        Assert.assertFalse(realmIdentity.verifyCredential(generatePassword("passwordd")));
    }

}
