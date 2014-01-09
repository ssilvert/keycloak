/*
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
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

package org.keycloak.subsystem.extension;

import java.util.HashMap;
import java.util.Map;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADDRESS;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2013 Red Hat Inc.
 */
public class KeycloakAdapterConfigService implements Service<KeycloakAdapterConfigService> {
    private static final String CREDENTIALS_JSON_NAME = "credentials";

    public static final ServiceName SERVICE_NAME = ServiceName.JBOSS.append("KeycloakAdapterConfigService");
    public static final KeycloakAdapterConfigService INSTANCE = new KeycloakAdapterConfigService();

    private Map<String, ModelNode> realms = new HashMap<String, ModelNode>();
    private Map<String, ModelNode> deployments = new HashMap<String, ModelNode>();

    private KeycloakAdapterConfigService() {

    }

    @Override
    public void start(StartContext sc) throws StartException {

    }

    @Override
    public void stop(StopContext sc) {

    }

    @Override
    public KeycloakAdapterConfigService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    public void addRealm(ModelNode operation, ModelNode model) {
        this.realms.put(realmNameFromOp(operation), model.clone());
    }

    public void updateRealm(ModelNode operation, String attrName, ModelNode resolvedValue) {
        ModelNode realm = this.realms.get(realmNameFromOp(operation));
        realm.get(attrName).set(resolvedValue);
    }

    public void removeRealm(ModelNode operation) {
        this.realms.remove(realmNameFromOp(operation));
    }

    public void addSecureDeployment(ModelNode operation, ModelNode model) {
        ModelNode deployment = model.clone();
        deployment.get(RealmDefinition.TAG_NAME).set(realmNameFromOp(operation));
        this.deployments.put(deploymentNameFromOp(operation), deployment);
    }

    public void updateSecureDeployment(ModelNode operation, String attrName, ModelNode resolvedValue) {
        ModelNode deployment = this.deployments.get(deploymentNameFromOp(operation));
        deployment.get(attrName).set(resolvedValue);
        System.out.println("===== Updated Deployment =======");
        System.out.println("deployment=");
        System.out.println(this.deployments.get(deploymentNameFromOp(operation)).toString());
        System.out.println("================================");
    }

    public void addCredential(ModelNode operation, ModelNode model) {
        ModelNode deployment = this.deployments.get(deploymentNameFromOp(operation));
        ModelNode credentials = deployment.get(CREDENTIALS_JSON_NAME);
        if (!credentials.isDefined()) {
            credentials = new ModelNode();
        }

        String credentialName = credentialNameFromOp(operation);
        credentials.get(credentialName).set(model.get("value").asString());

        deployment.get(CREDENTIALS_JSON_NAME).set(credentials);

        System.out.println("====== added credential ============");
        System.out.println("deployment =");
        System.out.println(this.deployments.get(deploymentNameFromOp(operation)));
    }

    private String realmNameFromOp(ModelNode operation) {
        return valueFromOp(RealmDefinition.TAG_NAME, operation);
    }

    private String deploymentNameFromOp(ModelNode operation) {
        return valueFromOp(SecureDeploymentDefinition.TAG_NAME, operation);
    }

    private String credentialNameFromOp(ModelNode operation) {
        return valueFromOp(CredentialDefinition.TAG_NAME, operation);
    }

    private String valueFromOp(String tagName, ModelNode operation) {
        String deploymentName = getValueOfAddrElement(operation.get(ADDRESS), tagName);
        if (deploymentName == null) throw new RuntimeException("Can't find '" + tagName + "' in address " + operation.toString());
        return deploymentName;
    }

    private String getValueOfAddrElement(ModelNode address, String elementName) {
        for (ModelNode element : address.asList()) {
            if (element.has(elementName)) return element.get(elementName).asString();
        }

        return null;
    }

    public String getJSON(String deploymentName, String resourceName) {
        ModelNode deployment = this.deployments.get(deploymentName);
        String realmName = deployment.get(RealmDefinition.TAG_NAME).asString();
        ModelNode realm = this.realms.get(realmName);

        ModelNode json = new ModelNode();
        json.get(RealmDefinition.TAG_NAME).set(realmName);
        json.get("resource").set(resourceName);

        // Realm values set first.  Some can be overridden by deployment values.
        setJSONValues(json, realm);
        setJSONValues(json, deployment);

        // TODO: change this to true to compact the string
        return json.toJSONString(false);
    }

    private void setJSONValues(ModelNode json, ModelNode values) {
        for (Property prop : values.asPropertyList()) {
            String name = prop.getName();
            ModelNode value = prop.getValue();
            if (value.isDefined()) {
                json.get(name).set(value);
            }
        }
    }

    public boolean isKeycloakDeployment(String deploymentName) {
        return this.deployments.containsKey(deploymentName);
    }

    static KeycloakAdapterConfigService find(ServiceRegistry registry) {
        ServiceController<?> container = registry.getService(KeycloakAdapterConfigService.SERVICE_NAME);
        if (container != null) {
            KeycloakAdapterConfigService service = (KeycloakAdapterConfigService)container.getValue();
            return service;
        }
        return null;
    }

    static KeycloakAdapterConfigService find(OperationContext context) {
        return find(context.getServiceRegistry(true));
    }
}
