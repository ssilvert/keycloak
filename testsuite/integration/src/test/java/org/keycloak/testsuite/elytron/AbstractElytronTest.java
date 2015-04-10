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
import java.security.Provider;
import java.security.Security;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.keycloak.elytron.KeycloakSecurityRealm;
import org.keycloak.elytron.KeycloakSecurityRealmBuilder;
import org.wildfly.security.WildFlyElytronProvider;
import org.wildfly.security.auth.provider.CredentialSupport;
import org.wildfly.security.password.Password;
import org.wildfly.security.password.PasswordFactory;
import org.wildfly.security.password.interfaces.ClearPassword;
import static org.wildfly.security.password.interfaces.ClearPassword.ALGORITHM_CLEAR;
import org.wildfly.security.password.spec.ClearPasswordSpec;
import org.wildfly.security.password.spec.PasswordSpec;

/**
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2015 Red Hat Inc.
 */
public abstract class AbstractElytronTest {

    protected static KeycloakSecurityRealm keycloakRealm;
    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private static final Provider provider = new WildFlyElytronProvider();

    @BeforeClass
    public static void createRealm() throws URISyntaxException {
        keycloakRealm = KeycloakSecurityRealmBuilder.builder()
                .setRealmName("test")
                .setAuthServerUri(new URI("http://localhost:8081/auth"))
                //.setElytronClientName("elytron-client") <-- this is the default value
                .setElytronClientSecret("elytron")
                .addCredentialSupport(ClearPassword.class, CredentialSupport.VERIFIABLE_ONLY)
                .addCredentialSupport(Password.class, CredentialSupport.VERIFIABLE_ONLY)
                .build();
    }

    @BeforeClass
    public static void register() {
        Security.addProvider(provider);
    }

    @AfterClass
    public static void remove() {
        Security.removeProvider(provider.getName());
    }

    protected static Password generatePassword(String strPassword) {
        try {
            PasswordFactory factory = PasswordFactory.getInstance(ALGORITHM_CLEAR);
            return factory.generatePassword(createClearPasswordSpec(strPassword.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            return null;
        }
    }

    private static PasswordSpec createClearPasswordSpec(byte[] userPassword) {
        return new ClearPasswordSpec(new String(userPassword, UTF_8).toCharArray());
    }
}
