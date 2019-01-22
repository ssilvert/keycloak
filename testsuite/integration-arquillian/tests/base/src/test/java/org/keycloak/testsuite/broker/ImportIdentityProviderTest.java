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

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.broker.oidc.OIDCIdentityProvider;
import org.keycloak.broker.oidc.OIDCIdentityProviderConfig;
import org.keycloak.broker.oidc.OIDCIdentityProviderFactory;
import org.keycloak.broker.saml.SAMLIdentityProvider;
import org.keycloak.broker.saml.SAMLIdentityProviderConfig;
import org.keycloak.broker.saml.SAMLIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.DefaultAuthenticationFlows;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.social.facebook.FacebookIdentityProvider;
import org.keycloak.social.facebook.FacebookIdentityProviderFactory;
import org.keycloak.social.github.GitHubIdentityProvider;
import org.keycloak.social.github.GitHubIdentityProviderFactory;
import org.keycloak.social.google.GoogleIdentityProvider;
import org.keycloak.social.google.GoogleIdentityProviderFactory;
import org.keycloak.social.linkedin.LinkedInIdentityProvider;
import org.keycloak.social.linkedin.LinkedInIdentityProviderFactory;
import org.keycloak.social.openshift.OpenshiftV3IdentityProvider;
import org.keycloak.social.openshift.OpenshiftV3IdentityProviderConfig;
import org.keycloak.social.openshift.OpenshiftV3IdentityProviderFactory;
import org.keycloak.social.paypal.PayPalIdentityProvider;
import org.keycloak.social.paypal.PayPalIdentityProviderConfig;
import org.keycloak.social.paypal.PayPalIdentityProviderFactory;
import org.keycloak.social.stackoverflow.StackOverflowIdentityProviderConfig;
import org.keycloak.social.stackoverflow.StackoverflowIdentityProvider;
import org.keycloak.social.stackoverflow.StackoverflowIdentityProviderFactory;
import org.keycloak.social.twitter.TwitterIdentityProvider;
import org.keycloak.social.twitter.TwitterIdentityProviderFactory;
import org.keycloak.testsuite.AbstractKeycloakTest;
import org.keycloak.testsuite.admin.AbstractAdminTest;
import org.keycloak.testsuite.arquillian.annotation.ModelTest;


/**
 * Migrated from old testsuite.  Previous version by Pedro Igor.
 * 
 * @author Stan Silvert ssilvert@redhat.com (C) 2019 Red Hat Inc.
 * @author pedroigor
 */
public class ImportIdentityProviderTest extends AbstractKeycloakTest {

    @Override
    public void addTestRealms(List<RealmRepresentation> testRealms) {
        RealmRepresentation realmRepresentation = AbstractAdminTest.loadJson(getClass().getResourceAsStream("/broker-test/test-realm-with-broker.json"), RealmRepresentation.class);
        testRealms.add(realmRepresentation);
    }
    
    @Test
    @ModelTest
    public void testInstallation(KeycloakSession session) throws Exception {
        RealmModel realm = session.realms().getRealm("realm-with-broker");
        assertIdentityProviderConfig(realm, realm.getIdentityProviders(), session);

        assertTrue(realm.isIdentityFederationEnabled());
    }
    
    @Test
    @ModelTest
    public void testUpdateIdentityProvider(KeycloakSession session) throws Exception {
        RealmModel realm = session.realms().getRealm("realm-with-broker");
        List<IdentityProviderModel> identityProviders = realm.getIdentityProviders();

        assertFalse(identityProviders.isEmpty());

        IdentityProviderModel identityProviderModel = identityProviders.get(0);
        String identityProviderId = identityProviderModel.getAlias();

        identityProviderModel.getConfig().put("config-added", "value-added");
        identityProviderModel.setEnabled(false);
        identityProviderModel.setTrustEmail(true);
        identityProviderModel.setStoreToken(true);
        identityProviderModel.setAuthenticateByDefault(true);
        identityProviderModel.setFirstBrokerLoginFlowId(realm.getBrowserFlow().getId());
        identityProviderModel.setPostBrokerLoginFlowId(realm.getDirectGrantFlow().getId());

        realm.updateIdentityProvider(identityProviderModel);

        realm = session.realms().getRealm(realm.getId());

        identityProviderModel = realm.getIdentityProviderByAlias(identityProviderId);

        assertEquals("value-added", identityProviderModel.getConfig().get("config-added"));
        assertFalse(identityProviderModel.isEnabled());
        assertTrue(identityProviderModel.isTrustEmail());
        assertTrue(identityProviderModel.isStoreToken());
        assertTrue(identityProviderModel.isAuthenticateByDefault());
        assertEquals(identityProviderModel.getFirstBrokerLoginFlowId(), realm.getBrowserFlow().getId());
        assertEquals(identityProviderModel.getPostBrokerLoginFlowId(), realm.getDirectGrantFlow().getId());

        identityProviderModel.getConfig().remove("config-added");
        identityProviderModel.setEnabled(true);
        identityProviderModel.setTrustEmail(false);
        identityProviderModel.setAuthenticateByDefault(false);

        realm.updateIdentityProvider(identityProviderModel);

        realm = session.realms().getRealm(realm.getId());
        identityProviderModel = realm.getIdentityProviderByAlias(identityProviderId);

        assertFalse(identityProviderModel.getConfig().containsKey("config-added"));
        assertTrue(identityProviderModel.isEnabled());
        assertFalse(identityProviderModel.isTrustEmail());
        assertFalse(identityProviderModel.isAuthenticateByDefault());
    }

    private void assertIdentityProviderConfig(RealmModel realm, List<IdentityProviderModel> identityProviders, KeycloakSession session) {
        assertFalse(identityProviders.isEmpty());

        Set<String> checkedProviders = new HashSet<>(ExpectedProviders.get());

        for (IdentityProviderModel identityProvider : identityProviders) {
            if (identityProvider.getAlias().startsWith("model-")) {
                String providerId = identityProvider.getProviderId();

                if (SAMLIdentityProviderFactory.PROVIDER_ID.equals(providerId)) {
                    assertSamlIdentityProviderConfig(identityProvider, session);
                } else if (GoogleIdentityProviderFactory.PROVIDER_ID.equals(providerId)) {
                    assertGoogleIdentityProviderConfig(identityProvider, session);
                } else if (OIDCIdentityProviderFactory.PROVIDER_ID.equals(providerId)) {
                    assertOidcIdentityProviderConfig(identityProvider, session);
                } else if (FacebookIdentityProviderFactory.PROVIDER_ID.equals(providerId)) {
                    assertFacebookIdentityProviderConfig(realm, identityProvider, session);
                } else if (GitHubIdentityProviderFactory.PROVIDER_ID.equals(providerId)) {
                    assertGitHubIdentityProviderConfig(realm, identityProvider, session);
                } else if (PayPalIdentityProviderFactory.PROVIDER_ID.equals(providerId)) {
                    assertPayPalIdentityProviderConfig(realm, identityProvider, session);
                } else if (TwitterIdentityProviderFactory.PROVIDER_ID.equals(providerId)) {
                    assertTwitterIdentityProviderConfig(identityProvider, session);
                } else if (LinkedInIdentityProviderFactory.PROVIDER_ID.equals(providerId)) {
                    assertLinkedInIdentityProviderConfig(identityProvider, session);
                } else if (StackoverflowIdentityProviderFactory.PROVIDER_ID.equals(providerId)) {
                    assertStackoverflowIdentityProviderConfig(identityProvider, session);
                } else if (OpenshiftV3IdentityProviderFactory.PROVIDER_ID.equals(providerId)) {
                    assertOpenshiftIdentityProviderConfig(identityProvider, session);
                } else {
                    continue;
                }

                checkedProviders.remove(providerId);
            }
        }

        assertTrue(checkedProviders.isEmpty());
    }

    private void assertGoogleIdentityProviderConfig(IdentityProviderModel identityProvider, KeycloakSession session) {
        GoogleIdentityProvider googleIdentityProvider = new GoogleIdentityProviderFactory().create(session, identityProvider);
        OIDCIdentityProviderConfig config = googleIdentityProvider.getConfig();

        assertEquals("model-google", config.getAlias());
        assertEquals(GoogleIdentityProviderFactory.PROVIDER_ID, config.getProviderId());
        assertEquals(true, config.isEnabled());
        assertEquals(true, config.isTrustEmail());
        assertEquals(false, config.isAuthenticateByDefault());
        assertEquals(true, config.isStoreToken());
        assertEquals("clientId", config.getClientId());
        assertEquals("clientSecret", config.getClientSecret());
        assertEquals(GoogleIdentityProvider.AUTH_URL, config.getAuthorizationUrl());
        assertEquals(GoogleIdentityProvider.TOKEN_URL, config.getTokenUrl());
        assertEquals(GoogleIdentityProvider.PROFILE_URL, config.getUserInfoUrl());

    }

    private void assertSamlIdentityProviderConfig(IdentityProviderModel identityProvider, KeycloakSession session) {
        SAMLIdentityProvider samlIdentityProvider = new SAMLIdentityProviderFactory().create(session, identityProvider);
        SAMLIdentityProviderConfig config = samlIdentityProvider.getConfig();

        assertEquals("model-saml-signed-idp", config.getAlias());
        assertEquals(SAMLIdentityProviderFactory.PROVIDER_ID, config.getProviderId());
        assertEquals(true, config.isEnabled());
        assertEquals(false, config.isAuthenticateByDefault());
        assertEquals(false, config.isTrustEmail());
        assertEquals(false, config.isStoreToken());
        assertEquals("http://localhost:8082/auth/realms/realm-with-saml-identity-provider/protocol/saml", config.getSingleSignOnServiceUrl());
        assertEquals("urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress", config.getNameIDPolicyFormat());
        assertEquals("MIICwzCCAasCBgFlqZ1FNTANBgkqhkiG9w0BAQsFADAlMSMwIQYDVQQDDBpyZWFsbS13aXRoLXNhbWwtc2lnbmVkLWlkcDAeFw0xODA5MDUxMjAxMjFaFw0yODA5MDUxMjAzMDFaMCUxIzAhBgNVBAMMGnJlYWxtLXdpdGgtc2FtbC1zaWduZWQtaWRwMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgj8r0029eL0jJKXv6XbNj+QqsZO25HhZ0IjTEtb8mfh0tju/X8c6dXgILh5wU7OF00U+0mSYSE/+rrYKmY5g4oCleTe1+abavATP1tamtXGAUYqdutaXPrVn9yMsCWEPchSPZlEGq5iBJdA+xh9ejUmZJYXmln26HUVWq71/jC9GpjbRmFQ37f0X7WJoGyiqyttfKkKfUeBmRbX/0P0Zm6DVze8HjCDVPBllZE0a3HCgSF0rp0+s1xn7o91qdWKVattAVsGNjjDPz/sgwHOyyhDtSyajwXU+K/QUZ9pV4moGtwC9uIEymTylP7bu7qnxXIhfouEa+fEjAzTs0HJ5JQIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQBRiW8PXdVr1aGaLrlVIJHvqQiPXh2/ZB+An0Ed7gH03mWGMiC63O3kisrhFGD8uS7YVlIxOfINyB1IT0XJ51Vxo0lWimcKTXzA7MjWlHoWnR9ZmvPiWZpjjAvyxl0nSqJWk2gtRD/PHNgJpqmISnyFqLIJqbr2Zk3Jv87j0CyPoUKMnSOXZYI+HQhgUJyY9CyyIVGrWTs21dkuG4Z9bYzcDYW3zPwt7zWgCd4wr/Gg2ZJeIpdlcfg30Dn2nkkDDpnEIEq/MtLam9Q1/f6T/XyaIEOyWDMDs62bvNtFlt0d4q2oOWJNyEYDAoSnx7x+/ac0Y8EYGRrNpmpiXwUxt2th", config.getSigningCertificate());
        assertEquals(true, config.isWantAuthnRequestsSigned());
        assertEquals(true, config.isForceAuthn());
        assertEquals(true, config.isPostBindingAuthnRequest());
        assertEquals(true, config.isPostBindingResponse());
        assertEquals(true, config.isValidateSignature());
        assertEquals(false, config.isAddExtensionsElementWithKeyInfo());
    }

    private void assertOidcIdentityProviderConfig(IdentityProviderModel identityProvider, KeycloakSession session) {
        OIDCIdentityProvider googleIdentityProvider = new OIDCIdentityProviderFactory().create(session, identityProvider);
        OIDCIdentityProviderConfig config = googleIdentityProvider.getConfig();

        assertEquals("model-oidc-idp", config.getAlias());
        assertEquals(OIDCIdentityProviderFactory.PROVIDER_ID, config.getProviderId());
        assertEquals(false, config.isEnabled());
        assertEquals(false, config.isTrustEmail());
        assertEquals(false, config.isAuthenticateByDefault());
        assertEquals(false, config.isStoreToken());
        assertEquals("clientId", config.getClientId());
        assertEquals("clientSecret", config.getClientSecret());
    }

    private void assertFacebookIdentityProviderConfig(RealmModel realm, IdentityProviderModel identityProvider, KeycloakSession session) {
        FacebookIdentityProvider facebookIdentityProvider = new FacebookIdentityProviderFactory().create(session, identityProvider);
        OAuth2IdentityProviderConfig config = facebookIdentityProvider.getConfig();

        assertEquals("model-facebook", config.getAlias());
        assertEquals(FacebookIdentityProviderFactory.PROVIDER_ID, config.getProviderId());
        assertEquals(true, config.isEnabled());
        assertEquals(false, config.isTrustEmail());
        assertEquals(false, config.isAuthenticateByDefault());
        assertEquals(false, config.isStoreToken());
        assertEquals("clientId", config.getClientId());
        assertEquals("clientSecret", config.getClientSecret());
        assertEquals(realm.getBrowserFlow().getId(), identityProvider.getFirstBrokerLoginFlowId());
        Assert.assertNull(identityProvider.getPostBrokerLoginFlowId());
        assertEquals(FacebookIdentityProvider.AUTH_URL, config.getAuthorizationUrl());
        assertEquals(FacebookIdentityProvider.TOKEN_URL, config.getTokenUrl());
        assertEquals(FacebookIdentityProvider.PROFILE_URL, config.getUserInfoUrl());
    }

    private void assertGitHubIdentityProviderConfig(RealmModel realm, IdentityProviderModel identityProvider, KeycloakSession session) {
        GitHubIdentityProvider gitHubIdentityProvider = new GitHubIdentityProviderFactory().create(session, identityProvider);
        OAuth2IdentityProviderConfig config = gitHubIdentityProvider.getConfig();

        assertEquals("model-github", config.getAlias());
        assertEquals(GitHubIdentityProviderFactory.PROVIDER_ID, config.getProviderId());
        assertEquals(true, config.isEnabled());
        assertEquals(false, config.isTrustEmail());
        assertEquals(false, config.isAuthenticateByDefault());
        assertEquals(false, config.isStoreToken());
        assertEquals("clientId", config.getClientId());
        assertEquals("clientSecret", config.getClientSecret());
        assertEquals(realm.getFlowByAlias(DefaultAuthenticationFlows.FIRST_BROKER_LOGIN_FLOW).getId(), identityProvider.getFirstBrokerLoginFlowId());
        assertEquals(realm.getBrowserFlow().getId(), identityProvider.getPostBrokerLoginFlowId());
        assertEquals(GitHubIdentityProvider.AUTH_URL, config.getAuthorizationUrl());
        assertEquals(GitHubIdentityProvider.TOKEN_URL, config.getTokenUrl());
        assertEquals(GitHubIdentityProvider.PROFILE_URL, config.getUserInfoUrl());
    }

    private void assertPayPalIdentityProviderConfig(RealmModel realm, IdentityProviderModel identityProvider, KeycloakSession session) {
        PayPalIdentityProvider payPalIdentityProvider = new PayPalIdentityProviderFactory().create(session, identityProvider);
        PayPalIdentityProviderConfig config = payPalIdentityProvider.getConfig();

        assertEquals("model-paypal", config.getAlias());
        assertEquals(PayPalIdentityProviderFactory.PROVIDER_ID, config.getProviderId());
        assertEquals(true, config.isEnabled());
        assertEquals(false, config.isTrustEmail());
        assertEquals(false, config.isAuthenticateByDefault());
        assertEquals(false, config.isStoreToken());
        assertEquals("clientId", config.getClientId());
        assertEquals("clientSecret", config.getClientSecret());
        assertEquals(false, config.targetSandbox());
        assertEquals(realm.getFlowByAlias(DefaultAuthenticationFlows.FIRST_BROKER_LOGIN_FLOW).getId(), identityProvider.getFirstBrokerLoginFlowId());
        assertEquals(realm.getBrowserFlow().getId(), identityProvider.getPostBrokerLoginFlowId());
        assertEquals(PayPalIdentityProvider.AUTH_URL, config.getAuthorizationUrl());
        assertEquals(PayPalIdentityProvider.BASE_URL + PayPalIdentityProvider.TOKEN_RESOURCE, config.getTokenUrl());
        assertEquals(PayPalIdentityProvider.BASE_URL + PayPalIdentityProvider.PROFILE_RESOURCE, config.getUserInfoUrl());
    }

    private void assertLinkedInIdentityProviderConfig(IdentityProviderModel identityProvider, KeycloakSession session) {
        LinkedInIdentityProvider liIdentityProvider = new LinkedInIdentityProviderFactory().create(session, identityProvider);
        OAuth2IdentityProviderConfig config = liIdentityProvider.getConfig();

        assertEquals("model-linkedin", config.getAlias());
        assertEquals(LinkedInIdentityProviderFactory.PROVIDER_ID, config.getProviderId());
        assertEquals(true, config.isEnabled());
        assertEquals(false, config.isTrustEmail());
        assertEquals(false, config.isAuthenticateByDefault());
        assertEquals(false, config.isStoreToken());
        assertEquals("clientId", config.getClientId());
        assertEquals("clientSecret", config.getClientSecret());
        assertEquals(LinkedInIdentityProvider.AUTH_URL, config.getAuthorizationUrl());
        assertEquals(LinkedInIdentityProvider.TOKEN_URL, config.getTokenUrl());
        assertEquals(LinkedInIdentityProvider.PROFILE_URL, config.getUserInfoUrl());
    }

    private void assertStackoverflowIdentityProviderConfig(IdentityProviderModel identityProvider, KeycloakSession session) {
        StackoverflowIdentityProvider soIdentityProvider = new StackoverflowIdentityProviderFactory().create(session, identityProvider);
        StackOverflowIdentityProviderConfig config = soIdentityProvider.getConfig();

        assertEquals("model-stackoverflow", config.getAlias());
        assertEquals(StackoverflowIdentityProviderFactory.PROVIDER_ID, config.getProviderId());
        assertEquals(true, config.isEnabled());
        assertEquals(false, config.isTrustEmail());
        assertEquals(false, config.isAuthenticateByDefault());
        assertEquals(false, config.isStoreToken());
        assertEquals("clientId", config.getClientId());
        assertEquals("clientSecret", config.getClientSecret());
        assertEquals("keyValue", config.getKey());
        assertEquals(StackoverflowIdentityProvider.AUTH_URL, config.getAuthorizationUrl());
        assertEquals(StackoverflowIdentityProvider.TOKEN_URL, config.getTokenUrl());
        assertEquals(StackoverflowIdentityProvider.PROFILE_URL, config.getUserInfoUrl());
    }

    private void assertOpenshiftIdentityProviderConfig(IdentityProviderModel identityProvider, KeycloakSession session) {
        OpenshiftV3IdentityProvider osoIdentityProvider = new OpenshiftV3IdentityProviderFactory().create(session, identityProvider);
        OpenshiftV3IdentityProviderConfig config = osoIdentityProvider.getConfig();

        assertEquals("model-openshift-v3", config.getAlias());
        assertEquals(OpenshiftV3IdentityProviderFactory.PROVIDER_ID, config.getProviderId());
        assertEquals(true, config.isEnabled());
        assertEquals(false, config.isTrustEmail());
        assertEquals(false, config.isAuthenticateByDefault());
        assertEquals(true, config.isStoreToken());
        assertEquals(OpenshiftV3IdentityProvider.BASE_URL, config.getBaseUrl());
        assertEquals("clientId", config.getClientId());
        assertEquals("clientSecret", config.getClientSecret());
    }

    private void assertTwitterIdentityProviderConfig(IdentityProviderModel identityProvider, KeycloakSession session) {
        TwitterIdentityProvider twitterIdentityProvider = new TwitterIdentityProviderFactory().create(session, identityProvider);
        OAuth2IdentityProviderConfig config = twitterIdentityProvider.getConfig();

        assertEquals("model-twitter", config.getAlias());
        assertEquals(TwitterIdentityProviderFactory.PROVIDER_ID, config.getProviderId());
        assertEquals(true, config.isEnabled());
        assertEquals(false, config.isTrustEmail());
        assertEquals(false, config.isAuthenticateByDefault());
        assertEquals(true, config.isStoreToken());
        assertEquals("clientId", config.getClientId());
        assertEquals("clientSecret", config.getClientSecret());
    }   
}
