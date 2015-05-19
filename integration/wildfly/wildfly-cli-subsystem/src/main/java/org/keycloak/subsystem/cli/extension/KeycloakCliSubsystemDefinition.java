package org.keycloak.subsystem.cli.extension;

import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.registry.ManagementResourceRegistration;

/**
 * @author Stan Silvert ssilvert@redhat.com (C) 2015 Red Hat Inc.
 */
public class KeycloakCliSubsystemDefinition extends SimpleResourceDefinition {
    public static final KeycloakCliSubsystemDefinition INSTANCE = new KeycloakCliSubsystemDefinition();

    private KeycloakCliSubsystemDefinition() {
        super(KeycloakCliSubsystemExtension.SUBSYSTEM_PATH,
                KeycloakCliSubsystemExtension.getResourceDescriptionResolver(null),
                //We always need to add an 'add' operation
                KeycloakCliSubsystemAdd.INSTANCE,
                //Every resource that is added, normally needs a remove operation
                KeycloakCliSubsystemRemove.INSTANCE);
    }

    @Override
    public void registerOperations(ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);
        resourceRegistration.registerOperationHandler(SubmitHandler.DEFINITION, SubmitHandler.INSTANCE);
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        //you can register attributes here
    }
}
