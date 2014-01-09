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

import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;

import org.jboss.msc.service.ServiceName;

/**
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2013 Red Hat Inc.
 */
public class SecureDeploymentRemoveHandler extends AbstractRemoveStepHandler {

    public static SecureDeploymentRemoveHandler INSTANCE = new SecureDeploymentRemoveHandler();

    private SecureDeploymentRemoveHandler() {}

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
      /*  System.out.println("*********** SecureDeploymentRemoveHandler.performRuntime ***********");
        System.out.println("operation=" + operation.toString());
        System.out.println("model = " + model.toString());
        System.out.println("***************************************");  */

     /*   String secureDeploymentName = KeycloakAdapterConfigService.getDeploymentNameFromOperation(operation);
        ServiceName serviceName = KeycloakAdapterConfigService.createServiceName(secureDeploymentName);
        context.removeService(serviceName); */
    }
}
