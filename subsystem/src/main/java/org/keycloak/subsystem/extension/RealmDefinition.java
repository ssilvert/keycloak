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

import org.jboss.as.controller.ModelOnlyWriteAttributeHandler;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.operations.common.GenericSubsystemDescribeHandler;
import org.jboss.as.controller.operations.validation.IntRangeValidator;
import org.jboss.as.controller.operations.validation.StringLengthValidator;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * Defines attributes and operations for the Realm
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2013 Red Hat Inc.
 */
public class RealmDefinition extends SimpleResourceDefinition {

    protected static final SimpleAttributeDefinition REALM_PUBLIC_KEY =
            new SimpleAttributeDefinitionBuilder("realm-public-key", ModelType.STRING, false)
                    .setXmlName("realm-public-key")
                    .setAllowExpression(true)
                    .setValidator(new StringLengthValidator(1, Integer.MAX_VALUE, false, true))
                    .build();

    protected static final SimpleAttributeDefinition AUTH_URL =
            new SimpleAttributeDefinitionBuilder("auth-url", ModelType.STRING, false)
                    .setXmlName("auth-url")
                    .setAllowExpression(true)
                    .setValidator(new StringLengthValidator(1, Integer.MAX_VALUE, false, true))
                    .build();

    protected static final SimpleAttributeDefinition CODE_URL =
            new SimpleAttributeDefinitionBuilder("code-url", ModelType.STRING, false)
                    .setXmlName("code-url")
                    .setAllowExpression(true)
                    .setValidator(new StringLengthValidator(1, Integer.MAX_VALUE, false, true))
                    .build();

    protected static final SimpleAttributeDefinition SSL_NOT_REQUIRED =
            new SimpleAttributeDefinitionBuilder("ssl-not-required", ModelType.BOOLEAN, true)
                    .setXmlName("ssl-not-required")
                    .setAllowExpression(true)
                    .setDefaultValue(new ModelNode(true))
                    .build();

    protected static final SimpleAttributeDefinition ALLOW_ANY_HOSTNAME =
            new SimpleAttributeDefinitionBuilder("allow-any-hostname", ModelType.BOOLEAN, true)
                    .setXmlName("allow-any-hostname")
                    .setAllowExpression(true)
                    .setDefaultValue(new ModelNode(false))
                    .build();

    protected static final SimpleAttributeDefinition DISABLE_TRUST_MANAGER =
            new SimpleAttributeDefinitionBuilder("disable-trust-manager", ModelType.BOOLEAN, true)
                    .setXmlName("disable-trust-manager")
                    .setAllowExpression(true)
                    .setDefaultValue(new ModelNode(true))
                    .build();

    protected static final SimpleAttributeDefinition TRUSTSTORE =
            new SimpleAttributeDefinitionBuilder("truststore", ModelType.STRING, true)
                    .setXmlName("truststore")
                    .setAllowExpression(true)
                    .setValidator(new StringLengthValidator(1, Integer.MAX_VALUE, false, true))
                    .build();

    protected static final SimpleAttributeDefinition TRUSTSTORE_PASSWORD =
            new SimpleAttributeDefinitionBuilder("truststore-password", ModelType.STRING, true)
                    .setXmlName("truststore-password")
                    .setAllowExpression(true)
                    .setValidator(new StringLengthValidator(1, Integer.MAX_VALUE, false, true))
                    .build();

    protected static final SimpleAttributeDefinition CONNECTION_POOL_SIZE =
            new SimpleAttributeDefinitionBuilder("connection-pool-size", ModelType.INT, true)
                    .setXmlName("connection-pool-size")
                    .setAllowExpression(true)
                    .setDefaultValue(new ModelNode(10))
                    .setValidator(new IntRangeValidator(0))
                    .build();

/*    truststore (* if ssl and disable-trust-manager not set)
truststore-password (* if ssl and disable-trust-manager not set)
connection-pool-size */
    protected static final SimpleAttributeDefinition[] ATTRIBUTES = {
        REALM_PUBLIC_KEY,
        AUTH_URL,
        CODE_URL,
        TRUSTSTORE,
        TRUSTSTORE_PASSWORD,
        SSL_NOT_REQUIRED,
        ALLOW_ANY_HOSTNAME,
        DISABLE_TRUST_MANAGER,
        CONNECTION_POOL_SIZE
    };

    private static final ModelOnlyWriteAttributeHandler realmAttrHandler = new ModelOnlyWriteAttributeHandler(ATTRIBUTES);

    public RealmDefinition() {
        super(PathElement.pathElement("realm"),
                KeycloakExtension.getResourceDescriptionResolver("realm"),
                AddRealmHandler.INSTANCE,
                ReloadRequiredRemoveStepHandler.INSTANCE);
    }

    @Override
    public void registerOperations(ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);
        resourceRegistration.registerOperationHandler(GenericSubsystemDescribeHandler.DEFINITION, GenericSubsystemDescribeHandler.INSTANCE);
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        super.registerAttributes(resourceRegistration);

        for (SimpleAttributeDefinition attrDef : ATTRIBUTES) {
            resourceRegistration.registerReadWriteAttribute(attrDef, null, realmAttrHandler);
        }
    }
}
