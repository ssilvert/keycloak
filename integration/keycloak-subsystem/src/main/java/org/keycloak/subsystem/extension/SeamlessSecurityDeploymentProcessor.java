/*
 * Copyright 2014 Red Hat Inc. and/or its affiliates and other contributors
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

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.web.common.WarMetaData;
import org.jboss.logging.Logger;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.LoginConfigMetaData;

import org.jboss.as.ee.structure.DeploymentType;
import org.jboss.as.ee.structure.DeploymentTypeMarker;
import org.jboss.dmr.ModelNode;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ApplicationsResource;
import org.keycloak.representations.idm.ApplicationRepresentation;

/**
 * If no auth method is declared in web.xml, use a template to set up the
 * deployment as a Keycloak secure deployment.
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2014 Red Hat Inc.
 */
public class SeamlessSecurityDeploymentProcessor implements DeploymentUnitProcessor {
    protected Logger log = Logger.getLogger(SeamlessSecurityDeploymentProcessor.class);

    protected boolean hasAuthMethod(DeploymentUnit deploymentUnit, KeycloakAdapterConfigService service) {
        String deploymentName = deploymentUnit.getName();
        if (service.isSecureDeployment(deploymentName) && !service.isSeamlessDeployment(deploymentName)) {
            return true; // already has secure deployment declared
        }
        System.out.println("#1");
        WarMetaData warMetaData = deploymentUnit.getAttachment(WarMetaData.ATTACHMENT_KEY);
        if (warMetaData == null) return false;
        System.out.println("#2");
        JBossWebMetaData webMetaData = warMetaData.getMergedJBossWebMetaData();
        if (webMetaData == null) return false;
        System.out.println("#3");

        // Keycloak Server implicitly has an auth method
        // TODO: Make this use a context param instead.  That way, any WAR can signal that it doesn't want to be a seamless client.
        if ((webMetaData.getDescriptionGroup() != null) && "Keycloak Server".equals(webMetaData.getDescriptionGroup().getDisplayName())) return true;

        System.out.println("#4");
        LoginConfigMetaData loginConfig = webMetaData.getLoginConfig();
        if (loginConfig == null || loginConfig.getAuthMethod() == null) {
            System.out.println("#5");
            return false;
        }
        System.out.println("#6");
        return true;
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        if (!DeploymentTypeMarker.isType(DeploymentType.WAR, deploymentUnit)) return;

        String deploymentName = deploymentUnit.getName();
        KeycloakAdapterConfigService service = KeycloakAdapterConfigService.find(phaseContext.getServiceRegistry());

        System.out.println("**************************");
        System.out.println(deploymentName + ": hasAuthMethod = " + hasAuthMethod(deploymentUnit, service));
        System.out.println("**************************");

        ModelNode seamlessDeploymentModel = service.getSecureDeploymentTemplate();
        System.out.println("$1");
        if (seamlessDeploymentModel == null) return;
        System.out.println("$2");
        if (hasAuthMethod(deploymentUnit, service)) return;
        System.out.println("$3");

        // Can't do this.  context root is null at this point?
        String ctxRoot = deploymentUnit.getAttachment(WarMetaData.ATTACHMENT_KEY).getMergedJBossWebMetaData().getContextRoot();
        System.out.println("ctxRoot=" + ctxRoot);

        seamlessDeploymentModel.get(SecureDeploymentDefinition.RESOURCE.getName()).set(deploymentName.substring(0, deploymentName.lastIndexOf('.')));

        seamlessDeploymentModel.get(KeycloakAdapterConfigService.CREDENTIALS_JSON_NAME).get("secret").set("a7a8f2e3-463c-4911-bac7-24f0655402df");

        System.out.println("adding seamless secure deployment for " + deploymentName);
        System.out.println("template:");
        System.out.println(seamlessDeploymentModel.toString());
        service.addSecureDeployment(deploymentName, seamlessDeploymentModel, true);
        addDeploymentToAuthServer(deploymentName, seamlessDeploymentModel);
    }

    private void addDeploymentToAuthServer(String deploymentName, ModelNode seamlessDeploymentModel) {
        try {
        Class clazz = getClass().getClassLoader().loadClass("org.keycloak.admin.client.token.TokenService");
        Thread.currentThread().setContextClassLoader(clazz.getClassLoader());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        String realm = seamlessDeploymentModel.get("real").asString();

        Keycloak keycloak = Keycloak.getInstance("http://localhost:8080/auth", realm, "admin", "password", "security-admin-console");
        ApplicationsResource applications = keycloak.realm(realm).applications();
        for (ApplicationRepresentation app : applications.findAll()) {
        if (app.getBaseUrl() != null) {
            System.out.println(app.getBaseUrl() + app.getName());
        } else {
            System.out.println(app.getName());
        }
    }
    }

    @Override
    public void undeploy(DeploymentUnit deploymentUnit) {

    }

}
