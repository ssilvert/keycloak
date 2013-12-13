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

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2013 Red Hat Inc.
 */
public class SecureDeploymentService implements Service<SecureDeploymentService> {

    private static final String SERVICE_NAME = "SecureDeploymentService";

    private final String secureDeploymentName;
    private ModelNode keycloakModel;

    public SecureDeploymentService(String secureDeploymentName, ModelNode keycloakModel) {
        this.secureDeploymentName = secureDeploymentName;
        this.keycloakModel = keycloakModel.clone();
    }

    @Override
    public void start(StartContext sc) throws StartException {

    }

    @Override
    public void stop(StopContext sc) {

    }

    @Override
    public SecureDeploymentService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    /**
     * Use the last element of the address to create the proper service name.
     * @param node
     * @return
     */
    public static String getDeploymentNameFromOperation(ModelNode operation) {
        return PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();
    }

    public static ServiceName createServiceName(String suffix) {
        return ServiceName.JBOSS.append(SERVICE_NAME, suffix);
    }

    public String getSecureDeploymentName() {
        return this.secureDeploymentName;
    }

    public void updateKeycloakModelAttribute(String attributeName, ModelNode value) {
        this.keycloakModel.get(attributeName).set(value);
    }
}
