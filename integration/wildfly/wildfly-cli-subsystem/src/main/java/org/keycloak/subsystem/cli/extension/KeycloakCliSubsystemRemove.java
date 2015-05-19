package org.keycloak.subsystem.cli.extension;

import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;

/**
 * Handler responsible for removing the subsystem resource from the model
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2015 Red Hat Inc.
 */
class KeycloakCliSubsystemRemove extends AbstractRemoveStepHandler {

    static final KeycloakCliSubsystemRemove INSTANCE = new KeycloakCliSubsystemRemove();


    private KeycloakCliSubsystemRemove() {
    }

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
        //Remove any services installed by the corresponding add handler here
        //context.removeService(ServiceName.of("some", "name"));
    }


}
