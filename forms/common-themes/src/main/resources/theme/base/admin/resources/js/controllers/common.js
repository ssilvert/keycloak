module.controller('RoleSelectorModalCtrl', function($scope, realm, config, configName, RealmRoles, Client, ClientRole, $modalInstance) {
    $scope.selectedRealmRole = {
        role: undefined
    };
    $scope.selectedClientRole = {
        role: undefined
    };
    $scope.client = {
        selected: undefined
    };

    $scope.selectRealmRole = function() {
        config[configName] = $scope.selectedRealmRole.role.name;
        $modalInstance.close();
    }

    $scope.selectClientRole = function() {
        config[configName] = $scope.client.selected.clientId + "." + $scope.selectedClientRole.role.name;
        $modalInstance.close();
    }

    $scope.cancel = function() {
        $modalInstance.dismiss();
    }

    $scope.changeClient = function() {
        if ($scope.client.selected) {
            ClientRole.query({realm: realm.realm, client: $scope.client.selected.id}, function (data) {
                $scope.clientRoles = data;
             });
        } else {
            console.log('selected client was null');
            $scope.clientRoles = null;
        }

    }
    RealmRoles.query({realm: realm.realm}, function(data) {
        $scope.realmRoles = data;
    })
    Client.query({realm: realm.realm}, function(data) {
        $scope.clients = data;
        if (data.length > 0) {
            $scope.client.selected = data[0];
            $scope.changeClient();
        }
    })
});

module.controller('ProviderConfigCtrl', function ($modal, $scope) {
    $scope.openRoleSelector = function (configName, config) {
        $modal.open({
            templateUrl: resourceUrl + '/partials/modal/role-selector.html',
            controller: 'RoleSelectorModalCtrl',
            resolve: {
                realm: function () {
                    return $scope.realm;
                },
                config: function () {
                    return config;
                },
                configName: function () {
                    return configName;
                }
            }
        })
    }
});

module.controller('PartialImportCtrl', function($scope, realm, section, sectionName, resourceName, $location, $route, 
                                                overwriteEnabled, Notifications, $modal, $resource) {
    $scope.fileContent = {
        enabled: true
    };
    $scope.section = section;
    $scope.sectionName = sectionName;
    $scope.overwriteEnabled = overwriteEnabled;
    $scope.changed = false;
    $scope.files = [];
    $scope.realm = realm;
    $scope.overwrite = false;
    $scope.skip = false;
    
    var oldCopy = angular.copy($scope.fileContent);

    $scope.importFile = function($fileContent){
        $scope.fileContent = angular.copy(JSON.parse($fileContent));
        $scope.importing = true;
    };

    $scope.viewImportDetails = function() {
        $modal.open({
            templateUrl: resourceUrl + '/partials/modal/view-object.html',
            controller: 'ObjectModalCtrl',
            resolve: {
                object: function () {
                    return $scope.fileContent;
                }
            }
        })
    };
    
    $scope.$watch('fileContent', function() {
        if (!angular.equals($scope.fileContent, oldCopy)) {
            $scope.changed = true;
        }
    }, true);
    
    $scope.$watch('overwrite', function() {
        if ($scope.overwrite) $scope.skip = false;
    });
    
    $scope.$watch('skip', function() {
        if ($scope.skip) $scope.overwrite = false;
    });
    
    $scope.save = function() {
        var json = angular.copy($scope.fileContent);
        json.overwrite = $scope.overwrite;
        json.skip = $scope.skip;
        var importFile = $resource(authUrl + '/admin/realms/' + realm.realm + '/' + resourceName + '/import');
        importFile.save(json, function() {
            Notifications.success('The ' + sectionName + ' have been imported.');
        }, function(error) {
            if (error.data.errorMessage) {
                Notifications.error(error.data.errorMessage);
            } else {
                Notifications.error('Unexpected error during import');
            }
        });
    };
    
    $scope.cancel = function() {
        $location.url("/realms/" + realm.realm + "/" + section);
    };
    
    $scope.reset = function() {
        $route.reload();
    }

});

module.controller('RealmImportCtrl', function($scope, realm, $route, 
                                              Notifications, $modal, $resource) {
    $scope.rawContent = {};
    $scope.fileContent = {
        enabled: true
    };
    $scope.changed = false;
    $scope.files = [];
    $scope.realm = realm;
    $scope.overwrite = false;
    $scope.skip = false;
    $scope.importUsers = false;
    $scope.importClients = false;
    $scope.importIdentityProviders = false;
    $scope.importRealmRoles = false;
    $scope.importClientRoles = false;
    $scope.ifResourceExists='FAIL';
    $scope.isMultiRealm = false;
    $scope.results = {};
    
    var oldCopy = angular.copy($scope.fileContent);

    $scope.importFile = function($fileContent){
        var parsed;
        try {
            parsed = JSON.parse($fileContent);
        } catch (e) {
            Notifications.error('Unable to parse JSON file.');
            return;
        }
        
        $scope.rawContent = angular.copy(parsed);
        if (($scope.rawContent instanceof Array) && ($scope.rawContent.length > 0)) {
            if ($scope.rawContent.length > 1) $scope.isMultiRealm = true;
            $scope.fileContent = $scope.rawContent[0];
        } else {
            $scope.fileContent = $scope.rawContent;
        }
        
        $scope.importing = true;
        $scope.importUsers = $scope.hasArray('users');
        $scope.importClients = $scope.hasArray('clients');
        $scope.importIdentityProviders = $scope.hasArray('identityProviders');
        $scope.importRealmRoles = $scope.hasRealmRoles();
        $scope.importClientRoles = $scope.hasClientRoles();
        $scope.results = {};
        if (!$scope.hasResources()) {
            $scope.nothingToImport();
        }
    };

    $scope.hasResults = function() {
        return (Object.keys($scope.results).length > 0) &&
                ($scope.results.results !== 'undefined') &&
                ($scope.results.results.length > 0);
    }
    
    $scope.viewImportDetails = function() {
        $modal.open({
            templateUrl: resourceUrl + '/partials/modal/view-object.html',
            controller: 'ObjectModalCtrl',
            resolve: {
                object: function () {
                    return $scope.fileContent;
                }
            }
        })
    };
    
    $scope.hasArray = function(section) {
        return ($scope.fileContent !== 'undefined') &&
               ($scope.fileContent.hasOwnProperty(section)) &&
               ($scope.fileContent[section] instanceof Array) &&
               ($scope.fileContent[section].length > 0);
    }
    
    $scope.hasRealmRoles = function() {
        return $scope.hasRoles() &&
               ($scope.fileContent.roles.hasOwnProperty('realm')) &&
               ($scope.fileContent.roles.realm instanceof Array) &&
               ($scope.fileContent.roles.realm.length > 0);
    }
    
    $scope.hasRoles = function() {
        return ($scope.fileContent !== 'undefined') &&
               ($scope.fileContent.hasOwnProperty('roles')) &&
               ($scope.fileContent.roles !== 'undefined');
    }
    
    $scope.hasClientRoles = function() {
        return $scope.hasRoles() &&
               ($scope.fileContent.roles.hasOwnProperty('client')) &&
               (Object.keys($scope.fileContent.roles.client).length > 0);
    }
    
    $scope.itemCount = function(section) {
        if (!$scope.importing) return 0;
        if ($scope.hasRealmRoles() && (section === 'roles.realm')) return $scope.fileContent.roles.realm.length;
        if ($scope.hasClientRoles() && (section === 'roles.client')) return Object.keys($scope.fileContent.roles.client).length;
        
        if (!$scope.fileContent.hasOwnProperty(section)) return 0;
        
        return $scope.fileContent[section].length;
    }
    
    $scope.hasResources = function() {
        return ($scope.importUsers && $scope.hasArray('users')) ||
               ($scope.importClients && $scope.hasArray('clients')) ||
               ($scope.importIdentityProviders && $scope.hasArray('identityProviders')) ||
               ($scope.importRealmRoles && $scope.hasRealmRoles()) ||
               ($scope.importClientRoles && $scope.hasClientRoles());
    }
    
    $scope.nothingToImport = function() {
        Notifications.error('No resouces specified to import.');
    }
    
    $scope.$watch('fileContent', function() {
        if (!angular.equals($scope.fileContent, oldCopy)) {
            $scope.changed = true;
        }
    }, true);
    
    $scope.save = function() {
        var json = angular.copy($scope.fileContent);
        json.ifResourceExists = $scope.ifResourceExists;
        if (!$scope.importUsers) delete json.users;
        if (!$scope.importIdentityProviders) delete json.identityProviders;
        if (!$scope.importClients) delete json.clients;
        
        if (json.hasOwnProperty('roles')) {
            if (!$scope.importRealmRoles) delete json.roles.realm;
            if (!$scope.importClientRoles) delete json.roles.client;
        }
        
        var importFile = $resource(authUrl + '/admin/realms/' + realm.realm + '/partialImport');
        $scope.results = importFile.save(json, function() {
            var message = $scope.results.added + ' records added. ';
            if ($scope.ifResourceExists === 'SKIP') {
                message += $scope.results.skipped + ' records skipped.'
            }
            if ($scope.ifResourceExists === 'OVERWRITE') {
                message += $scope.results.overwritten + ' records overwritten.';
            }
            Notifications.success(message);
        }, function(error) {
            if (error.data.errorMessage) {
                Notifications.error(error.data.errorMessage);
            } else {
                Notifications.error('Unexpected error during import');
            }
        });
    };
    
    $scope.reset = function() {
        $route.reload();
    }

});

module.controller('PartialExportCtrl', function($scope, realm, section, sectionName, resourceName, propertyName, searchEnabled,
                                                $location, Notifications, $resource, LatestQuery) {
    $scope.section = section;
    $scope.sectionName = sectionName;
    $scope.searchEnabled = searchEnabled;
    $scope.realm = realm;
    $scope.searchQuery = LatestQuery.get();
    $scope.fileName = "keycloak-" + propertyName;
    $scope.condensed = false;
    $scope.exported = {};

    $scope.cancel = function() {
        $location.url("/realms/" + realm.realm + "/" + section);
    };
    
    $scope.localExport = function() {
        var isArray;
        if ($scope.section !== 'roles') {
            isArray = {get: {isArray:true}};
        } else {
            isArray = {};
        }
        
        var exportResource = $resource(authUrl + '/admin/realms/' + realm.realm + '/' + resourceName + '/localExport', 
                                       {}, isArray);
        var json = exportResource.get({search: $scope.searchQuery}, function() {
            $scope.exported[propertyName] = json;
            var blob = new Blob([angular.toJson($scope.exported,!$scope.condensed)], { type: 'application/json' });
            saveAs(blob, $scope.fileName + ".json");
            Notifications.success('The ' + sectionName + ' have been exported.');
        }, function(error) {
            if (error.data.errorMessage) {
                Notifications.error(error.data.errorMessage);
            } else {
                Notifications.error('Unexpected error during export');
            }
        });
    }
    
    $scope.serverExport = function() {
        var exportResource = $resource(authUrl + '/admin/realms/' + realm.realm + '/' + resourceName + '/serverExport');
        exportResource.get({search: $scope.searchQuery, fileName: $scope.fileName, condensed: $scope.condensed}, function() {
           Notifications.success($scope.sectionName + ' saved on the server.');
        }, function(error) {
            if (error.data.errorMessage) {
                Notifications.error(error.data.errorMessage);
            } else {
                Notifications.error('Unexpected error during export');
            }
        });
    }
});