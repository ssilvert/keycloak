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

import java.security.Principal;
import javax.security.auth.x500.X500Principal;
import org.junit.Assert;
import org.junit.Test;
import org.wildfly.security.auth.provider.CredentialSupport;
import org.wildfly.security.auth.provider.RealmIdentity;
import org.wildfly.security.auth.provider.RealmUnavailableException;
import org.wildfly.security.password.Password;

/**
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2015 Red Hat Inc.
 */
public class ServerlessElytronTest extends AbstractElytronTest {

    @Test
    public void testCreateRealmIdentity() throws RealmUnavailableException {
        RealmIdentity realmId = keycloakRealm.createRealmIdentity("stan");
        Assert.assertNotNull(realmId);
        Principal principal = realmId.getPrincipal();
        Assert.assertNotNull(principal);
        Assert.assertEquals("stan", principal.getName());

        // test version that accepts a Principal instead of String
        realmId = keycloakRealm.createRealmIdentity(principal);
        Assert.assertNotNull(realmId);
        principal = realmId.getPrincipal();
        Assert.assertNotNull(principal);
        Assert.assertEquals("stan", principal.getName());
    }

    @Test(expected=NullPointerException.class)
    public void testNullPrincipal() throws RealmUnavailableException {
        Principal principal = null;
        keycloakRealm.createRealmIdentity(principal);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testWrongPrincipalType() throws RealmUnavailableException {
        Principal principal = new X500Principal("stan");
        keycloakRealm.createRealmIdentity(principal);
    }

    @Test
    public void testGetCredentialSupport() throws RealmUnavailableException {
        Assert.assertEquals(CredentialSupport.VERIFIABLE_ONLY, keycloakRealm.getCredentialSupport(Password.class));
        Assert.assertEquals(CredentialSupport.UNKNOWN, keycloakRealm.getCredentialSupport(String.class));
        Assert.assertEquals(CredentialSupport.VERIFIABLE_ONLY, keycloakRealm.createRealmIdentity("foo").getCredentialSupport(Password.class));
    }

    @Test
    public void testThrowRealmUnavailableException() throws RealmUnavailableException {
        RealmIdentity realmIdentity = keycloakRealm.createRealmIdentity("test-user@localhost"); // exception not thrown here
        try {
            realmIdentity.verifyCredential(generatePassword("doesnotmatter"));
            Assert.fail("Should have thrown RealmUnavailableException because server not running");
        } catch (RealmUnavailableException e) {
            // passed the test
        }
    }

}
