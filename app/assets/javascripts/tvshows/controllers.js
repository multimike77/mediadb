/**
 * TV Show controllers.
 */
define([], function () {
    'use strict';

    /** Controls the tv shows page */
    var TVShowCtrl = function ($scope, $rootScope, $location, helper, $http) {
        $rootScope.pageTitle = 'Serien';

        $http.get('/tv/list').success(function (data) {
            $scope.tvshows = partitionArray(data, 4);
        });
    };

    TVShowCtrl.$inject = ['$scope', '$rootScope', '$location', 'helper', '$http'];

    var TVShowDetailsCtrl = function($scope, $rootScope, $location, helper, $http, $routeParams) {
        var tvShowName = $routeParams.showName;
        var path = '/tv/show/' + tvShowName;

        $rootScope.pageTitle = 'Serien Details - ' + tvShowName;
        $http.get(path).success(function (data) {
            console.log(data);
            $scope.tvShow = data;

            $http.get(path + '/episodes').success(function(data) {
                console.log(data);
                $scope.seasons = data;
            });
        });
    };

    TVShowDetailsCtrl.$inject = ['$scope', '$rootScope', '$location', 'helper', '$http', '$routeParams'];

    function partitionArray(input, partitionSize) {
        var i, j, temparray = [];
        for (i = 0, j = input.length; i < j; i += partitionSize) {
            temparray.push(input.slice(i, i + partitionSize));
        }
        return temparray;
    }

    return {
        TVShowCtrl: TVShowCtrl,
        TVShowDetailsCtrl: TVShowDetailsCtrl
    };

});
