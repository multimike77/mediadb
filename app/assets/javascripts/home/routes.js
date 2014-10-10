/**
 * Home routes.
 */
define(['angular', 'common'], function(angular) {
  'use strict';

  var mod = angular.module('home.routes', ['yourprefix.common']);
  mod.config(['$routeProvider', '$locationProvider', function($routeProvider, $locationProvider) {
    $routeProvider
      //.when('/',  {templateUrl: '/assets/javascripts/home/home.html', controller:controllers.HomeCtrl})
        .when('/', {redirectTo: '/movies'})
      .otherwise( {templateUrl: '/assets/javascripts/home/notFound.html'});

      $locationProvider.html5Mode(true);
      $locationProvider.hashPrefix('!');
  }]);
  return mod;
});
