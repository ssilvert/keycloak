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

package org.keycloak.subsystem.cli.extension;

import java.io.IOException;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationDefinition;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleOperationDefinitionBuilder;
import org.jboss.as.controller.operations.validation.EnumValidator;
//import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.subsystem.cli.extension.KeycloakREST.Failure;

/**
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2015 Red Hat Inc.
 */
public class SubmitHandler implements OperationStepHandler {
    static final String SUBMIT_OPERATION = "submit";
        static final SimpleAttributeDefinition USER = new SimpleAttributeDefinitionBuilder("user", ModelType.STRING, false)
        .setAllowNull(true)
        .build();

    static final SimpleAttributeDefinition PASSWORD = new SimpleAttributeDefinitionBuilder("password", ModelType.STRING, false)
        .setAllowNull(true)
        .build();

    static final SimpleAttributeDefinition METHOD = new SimpleAttributeDefinitionBuilder("method", ModelType.STRING, false)
        //.setFlags(AttributeAccess.Flag.RESTART_RESOURCE_SERVICES)
        .setValidator(EnumValidator.create(HttpMethod.class, false, false))
        .build();

    static final SimpleAttributeDefinition PATH = new SimpleAttributeDefinitionBuilder("path", ModelType.STRING, false)
        .build();

  //  static final SimpleAttributeDefinition PARAMS = new SimpleAttributeDefinitionBuilder("password", ModelType.OBJECT, false)
  //      .setV
  //      .build();

    static final OperationDefinition DEFINITION = new SimpleOperationDefinitionBuilder(SUBMIT_OPERATION, KeycloakCliSubsystemExtension.getResourceDescriptionResolver(null))
            .setRuntimeOnly()
            .setReplyType(ModelType.OBJECT)
            .setReplyValueType(ModelType.STRING)
            .addParameter(USER)
            .addParameter(PASSWORD)
            .addParameter(METHOD)
            .addParameter(PATH)
        //    .addParameter(PARAMS)
            .build();

    static final OperationStepHandler INSTANCE = new SubmitHandler();

    private SubmitHandler() {}

    @Override
    public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
        USER.validateOperation(operation);
        PASSWORD.validateOperation(operation);
        METHOD.validateOperation(operation);
        PATH.validateOperation(operation);

        try {
            System.out.println("#1");
            AccessTokenResponse token = KeycloakREST.getToken();
            System.out.println("#2");
            String path = operation.get(PATH.getName()).asString();
            HttpMethod method = HttpMethod.valueOf(operation.get(METHOD.getName()).asString());

            String jsonString = KeycloakREST.submit(token, method, path);
            ModelNode result = ModelNode.fromJSONString(jsonString);
            System.out.println("#3");
            context.getResult().set(result);

        } catch (IOException | Failure e) {
            throw new OperationFailedException(e);
        }
    }

}
