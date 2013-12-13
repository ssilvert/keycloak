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

import org.jboss.as.controller.AbstractWriteAttributeHandler;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceName;

/**
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2013 Red Hat Inc.
 */
public class SecureDeploymentWriteAttributeHandler extends AbstractWriteAttributeHandler<SecureDeploymentService> {

    public SecureDeploymentWriteAttributeHandler(AttributeDefinition... definitions) {
        super(definitions);
    }

    @Override
    protected boolean applyUpdateToRuntime(OperationContext context, ModelNode operation, String attributeName,
                                           ModelNode resolvedValue, ModelNode currentValue, HandbackHolder<SecureDeploymentService> hh) throws OperationFailedException {
        SecureDeploymentService service = getSecureDeploymentService(context, operation);
        if (service == null) {
            throw new OperationFailedException("Could not find SecureDeploymentService for this secure-deployment.");
        }

        System.out.println("*****************************");
        System.out.println("SecureDeploymentWriteAttributeHandler.applyUpdateToRuntime");
        System.out.println("attributeName=" + attributeName);
        System.out.println("resolvedValue=" + resolvedValue.toString());
        System.out.println("*****************************");
        hh.setHandback(service);

        service.updateKeycloakModelAttribute(attributeName, resolvedValue);

        return false;
    }

    private SecureDeploymentService getSecureDeploymentService(OperationContext context, ModelNode operation) {
        String deploymentName = SecureDeploymentService.getDeploymentNameFromOperation(operation);
        ServiceName serviceName =  SecureDeploymentService.createServiceName(deploymentName);
        return (SecureDeploymentService)context.getServiceRegistry(true)
                                               .getService(serviceName)
                                               .getValue();
    }

    @Override
    protected void revertUpdateToRuntime(OperationContext context, ModelNode operation, String attributeName,
                                         ModelNode valueToRestore, ModelNode valueToRevert, SecureDeploymentService service) throws OperationFailedException {
        service.updateKeycloakModelAttribute(attributeName, valueToRestore);
    }

}
