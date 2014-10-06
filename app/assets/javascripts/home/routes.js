/**
 * Home routes.
 */
define(['angular', './controllers', 'common'], function(angular, controllers) {
  'use strict';

  var mod = angular.module('home.routes', ['yourprefix.common']);
  mod.config(['$routeProvider', '$locationProvider', function($routeProvider, $locationProvider) {
    $routeProvider
      .when('/',  {templateUrl: '/assets/javascripts/home/home.html', controller:controllers.HomeCtrl})
      .otherwise( {templateUrl: '/assets/javascripts/home/notFound.html'});

      $locationProvider.html5Mode(true);
      $locationProvider.hashPrefix('!');
  }]);
  return mod;
});
