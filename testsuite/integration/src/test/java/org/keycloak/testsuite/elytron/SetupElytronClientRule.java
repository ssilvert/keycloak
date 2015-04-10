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

import org.keycloak.models.ClientModel;
import org.keycloak.models.RealmModel;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.testsuite.rule.KeycloakRule;

/**
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2015 Red Hat Inc.
 */
public class SetupElytronClientRule extends KeycloakRule {
    public SetupElytronClientRule() {
        super(new KeycloakRule.KeycloakSetup() {
            @Override                                                                         // "test"
            public void config(RealmManager manager, RealmModel adminstrationRealm, RealmModel testRealm) {
                ClientModel oauthClient = testRealm.addClient("elytron-client");
                oauthClient.setEnabled(true);
                oauthClient.setPublicClient(false);
                oauthClient.setSecret("elytron");
                oauthClient.setDirectGrantsOnly(true);
            }
        });
    }
}
