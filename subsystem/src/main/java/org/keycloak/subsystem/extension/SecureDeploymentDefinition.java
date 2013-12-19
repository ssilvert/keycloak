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

import java.util.ArrayList;
import java.util.List;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.operations.common.GenericSubsystemDescribeHandler;
import org.jboss.as.controller.operations.validation.StringLengthValidator;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * Defines attributes and operations for the Keycloak Subsystem
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2013 Red Hat Inc.
 */
public class SecureDeploymentDefinition extends SimpleResourceDefinition {

    public static final String TAG_NAME = "secure-deployment";

    protected static final AttributeDefinition RESOURCE =
            new SimpleAttributeDefinitionBuilder("resource", ModelType.STRING, true)
            .setXmlName("resource")
            .setAllowExpression(true)
            .setValidator(new StringLengthValidator(1, Integer.MAX_VALUE, true, true))
            .build();
    protected static final SimpleAttributeDefinition DISABLE_TRUST_MANAGER =
            new SimpleAttributeDefinitionBuilder("use-resource-role-mappings", ModelType.BOOLEAN, true)
            .setXmlName("use-resource-role-mappings")
            .setAllowExpression(true)
            .setDefaultValue(new ModelNode(false))
            .build();
    protected static final SimpleAttributeDefinition BEARER_ONLY =
            new SimpleAttributeDefinitionBuilder("bearer-only", ModelType.BOOLEAN, true)
            .setXmlName("bearer-only")
            .setAllowExpression(true)
            .setDefaultValue(new ModelNode(false))
            .build();

    protected static final List<AttributeDefinition> DEPLOYMENT_ONLY_ATTRIBUTES = new ArrayList<AttributeDefinition>();
    static {
        DEPLOYMENT_ONLY_ATTRIBUTES.add(RESOURCE);
        DEPLOYMENT_ONLY_ATTRIBUTES.add(DISABLE_TRUST_MANAGER);
        DEPLOYMENT_ONLY_ATTRIBUTES.add(BEARER_ONLY);
    }

    protected static final List<AttributeDefinition> ALL_ATTRIBUTES = new ArrayList<AttributeDefinition>();
    static {
        ALL_ATTRIBUTES.addAll(DEPLOYMENT_ONLY_ATTRIBUTES);
        ALL_ATTRIBUTES.addAll(SharedAttributeDefinitons.ATTRIBUTES);
    }

    private static SecureDeploymentWriteAttributeHandler attrHandler = new SecureDeploymentWriteAttributeHandler(ALL_ATTRIBUTES);

    public SecureDeploymentDefinition() {
        super(PathElement.pathElement("secure-deployment"),
                KeycloakExtension.getResourceDescriptionResolver("secure-deployment"),
                SecureDeploymentAddHandler.INSTANCE,
                SecureDeploymentRemoveHandler.INSTANCE);
    }

    @Override
    public void registerOperations(ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);
        resourceRegistration.registerOperationHandler(GenericSubsystemDescribeHandler.DEFINITION, GenericSubsystemDescribeHandler.INSTANCE);
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        super.registerAttributes(resourceRegistration);
        for (AttributeDefinition attrDef : ALL_ATTRIBUTES) {
            resourceRegistration.registerReadWriteAttribute(attrDef, null, attrHandler);
        }
    }
}
