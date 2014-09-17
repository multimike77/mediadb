/**
 * TV Show routes.
 */
define(['angular', './controllers', 'common'], function (angular, controllers) {
    'use strict';

    var mod = angular.module('tvshows.routes', ['yourprefix.common']);
    mod.config(['$routeProvider', function ($routeProvider) {
        $routeProvider
            .when('/tv',
            {
                templateUrl: '/assets/javascripts/tvshows/tvshows.html',
                controller: controllers.TVShowCtrl
            }
        )
            .when('/tv/:showName',
            {
                templateUrl: '/assets/javascripts/tvshows/tvshow-details.html',
                controller: controllers.TVShowDetailsCtrl
            }
        );
    }]);
    return mod;
});
