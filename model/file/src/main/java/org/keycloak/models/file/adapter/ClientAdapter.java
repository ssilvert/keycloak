/*
 * Copyright 2015 Red Hat Inc. and/or its affiliates and other contributors
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
package org.keycloak.models.file.adapter;

import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientIdentityProviderMappingModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.keycloak.connections.file.InMemoryModel;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.UserModel;
import org.keycloak.models.entities.ClientEntity;
import org.keycloak.models.entities.ClientIdentityProviderMappingEntity;
import org.keycloak.models.entities.ProtocolMapperEntity;
import org.keycloak.models.entities.RoleEntity;
import org.keycloak.models.utils.KeycloakModelUtils;

/**
 * ApplicationModel used for JSON persistence.
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2015 Red Hat Inc.
 */
public class ClientAdapter implements ClientModel {

    private final RealmModel realm;
    private  KeycloakSession session;
    private final ClientEntity entity;
    private final InMemoryModel inMemoryModel;

    private final Map<String, RoleAdapter> allRoles = new HashMap<String, RoleAdapter>();
    private final Map<String, RoleModel> allScopeMappings = new HashMap<String, RoleModel>();

    public ClientAdapter(KeycloakSession session, RealmModel realm, ClientEntity entity, InMemoryModel inMemoryModel) {
        this.realm = realm;
        this.session = session;
        this.entity = entity;
        this.inMemoryModel = inMemoryModel;
    }

    @Override
    public void updateClient() {
        inMemoryModel.requestWrite(session);
    }

    @Override
    public String getId() {
        return entity.getId();
    }

    @Override
    public Set<String> getWebOrigins() {
        Set<String> result = new HashSet<String>();
        if (entity.getWebOrigins() != null) {
            result.addAll(entity.getWebOrigins());
        }
        return result;
    }

    @Override
    public void setWebOrigins(Set<String> webOrigins) {
        List<String> result = new ArrayList<String>();
        result.addAll(webOrigins);
        entity.setWebOrigins(result);
        inMemoryModel.requestWrite(session);
    }

    @Override
    public void addWebOrigin(String webOrigin) {
        Set<String> webOrigins = getWebOrigins();
        webOrigins.add(webOrigin);
        setWebOrigins(webOrigins);
        inMemoryModel.requestWrite(session);
    }

    @Override
    public void removeWebOrigin(String webOrigin) {
        Set<String> webOrigins = getWebOrigins();
        webOrigins.remove(webOrigin);
        setWebOrigins(webOrigins);
        inMemoryModel.requestWrite(session);
    }

    @Override
    public Set<String> getRedirectUris() {
        Set<String> result = new HashSet<String>();
        if (entity.getRedirectUris() != null) {
            result.addAll(entity.getRedirectUris());
        }
        return result;
    }

    @Override
    public void setRedirectUris(Set<String> redirectUris) {
        List<String> result = new ArrayList<String>();
        result.addAll(redirectUris);
        entity.setRedirectUris(result);
        inMemoryModel.requestWrite(session);
    }

    @Override
    public void addRedirectUri(String redirectUri) {
        if (entity.getRedirectUris().contains(redirectUri)) return;
        entity.getRedirectUris().add(redirectUri);
        inMemoryModel.requestWrite(session);
    }

    @Override
    public void removeRedirectUri(String redirectUri) {
        entity.getRedirectUris().remove(redirectUri);
        inMemoryModel.requestWrite(session);
    }

    @Override
    public boolean isEnabled() {
        return entity.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        entity.setEnabled(enabled);
        inMemoryModel.requestWrite(session);
    }

    @Override
    public boolean validateSecret(String secret) {
        return secret.equals(entity.getSecret());
    }

    @Override
    public String getSecret() {
        return entity.getSecret();
    }

    @Override
    public void setSecret(String secret) {
        entity.setSecret(secret);
        inMemoryModel.requestWrite(session);
    }

    @Override
    public boolean isPublicClient() {
        return entity.isPublicClient();
    }

    @Override
    public void setPublicClient(boolean flag) {
        entity.setPublicClient(flag);
        inMemoryModel.requestWrite(session);
    }


    @Override
    public boolean isFrontchannelLogout() {
        return entity.isFrontchannelLogout();
    }

    @Override
    public void setFrontchannelLogout(boolean flag) {
        entity.setFrontchannelLogout(flag);
        inMemoryModel.requestWrite(session);
    }

    @Override
    public boolean isFullScopeAllowed() {
        return entity.isFullScopeAllowed();
    }

    @Override
    public void setFullScopeAllowed(boolean value) {
        entity.setFullScopeAllowed(value);
        inMemoryModel.requestWrite(session);
    }

    @Override
    public RealmModel getRealm() {
        return realm;
    }

    @Override
    public int getNotBefore() {
        return entity.getNotBefore();
    }

    @Override
    public void setNotBefore(int notBefore) {
        entity.setNotBefore(notBefore);
        inMemoryModel.requestWrite(session);
    }

    @Override
    public Set<RoleModel> getScopeMappings() {
        return new HashSet<RoleModel>(allScopeMappings.values());
    }

    @Override
    public Set<RoleModel> getRealmScopeMappings() {
        Set<RoleModel> allScopes = getScopeMappings();

        Set<RoleModel> realmRoles = new HashSet<RoleModel>();
        for (RoleModel role : allScopes) {
            RoleAdapter roleAdapter = (RoleAdapter)role;
            if (roleAdapter.isRealmRole()) {
                realmRoles.add(role);
            }
        }
        return realmRoles;
    }

    @Override
    public void addScopeMapping(RoleModel role) {
        allScopeMappings.put(role.getId(), role);
        inMemoryModel.requestWrite(session);
    }

    @Override
    public void deleteScopeMapping(RoleModel role) {
        allScopeMappings.remove(role.getId());
        inMemoryModel.requestWrite(session);
    }

    @Override
    public String getProtocol() {
        return entity.getProtocol();
    }

    @Override
    public void setProtocol(String protocol) {
        entity.setProtocol(protocol);
        inMemoryModel.requestWrite(session);

    }

    @Override
    public void setAttribute(String name, String value) {
        entity.getAttributes().put(name, value);
        inMemoryModel.requestWrite(session);
    }

    @Override
    public void removeAttribute(String name) {
        entity.getAttributes().remove(name);
        inMemoryModel.requestWrite(session);
    }

    @Override
    public String getAttribute(String name) {
        return entity.getAttributes().get(name);
    }

    @Override
    public Map<String, String> getAttributes() {
        Map<String, String> copy = new HashMap<String, String>();
        copy.putAll(entity.getAttributes());
        return copy;
    }

    @Override
    public Set<ProtocolMapperModel> getProtocolMappers() {
        Set<ProtocolMapperModel> result = new HashSet<ProtocolMapperModel>();
        for (ProtocolMapperEntity entity : this.entity.getProtocolMappers()) {
            ProtocolMapperModel model = getProtocolMapperById(entity.getId());
            if (model != null) result.add(model);
        }
        return result;
    }

    @Override
    public ProtocolMapperModel addProtocolMapper(ProtocolMapperModel model) {
        if (getProtocolMapperByName(model.getProtocol(), model.getName()) != null) {
            throw new RuntimeException("protocol mapper name must be unique per protocol");
        }
        ProtocolMapperEntity entity = new ProtocolMapperEntity();
        entity.setId(KeycloakModelUtils.generateId());
        entity.setProtocol(model.getProtocol());
        entity.setName(model.getName());
        entity.setProtocolMapper(model.getProtocolMapper());
        entity.setConfig(model.getConfig());
        entity.setConsentRequired(model.isConsentRequired());
        entity.setConsentText(model.getConsentText());
        this.entity.getProtocolMappers().add(entity);
        inMemoryModel.requestWrite(session);
        return entityToModel(entity);
    }

    @Override
    public void removeProtocolMapper(ProtocolMapperModel mapping) {
        ProtocolMapperEntity toBeRemoved = null;
        for (ProtocolMapperEntity e : entity.getProtocolMappers()) {
            if (e.getId().equals(mapping.getId())) {
                toBeRemoved = e;
                break;
            }
        }

        entity.getProtocolMappers().remove(toBeRemoved);
        inMemoryModel.requestWrite(session);
    }

    @Override
    public void updateProtocolMapper(ProtocolMapperModel mapping) {
        ProtocolMapperEntity entity = getProtocolMapperEntityById(mapping.getId());
        entity.setProtocolMapper(mapping.getProtocolMapper());
        entity.setConsentRequired(mapping.isConsentRequired());
        entity.setConsentText(mapping.getConsentText());
        if (entity.getConfig() != null) {
            entity.getConfig().clear();
            entity.getConfig().putAll(mapping.getConfig());
        } else {
            entity.setConfig(mapping.getConfig());
        }
        inMemoryModel.requestWrite(session);
    }

    protected ProtocolMapperEntity getProtocolMapperEntityById(String id) {
        for (ProtocolMapperEntity e : entity.getProtocolMappers()) {
            if (e.getId().equals(id)) {
                return e;
            }
        }
        return null;
    }

    protected ProtocolMapperEntity getProtocolMapperEntityByName(String protocol, String name) {
        for (ProtocolMapperEntity e : entity.getProtocolMappers()) {
            if (e.getProtocol().equals(protocol) && e.getName().equals(name)) {
                return e;
            }
        }
        return null;

    }

    @Override
    public ProtocolMapperModel getProtocolMapperById(String id) {
        ProtocolMapperEntity entity = getProtocolMapperEntityById(id);
        if (entity == null) return null;
        return entityToModel(entity);
    }

    @Override
    public ProtocolMapperModel getProtocolMapperByName(String protocol, String name) {
        ProtocolMapperEntity entity = getProtocolMapperEntityByName(protocol, name);
        if (entity == null) return null;
        return entityToModel(entity);
    }

    protected ProtocolMapperModel entityToModel(ProtocolMapperEntity entity) {
        ProtocolMapperModel mapping = new ProtocolMapperModel();
        mapping.setId(entity.getId());
        mapping.setName(entity.getName());
        mapping.setProtocol(entity.getProtocol());
        mapping.setProtocolMapper(entity.getProtocolMapper());
        mapping.setConsentRequired(entity.isConsentRequired());
        mapping.setConsentText(entity.getConsentText());
        Map<String, String> config = new HashMap<String, String>();
        if (entity.getConfig() != null) config.putAll(entity.getConfig());
        mapping.setConfig(config);
        return mapping;
    }

    @Override
    public void updateIdentityProviders(List<ClientIdentityProviderMappingModel> identityProviders) {
        List<ClientIdentityProviderMappingEntity> stored = new ArrayList<ClientIdentityProviderMappingEntity>();

        for (ClientIdentityProviderMappingModel model : identityProviders) {
            ClientIdentityProviderMappingEntity entity = new ClientIdentityProviderMappingEntity();

            entity.setId(model.getIdentityProvider());
            entity.setRetrieveToken(model.isRetrieveToken());
            stored.add(entity);
        }

        entity.setIdentityProviders(stored);
        inMemoryModel.requestWrite(session);
    }

    @Override
    public List<ClientIdentityProviderMappingModel> getIdentityProviders() {
        List<ClientIdentityProviderMappingModel> models = new ArrayList<>();

        for (ClientIdentityProviderMappingEntity e : entity.getIdentityProviders()) {
            ClientIdentityProviderMappingModel model = new ClientIdentityProviderMappingModel();

            model.setIdentityProvider(e.getId());
            model.setRetrieveToken(e.isRetrieveToken());

            models.add(model);
        }

        return models;
    }

    @Override
    public boolean isAllowedRetrieveTokenFromIdentityProvider(String providerId) {
        for (ClientIdentityProviderMappingEntity identityProviderMappingModel : entity.getIdentityProviders()) {
            if (identityProviderMappingModel.getId().equals(providerId)) {
                return identityProviderMappingModel.isRetrieveToken();
            }
        }

        return false;
    }

    @Override
    public String getClientId() {
        return entity.getClientId();
    }

    @Override
    public void setClientId(String clientId) {
        if (appNameExists(clientId)) throw new ModelDuplicateException("Application named " + clientId + " already exists.");
        entity.setClientId(clientId);
        inMemoryModel.requestWrite(session);
    }

    private boolean appNameExists(String name) {
        for (ClientModel app : realm.getClients()) {
            if (app == this) continue;
            if (app.getClientId().equals(name)) return true;
        }

        return false;
    }

    @Override
    public boolean isSurrogateAuthRequired() {
        return entity.isSurrogateAuthRequired();
    }

    @Override
    public void setSurrogateAuthRequired(boolean surrogateAuthRequired) {
        entity.setSurrogateAuthRequired(surrogateAuthRequired);
        inMemoryModel.requestWrite(session);
    }

    @Override
    public String getManagementUrl() {
        return entity.getManagementUrl();
    }

    @Override
    public void setManagementUrl(String url) {
        entity.setManagementUrl(url);
        inMemoryModel.requestWrite(session);
    }

    @Override
    public void setBaseUrl(String url) {
        entity.setBaseUrl(url);
        inMemoryModel.requestWrite(session);
    }

    @Override
    public String getBaseUrl() {
        return entity.getBaseUrl();
    }

    @Override
    public boolean isBearerOnly() {
        return entity.isBearerOnly();
    }

    @Override
    public void setBearerOnly(boolean only) {
        entity.setBearerOnly(only);
        inMemoryModel.requestWrite(session);
    }

    @Override
    public boolean isConsentRequired() {
        return entity.isConsentRequired();
    }

    @Override
    public void setConsentRequired(boolean consentRequired) {
        entity.setConsentRequired(consentRequired);
        inMemoryModel.requestWrite(session);
    }

    @Override
    public boolean isDirectGrantsOnly() {
        return entity.isDirectGrantsOnly();
    }

    @Override
    public void setDirectGrantsOnly(boolean flag) {
        entity.setDirectGrantsOnly(flag);
        inMemoryModel.requestWrite(session);
    }

    @Override
    public RoleAdapter getRole(String name) {
        for (RoleAdapter role : allRoles.values()) {
            if (role.getName().equals(name)) return role;
        }
        return null;
    }

    @Override
    public RoleAdapter addRole(String name) {
        return this.addRole(KeycloakModelUtils.generateId(), name);
    }

    @Override
    public RoleAdapter addRole(String id, String name) {
        if (roleNameExists(name)) throw new ModelDuplicateException("Role named " + name + " already exists.");
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setId(id);
        roleEntity.setName(name);
        roleEntity.setClientId(getId());

        RoleAdapter role = new RoleAdapter(getRealm(), roleEntity, this, inMemoryModel);
        allRoles.put(id, role);

        inMemoryModel.requestWrite(session);
        return role;
    }

    private boolean roleNameExists(String name) {
        for (RoleModel role : allRoles.values()) {
            if (role.getName().equals(name)) return true;
        }

        return false;
    }

    @Override
    public boolean removeRole(RoleModel role) {
        boolean removed = (allRoles.remove(role.getId()) != null);

        // remove application roles from users
        for (UserModel user : inMemoryModel.getUsers(realm.getId())) {
            user.deleteRoleMapping(role);
        }

        // delete scope mappings from applications
        for (ClientModel app : realm.getClients()) {
            app.deleteScopeMapping(role);
        }

        // remove role from the realm
        realm.removeRole(role);

        this.deleteScopeMapping(role);

        inMemoryModel.requestWrite(session);
        return removed;
    }

    @Override
    public Set<RoleModel> getRoles() {
        return new HashSet(allRoles.values());
    }

    @Override
    public boolean hasScope(RoleModel role) {
        if (isFullScopeAllowed()) return true;
        Set<RoleModel> roles = getScopeMappings();
        if (roles.contains(role)) return true;

        for (RoleModel mapping : roles) {
            if (mapping.hasRole(role)) return true;
        }
        roles = getRoles();
        if (roles.contains(role)) return true;

        for (RoleModel mapping : roles) {
            if (mapping.hasRole(role)) return true;
        }
        return false;
    }

    @Override
    public Set<RoleModel> getClientScopeMappings(ClientModel client) {
        Set<RoleModel> allScopes = client.getScopeMappings();

        Set<RoleModel> appRoles = new HashSet<RoleModel>();
        for (RoleModel role : allScopes) {
            RoleAdapter roleAdapter = (RoleAdapter)role;
            if (getId().equals(roleAdapter.getRoleEntity().getClientId())) {
                appRoles.add(role);
            }
        }
        return appRoles;
    }

    @Override
    public List<String> getDefaultRoles() {
        return entity.getDefaultRoles();
    }

    @Override
    public void addDefaultRole(String name) {
        RoleModel role = getRole(name);
        if (role == null) {
            addRole(name);
        }

        List<String> defaultRoles = getDefaultRoles();
        if (defaultRoles.contains(name)) return;

        String[] defaultRoleNames = defaultRoles.toArray(new String[defaultRoles.size() + 1]);
        defaultRoleNames[defaultRoleNames.length - 1] = name;
        updateDefaultRoles(defaultRoleNames);
    }

    @Override
    public void updateDefaultRoles(String[] defaultRoles) {
        List<String> roleNames = new ArrayList<String>();
        for (String roleName : defaultRoles) {
            RoleModel role = getRole(roleName);
            if (role == null) {
                addRole(roleName);
            }

            roleNames.add(roleName);
        }

        entity.setDefaultRoles(roleNames);
        inMemoryModel.requestWrite(session);
    }

    @Override
    public int getNodeReRegistrationTimeout() {
        return entity.getNodeReRegistrationTimeout();
    }

    @Override
    public void setNodeReRegistrationTimeout(int timeout) {
        entity.setNodeReRegistrationTimeout(timeout);
        inMemoryModel.requestWrite(session);
    }

    @Override
    public Map<String, Integer> getRegisteredNodes() {
        return entity.getRegisteredNodes() == null ? Collections.<String, Integer>emptyMap() : Collections.unmodifiableMap(entity.getRegisteredNodes());
    }

    @Override
    public void registerNode(String nodeHost, int registrationTime) {
        if (entity.getRegisteredNodes() == null) {
            entity.setRegisteredNodes(new HashMap<String, Integer>());
        }

        entity.getRegisteredNodes().put(nodeHost, registrationTime);
        inMemoryModel.requestWrite(session);
    }

    @Override
    public void unregisterNode(String nodeHost) {
        if (entity.getRegisteredNodes() == null) return;

        entity.getRegisteredNodes().remove(nodeHost);
        inMemoryModel.requestWrite(session);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof ClientModel)) return false;

        ClientModel that = (ClientModel) o;
        return that.getId().equals(getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
