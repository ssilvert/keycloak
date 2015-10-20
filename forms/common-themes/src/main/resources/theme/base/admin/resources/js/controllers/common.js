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

module.controller('PartialImportCtrl', function($scope, realm, section, sectionName, resourceName, $location, $route, Notifications, $modal, $resource) {
    $scope.fileContent = {
        enabled: true
    };
    $scope.section = section;
    $scope.sectionName = sectionName;
    $scope.changed = false;
    $scope.files = [];
    $scope.realm = realm;
    
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
    
    $scope.save = function() {
        var json = angular.copy($scope.fileContent);
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