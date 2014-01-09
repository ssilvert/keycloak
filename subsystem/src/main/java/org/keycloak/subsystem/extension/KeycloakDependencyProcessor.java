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

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.module.ModuleDependency;
import org.jboss.as.server.deployment.module.ModuleSpecification;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoader;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceRegistry;

/**
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2013 Red Hat Inc.
 */
public class KeycloakDependencyProcessor implements DeploymentUnitProcessor {

    private static final ModuleIdentifier KEYCLOAK_ADAPTER = ModuleIdentifier.create("org.keycloak.adapter");
/*    private static final ModuleIdentifier BOUNCY_CASTLE = ModuleIdentifier.create("org.bouncycastle");
    private static final ModuleIdentifier JAX_RS = ModuleIdentifier.create("org.jboss.resteasy.resteasy-jaxrs");
    private static final ModuleIdentifier JACKSON_PROVIDER = ModuleIdentifier.create("org.jboss.resteasy.resteasy-jackson-provider");
    private static final ModuleIdentifier JOSE_JWT = ModuleIdentifier.create("org.jboss.resteasy.jose-jwt"); */

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        String deploymentName = deploymentUnit.getName();

     /*   KeycloakAdapterConfigService service = KeycloakAdapterConfigService.find(phaseContext.getServiceRegistry(), deploymentName);
        if (service != null) {
            addModules(deploymentUnit);
        } */
    }

    private void addModules(DeploymentUnit deploymentUnit) {
    /*    System.out.println("********************");
        System.out.println("Adding keycloak modules");
        System.out.println("*********************"); */
        final ModuleSpecification moduleSpecification = deploymentUnit.getAttachment(Attachments.MODULE_SPECIFICATION);
        final ModuleLoader moduleLoader = Module.getBootModuleLoader();

        moduleSpecification.addSystemDependency(new ModuleDependency(moduleLoader, KEYCLOAK_ADAPTER, false, false, true, false));
  //      moduleSpecification.addSystemDependency(new ModuleDependency(moduleLoader, BOUNCY_CASTLE, false, false, false, false));
   //     moduleSpecification.addSystemDependency(new ModuleDependency(moduleLoader, JAX_RS, false, false, true, false));
   //     moduleSpecification.addSystemDependency(new ModuleDependency(moduleLoader, JACKSON_PROVIDER, false, false, true, false));
   //     moduleSpecification.addSystemDependency(new ModuleDependency(moduleLoader, JOSE_JWT, false, false, false, false));
    }

    @Override
    public void undeploy(DeploymentUnit du) {

    }

}
