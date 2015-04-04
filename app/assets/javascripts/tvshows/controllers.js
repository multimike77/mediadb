/**
 * TV Show controllers.
 */
define(['angular'], function (angular) {
    'use strict';

    /** Controls the tv shows page */
    var TVShowCtrl = function ($scope, $rootScope, $location, helper, $http, $filter) {
        $rootScope.pageTitle = 'Serien';
        $scope.sorting = 'added';
        var shows = [];
        var orderBy = $filter('orderBy');

        $scope.sortMovies = function() {
            var sortField = $scope.sorting === 'alpha' ? 'details.title' : '-creationDate';
            var sortedMovies = orderBy(shows, sortField, false);
            $scope.tvshows = helper.partitionArray(sortedMovies, 4);
        };

        $http.get('/api/tv/list').success(function (data) {
            shows = data;
            $scope.sortMovies();
        });
    };

    TVShowCtrl.$inject = ['$scope', '$rootScope', '$location', 'helper', '$http', '$filter'];

    var TVShowDetailsCtrl = function($scope, $rootScope, $location, helper, $http, $routeParams) {
        var tvShowName = $routeParams.showName;
        var path = '/api/tv/show/' + tvShowName;

        $rootScope.pageTitle = 'Serien Details - ' + tvShowName;
        $http.get(path).success(function (data) {
            console.log(data);
            data.details.status = getTVShowStatus(data.details.status);
            $scope.tvShow = data;
            $scope.backgroundImg = {background: 'url(/images/w1280' +
                data.details.backdrop_path +
                ') no-repeat fixed 50% 0'};

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
