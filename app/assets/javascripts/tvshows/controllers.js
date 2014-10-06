/**
 * TV Show controllers.
 */
define(['angular'], function (angular) {
    'use strict';

    /** Controls the tv shows page */
    var TVShowCtrl = function ($scope, $rootScope, $location, helper, $http) {
        $rootScope.pageTitle = 'Serien';

        $http.get('/tv/list').success(function (data) {
            $scope.tvshows = helper.partitionArray(data, 4);
        });
    };

    TVShowCtrl.$inject = ['$scope', '$rootScope', '$location', 'helper', '$http'];

    var TVShowDetailsCtrl = function($scope, $rootScope, $location, helper, $http, $routeParams) {
        var tvShowName = $routeParams.showName;
        var path = '/tv/show/' + tvShowName;

        $rootScope.pageTitle = 'Serien Details - ' + tvShowName;
        $http.get(path).success(function (data) {
            console.log(data);
            data.details.status = getTVShowStatus(data.details.status);
            $scope.tvShow = data;

            $http.get(path + '/episodes').success(function(data) {
                angular.forEach(data, function(season) {
                    angular.forEach(season.episodes, function(episode) {
                        episode.filePath = helper.getDownloadPath(episode.filePath);
                    });
                });

                console.log(data);
                $scope.seasons = data;
            });
        });
    };

    TVShowDetailsCtrl.$inject = ['$scope', '$rootScope', '$location', 'helper', '$http', '$routeParams'];

    return {
        TVShowCtrl: TVShowCtrl,
        TVShowDetailsCtrl: TVShowDetailsCtrl
    };

    function getTVShowStatus(status) {
        switch(status) {
            case "Returning Series": return "Wiederkehrende Serie";
            case "Canceled": return "Abgesetzt";
            case "Ended": return "Beendet";
            case "In Production": return "In Produktion";
        }
    }
});
