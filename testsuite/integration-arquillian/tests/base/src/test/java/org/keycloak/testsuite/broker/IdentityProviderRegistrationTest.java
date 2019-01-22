/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.testsuite.broker;

import java.util.List;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.keycloak.broker.provider.IdentityProvider;
import org.keycloak.broker.provider.IdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.testsuite.AbstractKeycloakTest;
import org.keycloak.testsuite.Assert;
import org.keycloak.testsuite.arquillian.annotation.ModelTest;
import org.keycloak.testsuite.broker.provider.CustomIdentityProvider;
import org.keycloak.testsuite.broker.provider.CustomIdentityProviderFactory;
import org.keycloak.testsuite.broker.provider.social.CustomSocialProvider;
import org.keycloak.testsuite.broker.provider.social.CustomSocialProviderFactory;


/**
 * Migrated from old testsuite.  Previous version by Pedro Igor.
 * 
 * @author Stan Silvert ssilvert@redhat.com (C) 2019 Red Hat Inc.
 * @author pedroigor
 */
public class IdentityProviderRegistrationTest extends AbstractKeycloakTest {

    @Override
    public void addTestRealms(List<RealmRepresentation> testRealms) {
    }
    
    @Test
    @ModelTest
    public void testIdentityProviderRegistration(KeycloakSession session) {
        Set<String> installedProviders = getInstalledProviders(session);

        for (String providerId : ExpectedProviders.get()) {
            if (!installedProviders.contains(providerId)) {
                Assert.fail("Provider [" + providerId + "] not installed ");
            }
        }
    }
    
    @Test
    @ModelTest
    public void testCustomSocialProviderRegistration(KeycloakSession session) {
        String providerId = CustomSocialProviderFactory.PROVIDER_ID;

        Assert.assertTrue(getInstalledProviders(session).contains(providerId));

        SocialIdentityProviderFactory<CustomSocialProvider> providerFactory = (SocialIdentityProviderFactory) session.getKeycloakSessionFactory().getProviderFactory(SocialIdentityProvider.class, providerId);

        assertNotNull(providerFactory);

        IdentityProviderModel identityProviderModel = new IdentityProviderModel();

        identityProviderModel.setAlias("custom-provider");

        CustomSocialProvider customSocialProvider = providerFactory.create(session, identityProviderModel);

        assertNotNull(customSocialProvider);
        IdentityProviderModel config = customSocialProvider.getConfig();

        assertNotNull(config);
        assertEquals("custom-provider", config.getAlias());
    }
    
    @Test
    @ModelTest
    public void testCustomIdentityProviderRegistration(KeycloakSession session) {
        String providerId = CustomIdentityProviderFactory.PROVIDER_ID;

        Assert.assertTrue(getInstalledProviders(session).contains(providerId));

        IdentityProviderFactory<CustomIdentityProvider> providerFactory = (IdentityProviderFactory) session.getKeycloakSessionFactory().getProviderFactory(IdentityProvider.class, providerId);

        assertNotNull(providerFactory);

        IdentityProviderModel identityProviderModel = new IdentityProviderModel();

        identityProviderModel.setAlias("custom-provider");

        CustomIdentityProvider provider = providerFactory.create(session, identityProviderModel);

        assertNotNull(provider);
        IdentityProviderModel config = provider.getConfig();

        assertNotNull(config);
        assertEquals("custom-provider", config.getAlias());
    }
    
    private Set<String> getInstalledProviders(KeycloakSession session) {
        Set<String> installedProviders = session.listProviderIds(IdentityProvider.class);

        installedProviders.addAll(session.listProviderIds(SocialIdentityProvider.class));

        return installedProviders;
    }
        
}
