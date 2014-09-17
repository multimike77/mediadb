/**
 * TV Show routes.
 */
define(['angular', './controllers', 'common'], function (angular, controllers) {
    'use strict';

    var mod = angular.module('movies.routes', ['yourprefix.common']);
    mod.config(['$routeProvider', function ($routeProvider) {
        $routeProvider
            .when('/movies', {

                templateUrl: '/assets/javascripts/movies/movies.html',
                controller: controllers.MovieCtrl}
            )
            .when('/movies/:movieName', {
                templateUrl: '/assets/javascripts/movies/movie-details.html',
                controller: controllers.MovieDetailsCtrl}
            );
    }]);
    return mod;
});
