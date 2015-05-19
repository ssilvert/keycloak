package org.keycloak.subsystem.cli.extension;

import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.server.AbstractDeploymentChainStep;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.dmr.ModelNode;

/**
 * Handler responsible for adding the subsystem resource to the model
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2015 Red Hat Inc.
 */
class KeycloakCliSubsystemAdd extends AbstractBoottimeAddStepHandler {

    static final KeycloakCliSubsystemAdd INSTANCE = new KeycloakCliSubsystemAdd();

    private KeycloakCliSubsystemAdd() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performBoottime(final OperationContext context, ModelNode operation, final ModelNode model) {
        context.addStep(new AbstractDeploymentChainStep() {
            @Override
            protected void execute(DeploymentProcessorTarget processorTarget) {

            }
        }, OperationContext.Stage.RUNTIME);
    }
}
