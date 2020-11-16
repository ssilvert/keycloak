/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.storage.user;

import org.keycloak.models.GroupModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Optional capability interface implemented by UserStorageProviders.
 * Defines complex queries that are used to locate one or more users.  You must implement this interface
 * if you want to view and manager users from the administration console.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface UserQueryProvider {

    /**
     * Returns the number of users, without consider any service account.
     *
     * @param realm the realm
     * @return the number of users
     */
    int getUsersCount(RealmModel realm);

    /**
     * Returns the number of users that are in at least one of the groups
     * given.
     *
     * @param realm    the realm
     * @param groupIds set of groups id to check for
     * @return the number of users that are in at least one of the groups
     */
    default int getUsersCount(RealmModel realm, Set<String> groupIds) {
        if (groupIds == null || groupIds.isEmpty()) {
            return 0;
        }
        return countUsersInGroups(getUsersStream(realm), groupIds);
    }

    /**
     * Returns the number of users that match the given criteria.
     *
     * @param search search criteria
     * @param realm  the realm
     * @return number of users that match the search
     */
    default int getUsersCount(String search, RealmModel realm) {
        return (int) searchForUserStream(search, realm).count();
    }

    /**
     * Returns the number of users that match the given criteria and are in
     * at least one of the groups given.
     *
     * @param search   search criteria
     * @param realm    the realm
     * @param groupIds set of groups to check for
     * @return number of users that match the search and given groups
     */
    default int getUsersCount(String search, RealmModel realm, Set<String> groupIds) {
        if (groupIds == null || groupIds.isEmpty()) {
            return 0;
        }
        return countUsersInGroups(searchForUserStream(search, realm), groupIds);
    }

    /**
     * Returns the number of users that match the given filter parameters.
     *
     * @param params filter parameters
     * @param realm  the realm
     * @return number of users that match the given filters
     */
    default int getUsersCount(Map<String, String> params, RealmModel realm) {
        return (int) searchForUserStream(params, realm).count();
    }

    /**
     * Returns the number of users that match the given filter parameters and is in
     * at least one of the given groups.
     *
     * @param params   filter parameters
     * @param realm    the realm
     * @param groupIds set if groups to check for
     * @return number of users that match the given filters and groups
     */
    default int getUsersCount(Map<String, String> params, RealmModel realm, Set<String> groupIds) {
        if (groupIds == null || groupIds.isEmpty()) {
            return 0;
        }
        return countUsersInGroups(searchForUserStream(params, realm), groupIds);
    }

    /**
     * Returns the number of users from the given list of users that are in at
     * least one of the groups given in the groups set.
     *
     * @param users    list of users to check
     * @param groupIds id of groups that should be checked for
     * @return number of users that are in at least one of the groups
     */
    static int countUsersInGroups(Stream<UserModel> users, Set<String> groupIds) {
        return (int) users.filter(u -> u.getGroupsStream().map(GroupModel::getId).anyMatch(groupIds::contains)).count();
    }

    /**
     * Returns the number of users.
     *
     * @param realm the realm
     * @param includeServiceAccount if true, the number of users will also include service accounts. Otherwise, only the number of users.
     * @return the number of users
     */
    default int getUsersCount(RealmModel realm, boolean includeServiceAccount) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * @deprecated Use {@link #getUsersStream(RealmModel) getUsersStream} instead.
     */
    @Deprecated
    default List<UserModel> getUsers(RealmModel realm) {
        return this.getUsersStream(realm).collect(Collectors.toList());
    }

    /**
     * Searches all users in the realm.
     *
     * @param realm a reference to the realm.
     * @return a non-null {@code Stream} of users.
     */
    Stream<UserModel> getUsersStream(RealmModel realm);

    /**
     * @deprecated Use {@link #getUsersStream(RealmModel, int, int) getUsersStream} instead.
     */
    @Deprecated
    default List<UserModel> getUsers(RealmModel realm, int firstResult, int maxResults) {
        return this.getUsersStream(realm, firstResult, maxResults).collect(Collectors.toList());
    }

    /**
     * Searches all users in the realm, starting from the {@code firstResult} and containing at most {@code maxResults}.
     *
     * @param realm a reference to the realm.
     * @param firstResult first result to return. Ignored if negative.
     * @param maxResults maximum number of results to return. Ignored if negative.
     * @return a non-null {@code Stream} of users.
     */
    Stream<UserModel> getUsersStream(RealmModel realm, int firstResult, int maxResults);

    /**
     * Search for users with username, email or first + last name that is like search string.
     *
     * If possible, implementations should treat the parameter values as partial match patterns i.e. in RDMBS terms use LIKE.
     *
     * This method is used by the admin console search box
     *
     * @param search
     * @param realm
     * @return
     * @deprecated Use {@link #searchForUserStream(String, RealmModel) searchForUserStream} instead.
     */
    @Deprecated
    default List<UserModel> searchForUser(String search, RealmModel realm) {
        return this.searchForUserStream(search, realm).collect(Collectors.toList());
    }

    /**
     * Searches for users with username, email or first + last name that is like search string.  If possible, implementations
     * should treat the parameter values as partial match patterns (i.e. in RDMBS terms use LIKE).
     * <p/>
     * This method is used by the admin console search box
     *
     * @param search case sensitive search string.
     * @param realm a reference to the realm.
     * @return a non-null {@code Stream} of users that match the search string.
     */
    Stream<UserModel> searchForUserStream(String search, RealmModel realm);

    /**
     * Search for users with username, email or first + last name that is like search string.
     *
     * If possible, implementations should treat the parameter values as partial match patterns i.e. in RDMBS terms use LIKE.
     *
     * This method is used by the admin console search box
     *
     * @param search
     * @param realm
     * @param firstResult
     * @param maxResults
     * @return
     * @deprecated Use {@link #searchForUserStream(String, RealmModel, int, int) searchForUserStream} instead.
     */
    @Deprecated
    default List<UserModel> searchForUser(String search, RealmModel realm, int firstResult, int maxResults) {
        return this.searchForUserStream(search, realm, firstResult, maxResults).collect(Collectors.toList());
    }

    /**
     * Searches for users with username, email or first + last name that is like search string. If possible, implementations
     * should treat the parameter values as partial match patterns (i.e. in RDMBS terms use LIKE).
     * <p/>
     * This method is used by the admin console search box
     *
     * @param search case sensitive search string.
     * @param realm a reference to the realm.
     * @param firstResult first result to return. Ignored if negative.
     * @param maxResults maximum number of results to return. Ignored if negative.
     * @return a non-null {@code Stream} of users that match the search criteria.
     */
    Stream<UserModel> searchForUserStream(String search, RealmModel realm, int firstResult, int maxResults);

    /**
     * Search for user by parameter.  Valid parameters are:
     * "first" - first name
     * "last" - last name
     * "email" - email
     * "username" - username
     *
     * If possible, implementations should treat the parameter values as partial match patterns i.e. in RDMBS terms use LIKE.
     *
     * This method is used by the REST API when querying users.
     *
     *
     * @param params
     * @param realm
     * @return
     * @deprecated Use {@link #searchForUserStream(Map, RealmModel) searchForUserStream} instead.
     */
    @Deprecated
    default List<UserModel> searchForUser(Map<String, String> params, RealmModel realm) {
        return this.searchForUserStream(params, realm).collect(Collectors.toList());
    }

    /**
     * Searches for user by parameter. If possible, implementations should treat the parameter values as partial match patterns
     * (i.e. in RDMBS terms use LIKE). Valid parameters are:
     * <ul>
     *   <li><b>first</b> - first name</li>
     *   <li><b>last</b> - last name</li>
     *   <li><b>email</b> - email</li>
     *   <li><b>username</b> - username</li>
     *   <li><b>enabled</b> - if user is enabled (true/false)</li>
     * </ul>
     * This method is used by the REST API when querying users.
     *
     * @param params a map containing the search parameters.
     * @param realm a reference to the realm.
     * @return a non-null {@code Stream} of users that match the search parameters.
     */
    Stream<UserModel> searchForUserStream(Map<String, String> params, RealmModel realm);

    /**
     * Search for user by parameter.    Valid parameters are:
     * "first" - first name
     * "last" - last name
     * "email" - email
     * "username" - username
     * "enabled" - is user enabled (true/false)
     *
     * If possible, implementations should treat the parameter values as patterns i.e. in RDMBS terms use LIKE.
     * This method is used by the REST API when querying users.
     *
     *
     * @param params
     * @param realm
     * @param firstResult
     * @param maxResults
     * @return
     * @deprecated Use {@link #searchForUserStream(Map, RealmModel, int, int) searchForUserStream} instead.
     */
    @Deprecated
    default List<UserModel> searchForUser(Map<String, String> params, RealmModel realm, int firstResult, int maxResults) {
        return this.searchForUserStream(params, realm, firstResult, maxResults).collect(Collectors.toList());
    }

    /**
     * Searches for user by parameter. If possible, implementations should treat the parameter values as partial match patterns
     * (i.e. in RDMBS terms use LIKE). Valid parameters are:
     * <ul>
     *   <li><b>first</b> - first name</li>
     *   <li><b>last</b> - last name</li>
     *   <li><b>email</b> - email</li>
     *   <li><b>username</b> - username</li>
     *   <li><b>enabled</b> - if user is enabled (true/false)</li>
     * </ul>
     * This method is used by the REST API when querying users.
     *
     * @param params a map containing the search parameters.
     * @param realm a reference to the realm.
     * @param firstResult first result to return. Ignored if negative.
     * @param maxResults maximum number of results to return. Ignored if negative.
     * @return a non-null {@code Stream} of users that match the search criteria.
     */
    Stream<UserModel> searchForUserStream(Map<String, String> params, RealmModel realm, int firstResult, int maxResults);

    /**
     * Get users that belong to a specific group. Implementations do not have to search in UserFederatedStorageProvider
     * as this is done automatically.
     *
     * @see org.keycloak.storage.federated.UserFederatedStorageProvider
     *
     * @param realm
     * @param group
     * @return
     * @deprecated Use {@link #getGroupMembersStream(RealmModel, GroupModel) getGroupMembersStream} instead.
     */
    @Deprecated
    default List<UserModel> getGroupMembers(RealmModel realm, GroupModel group) {
        return this.getGroupMembersStream(realm, group).collect(Collectors.toList());
    }

    /**
     * Obtains users that belong to a specific group. Implementations do not have to search in {@code UserFederatedStorageProvider}
     * as this is done automatically.
     *
     * @see org.keycloak.storage.federated.UserFederatedStorageProvider
     *
     * @param realm a reference to the realm.
     * @param group a reference to the group.
     * @return a non-null {@code Stream} of users that belong to the group.
     */
    Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel group);

    /**
     * Get users that belong to a specific group.  Implementations do not have to search in UserFederatedStorageProvider
     * as this is done automatically.
     *
     * @see org.keycloak.storage.federated.UserFederatedStorageProvider
     *
     * @param realm
     * @param group
     * @param firstResult
     * @param maxResults
     * @return
     * @deprecated Use {@link #getGroupMembersStream(RealmModel, GroupModel, int, int) getGroupMembersStream} instead.
     */
    @Deprecated
    default List<UserModel> getGroupMembers(RealmModel realm, GroupModel group, int firstResult, int maxResults) {
        return this.getGroupMembersStream(realm, group, firstResult, maxResults).collect(Collectors.toList());
    }

    /**
     * Obtains users that belong to a specific group.  Implementations do not have to search in {@code UserFederatedStorageProvider}
     * as this is done automatically.
     *
     * @see org.keycloak.storage.federated.UserFederatedStorageProvider
     *
     * @param realm a reference to the realm.
     * @param group a reference to the group.
     * @param firstResult first result to return. Ignored if negative.
     * @param maxResults maximum number of results to return. Ignored if negative.
     * @return a non-null {@code Stream} of users that belong to the group.
     */
    Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel group, int firstResult, int maxResults);

    /**
     * Get users that belong to a specific role.
     *
     * @param realm
     * @param role
     * @return
     * @deprecated Use {@link #getRoleMembersStream(RealmModel, RoleModel) getRoleMembersStream} instead.
     */
    @Deprecated
    default List<UserModel> getRoleMembers(RealmModel realm, RoleModel role) {
        return this.getRoleMembersStream(realm, role).collect(Collectors.toList());
    }

    /**
     * Obtains users that have the specified role.
     *
     * @param realm a reference to the realm.
     * @param role a reference to the role.
     * @return a non-null {@code Stream} of users that have the specified role.
     */
    default Stream<UserModel> getRoleMembersStream(RealmModel realm, RoleModel role) {
        return Stream.empty();
    }

    /**
     * Search for users that have a specific role with a specific roleId.
     *
     * @param firstResult
     * @param maxResults
     * @param role
     * @return
     * @deprecated Use {@link #getRoleMembersStream(RealmModel, RoleModel, int, int) getRoleMembersStream} instead.
     */
    @Deprecated
    default List<UserModel> getRoleMembers(RealmModel realm, RoleModel role, int firstResult, int maxResults) {
        return this.getRoleMembersStream(realm, role, firstResult, maxResults).collect(Collectors.toList());
    }

    /**
     * Searches for users that have the specified role.
     *
     * @param realm a reference to the realm.
     * @param role a reference to the role.
     * @param firstResult first result to return. Ignored if negative.
     * @param maxResults maximum number of results to return. Ignored if negative.
     * @return a non-null {@code Stream} of users that have the specified role.
     */
    default Stream<UserModel> getRoleMembersStream(RealmModel realm, RoleModel role, int firstResult, int maxResults) {
        return Stream.empty();
    }

    /**
     * Search for users that have a specific attribute with a specific value.
     * Implementations do not have to search in UserFederatedStorageProvider
     * as this is done automatically.
     *
     * @see org.keycloak.storage.federated.UserFederatedStorageProvider
     *
     * @param attrName
     * @param attrValue
     * @param realm
     * @return
     * @deprecated Use {@link #searchForUserByUserAttributeStream(String, String, RealmModel) searchForUserByUserAttributeStream}
     * instead.
     */
    @Deprecated
    default List<UserModel> searchForUserByUserAttribute(String attrName, String attrValue, RealmModel realm) {
        return this.searchForUserByUserAttributeStream(attrName, attrValue, realm).collect(Collectors.toList());
    }

    /**
     * Searches for users that have a specific attribute with a specific value. Implementations do not have to search in
     * {@code UserFederatedStorageProvider} as this is done automatically.
     *
     * @see org.keycloak.storage.federated.UserFederatedStorageProvider
     *
     * @param attrName the attribute name.
     * @param attrValue the attribute value.
     * @param realm a reference to the realm.
     * @return a non-null {@code Stream} of users that match the search criteria.
     */
    Stream<UserModel> searchForUserByUserAttributeStream(String attrName, String attrValue, RealmModel realm);
}
