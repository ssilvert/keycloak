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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.keycloak.broker.oidc.OIDCIdentityProviderFactory;
import org.keycloak.broker.saml.SAMLIdentityProviderFactory;
import org.keycloak.social.facebook.FacebookIdentityProviderFactory;
import org.keycloak.social.github.GitHubIdentityProviderFactory;
import org.keycloak.social.google.GoogleIdentityProviderFactory;
import org.keycloak.social.linkedin.LinkedInIdentityProviderFactory;
import org.keycloak.social.paypal.PayPalIdentityProviderFactory;
import org.keycloak.social.stackoverflow.StackoverflowIdentityProviderFactory;
import org.keycloak.social.twitter.TwitterIdentityProviderFactory;

/**
 * @author Stan Silvert ssilvert@redhat.com (C) 2019 Red Hat Inc.
 */
public class ExpectedProviders {

    private static final Set<String> expectedProviders;

    static {
        Set<String> providers = new HashSet<>();

        providers.add(SAMLIdentityProviderFactory.PROVIDER_ID);
        providers.add(OIDCIdentityProviderFactory.PROVIDER_ID);
        providers.add(GoogleIdentityProviderFactory.PROVIDER_ID);
        providers.add(FacebookIdentityProviderFactory.PROVIDER_ID);
        providers.add(GitHubIdentityProviderFactory.PROVIDER_ID);
        providers.add(PayPalIdentityProviderFactory.PROVIDER_ID);
        providers.add(TwitterIdentityProviderFactory.PROVIDER_ID);
        providers.add(LinkedInIdentityProviderFactory.PROVIDER_ID);
        providers.add(StackoverflowIdentityProviderFactory.PROVIDER_ID);

        expectedProviders = Collections.unmodifiableSet(providers);
    }
    
    private ExpectedProviders() {};
        
    public static Set<String> get() {
        return expectedProviders;
    }
}
