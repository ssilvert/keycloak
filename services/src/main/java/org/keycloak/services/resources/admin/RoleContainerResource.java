package org.keycloak.services.resources.admin;

import java.io.IOException;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.spi.NotFoundException;
import org.keycloak.events.admin.OperationType;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleContainerModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.services.ErrorResponse;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.QueryParam;
import org.keycloak.exportimport.PartialExportUtil;
import org.keycloak.exportimport.util.ExportUtils;
import org.keycloak.models.utils.RepresentationToModel;
import org.keycloak.representations.idm.PartialImport;
import org.keycloak.representations.idm.RolesRepresentation;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class RoleContainerResource extends RoleResource {
    private final RealmModel realm;
    private final RealmAuth auth;
    protected RoleContainerModel roleContainer;
    private AdminEventBuilder adminEvent;
    private UriInfo uriInfo;

    public RoleContainerResource(UriInfo uriInfo, RealmModel realm, RealmAuth auth, RoleContainerModel roleContainer, AdminEventBuilder adminEvent) {
        super(realm);
        this.uriInfo = uriInfo;
        this.realm = realm;
        this.auth = auth;
        this.roleContainer = roleContainer;
        this.adminEvent = adminEvent;
    }

    @Path("serverExport")
    @GET
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    public void serverExport(@QueryParam("fileName") String fileName,
                             @QueryParam("condensed") boolean condensed) throws IOException {
        auth.requireView();

        RolesRepresentation roles = getRolesForExport();
        PartialExportUtil.serverExport("roles", roles, fileName, condensed, realm);
    }

    private RolesRepresentation getRolesForExport() {
        List<ClientModel> allClients = realm.getClients();
        return ExportUtils.getAllRoles(realm, allClients);
    }

    @Path("localExport")
    @GET
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    public RolesRepresentation localExport() throws IOException {
        auth.requireView();

        return getRolesForExport();
    }

    /**
     * Import Roles from a JSON file.
     *
     * @param uriInfo
     * @param roleImports
     * @return
     */
    @Path("import")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response importRoles(final @Context UriInfo uriInfo, PartialImport roleImports) {
        auth.requireManage();

        boolean skip = roleImports.isSkip();

        // check all constraints before mass import
        RolesRepresentation rolesRep = roleImports.getRoles();
        if (rolesRep == null) {
            return ErrorResponse.error("No roles to import.", Response.Status.INTERNAL_SERVER_ERROR);
        }

        List<RoleRepresentation> realmRoles = rolesRep.getRealm();
        if (realmRoles == null) {
            realmRoles = new ArrayList<>();
            rolesRep.setRealm(realmRoles);
        }
        Map<String, List<RoleRepresentation>> clientRoles = rolesRep.getClient();
        if (clientRoles == null) {
            clientRoles = new HashMap<>();
            rolesRep.setClient(clientRoles);
        }

        if (realmRoles.isEmpty() && clientRoles.isEmpty()) {
            return ErrorResponse.error("No roles to import.", Response.Status.INTERNAL_SERVER_ERROR);
        }

        // check realm roles
        Set<RoleModel> allRoles = roleContainer.getRoles();
        List<RoleRepresentation> realmRolesToSkip = new ArrayList<>();
        for (RoleRepresentation rep : realmRoles) {
            if (!realmRoleExists(rep, allRoles)) continue;

            if (skip) {
                realmRolesToSkip.add(rep);
            } else {
                return ErrorResponse.exists("Realm role name '" + rep.getName() + "' already exists");
            }
        }

        // remove skipped realm roles
        for (RoleRepresentation skipRole : realmRolesToSkip) {
            System.out.println("Skipping realm role " + skipRole.getName());
            rolesRep.getRealm().remove(skipRole);
        }

        // check client roles
        List<String> clientsToSkip = new ArrayList<>();
        Map<String, List<RoleRepresentation>> clientRolesToSkip = initClientRolesToSkip(clientRoles.keySet());
        for (String clientId : clientRoles.keySet()) {
            if (realm.getClientByClientId(clientId) == null) { // client doesn't exist
                if (skip) {
                    clientsToSkip.add(clientId);
                    continue;
                } else {
                    return ErrorResponse.error("Client " + clientId + " not found.", Response.Status.NOT_FOUND);
                }
            }

            for (RoleRepresentation roleRep : clientRoles.get(clientId)) {
                if (!clientRoleExists(clientId, roleRep)) continue;

                if (skip) {
                    clientRolesToSkip.get(clientId).add(roleRep);
                } else {
                    return ErrorResponse.exists("Client role " + roleRep.getName() + " for client " + clientId + " exists.");
                }
            }
        }

        // skip clients
        for (String clientId : clientsToSkip) {
            System.out.println("Skipping client " + clientId);
            rolesRep.getClient().remove(clientId);
        }

        // skip client roles
        for (String clientId : clientRolesToSkip.keySet()) {
            for (RoleRepresentation roleRep : clientRolesToSkip.get(clientId)) {
                System.out.println("Skipping client role " + roleRep.getName() + " of client " + clientId);
                rolesRep.getClient().get(clientId).remove(roleRep);
            }
        }

        RepresentationToModel.importRoles(rolesRep, realm);

        return Response.ok().build();
    }

    private boolean realmRoleExists(RoleRepresentation rep, Set<RoleModel> allRoles) {
        for (RoleModel role : allRoles) {
            if (rep.getName().equals(role.getName())) return true;
        }

        return false;
    }

    private boolean clientRoleExists(String clientId, RoleRepresentation roleRep) {
        ClientModel client = realm.getClientByClientId(clientId);
        if (client.getRole(roleRep.getName()) == null) return false;

        return true;
    }

    private Map<String, List<RoleRepresentation>> initClientRolesToSkip(Set<String> clientIds) {
        Map<String, List<RoleRepresentation>> rolesToSkip = new HashMap<>();
        for (String clientId : clientIds) {
            rolesToSkip.put(clientId, new ArrayList<RoleRepresentation>());
        }

        return rolesToSkip;
    }

    /**
     * Get all roles for the realm or client
     *
     * @return
     */
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public List<RoleRepresentation> getRoles() {
        auth.requireAny();

        Set<RoleModel> roleModels = roleContainer.getRoles();
        List<RoleRepresentation> roles = new ArrayList<RoleRepresentation>();
        for (RoleModel roleModel : roleModels) {
            roles.add(ModelToRepresentation.toRepresentation(roleModel));
        }
        return roles;
    }

    /**
     * Create a new role for the realm or client
     *
     * @param rep
     * @return
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createRole(final RoleRepresentation rep) {
        auth.requireManage();

        try {
            RoleModel role = roleContainer.addRole(rep.getName());
            role.setDescription(rep.getDescription());
            boolean scopeParamRequired = rep.isScopeParamRequired()==null ? false : rep.isScopeParamRequired();
            role.setScopeParamRequired(scopeParamRequired);

            adminEvent.operation(OperationType.CREATE).resourcePath(uriInfo, role.getId()).representation(rep).success();

            return Response.created(uriInfo.getAbsolutePathBuilder().path(role.getName()).build()).build();
        } catch (ModelDuplicateException e) {
            return ErrorResponse.exists("Role with name " + rep.getName() + " already exists");
        }
    }

    /**
     * Get a role by name
     *
     * @param roleName role's name (not id!)
     * @return
     */
    @Path("{role-name}")
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public RoleRepresentation getRole(final @PathParam("role-name") String roleName) {
        auth.requireView();

        RoleModel roleModel = roleContainer.getRole(roleName);
        if (roleModel == null) {
            throw new NotFoundException("Could not find role");
        }

        return getRole(roleModel);
    }

    /**
     * Delete a role by name
     *
     * @param roleName role's name (not id!)
     */
    @Path("{role-name}")
    @DELETE
    @NoCache
    public void deleteRole(final @PathParam("role-name") String roleName) {
        auth.requireManage();

        RoleRepresentation rep = getRole(roleName);
        RoleModel role = roleContainer.getRole(roleName);
        if (role == null) {
            throw new NotFoundException("Could not find role");
        }
        deleteRole(role);

        adminEvent.operation(OperationType.DELETE).resourcePath(uriInfo).success();

    }

    /**
     * Update a role by name
     *
     * @param roleName role's name (not id!)
     * @param rep
     * @return
     */
    @Path("{role-name}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateRole(final @PathParam("role-name") String roleName, final RoleRepresentation rep) {
        auth.requireManage();

        RoleModel role = roleContainer.getRole(roleName);
        if (role == null) {
            throw new NotFoundException("Could not find role");
        }
        try {
            updateRole(rep, role);

            adminEvent.operation(OperationType.UPDATE).resourcePath(uriInfo).representation(rep).success();

            return Response.noContent().build();
        } catch (ModelDuplicateException e) {
            return ErrorResponse.exists("Role with name " + rep.getName() + " already exists");
        }
    }

    /**
     * Add a composite to the role
     *
     * @param roleName role's name (not id!)
     * @param roles
     */
    @Path("{role-name}/composites")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void addComposites(final @PathParam("role-name") String roleName, List<RoleRepresentation> roles) {
        auth.requireManage();

        RoleModel role = roleContainer.getRole(roleName);
        if (role == null) {
            throw new NotFoundException("Could not find role");
        }
        addComposites(adminEvent, uriInfo, roles, role);
    }

    /**
     * Get composites of the role
     *
     * @param roleName role's name (not id!)
     * @return
     */
    @Path("{role-name}/composites")
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public Set<RoleRepresentation> getRoleComposites(final @PathParam("role-name") String roleName) {
        auth.requireManage();

        RoleModel role = roleContainer.getRole(roleName);
        if (role == null) {
            throw new NotFoundException("Could not find role");
        }
        return getRoleComposites(role);
    }

    /**
     * Get realm-level roles of the role's composite
     *
     * @param roleName role's name (not id!)
     * @return
     */
    @Path("{role-name}/composites/realm")
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public Set<RoleRepresentation> getRealmRoleComposites(final @PathParam("role-name") String roleName) {
        auth.requireManage();

        RoleModel role = roleContainer.getRole(roleName);
        if (role == null) {
            throw new NotFoundException("Could not find role");
        }
        return getRealmRoleComposites(role);
    }

    /**
     * An app-level roles for the specified app for the role's composite
     *
     * @param roleName role's name (not id!)
     * @param client
     * @return
     */
    @Path("{role-name}/composites/clients/{client}")
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public Set<RoleRepresentation> getClientRoleComposites(@Context final UriInfo uriInfo,
                                                                final @PathParam("role-name") String roleName,
                                                                final @PathParam("client") String client) {
        auth.requireManage();

        RoleModel role = roleContainer.getRole(roleName);
        if (role == null) {
            throw new NotFoundException("Could not find role");
        }
        ClientModel clientModel = realm.getClientById(client);
        if (client == null) {
            throw new NotFoundException("Could not find client");

        }
        return getClientRoleComposites(clientModel, role);
    }


    /**
     * Remove roles from the role's composite
     *
     * @param roleName role's name (not id!)
     * @param roles roles to remove
     */
    @Path("{role-name}/composites")
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    public void deleteComposites(
                                   final @PathParam("role-name") String roleName,
                                   List<RoleRepresentation> roles) {
        auth.requireManage();

        RoleModel role = roleContainer.getRole(roleName);
        if (role == null) {
            throw new NotFoundException("Could not find role");
        }
        deleteComposites(roles, role);
        adminEvent.operation(OperationType.DELETE).resourcePath(uriInfo).success();
    }

}
