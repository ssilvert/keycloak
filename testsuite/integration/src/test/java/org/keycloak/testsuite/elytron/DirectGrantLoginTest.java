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
import org.junit.ClassRule;
import org.junit.Test;
import org.keycloak.elytron.DirectGrantLogin;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.testsuite.rule.KeycloakRule;
import org.wildfly.security.auth.provider.RealmUnavailableException;

/**
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2015 Red Hat Inc.
 */
public class DirectGrantLoginTest extends AbstractElytronTest {
    @ClassRule
    public static KeycloakRule kc = new SetupElytronClientRule();

    @Test
    public void testLogin() throws RealmUnavailableException {
        AccessTokenResponse response = DirectGrantLogin.login(keycloakRealm.getConfig(), "test-user@localhost", "password".toCharArray());
        Assert.assertNotNull(response);
    }

    @Test
    public void testFailedLogin() throws RealmUnavailableException {
        AccessTokenResponse response = DirectGrantLogin.login(keycloakRealm.getConfig(), "test-user@localhost", "bad-password".toCharArray());
        Assert.assertNull(response);
    }

    @Test
    public void testLogout() throws RealmUnavailableException {
        AccessTokenResponse response = DirectGrantLogin.login(keycloakRealm.getConfig(), "test-user@localhost", "password".toCharArray());
        Assert.assertNotNull(response);
        DirectGrantLogin.logout(keycloakRealm.getConfig(), response);
    }

}
