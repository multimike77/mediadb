/**
 * Dashboard controllers.
 */
define([], function() {
  'use strict';

  /**
   * user is not a service, but stems from userResolve (Check ../user/services.js) object used by dashboard.routes.
   */
  var DashboardCtrl = function($scope, user, $http) {
    $scope.user = user;
      $http.get('/movies/list').success(function(data) {
         $scope.movies = data;
      });

  };
  DashboardCtrl.$inject = ['$scope', 'user', '$http'];

  var AdminDashboardCtrl = function($scope, user) {
    $scope.user = user;
  };
  AdminDashboardCtrl.$inject = ['$scope', 'user'];

  return {
    DashboardCtrl: DashboardCtrl,
    AdminDashboardCtrl: AdminDashboardCtrl
  };

});
