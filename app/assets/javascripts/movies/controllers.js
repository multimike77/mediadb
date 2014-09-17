/**
 * Movie controllers.
 */
define([], function () {
    'use strict';

    /** Controls the movies page */
    var MovieCtrl = function ($scope, $rootScope, $location, helper, $http) {
        $rootScope.pageTitle = 'Filme';

        $http.get('/movies/list').success(function (data) {
            $scope.movies = partitionArray(data, 4);
        });
    };
    MovieCtrl.$inject = ['$scope', '$rootScope', '$location', 'helper', '$http'];

    var MovieDetailsCtrl = function ($scope, $rootScope, $location, helper, $http, $routeParams) {
        console.log('aaa');
        var movieName = $routeParams.movieName;
        $rootScope.pageTitle = 'Filme - ' + movieName;
        var path = '/movies/details/' + movieName;

        $http.get(path).success(function (data) {
            console.log(data);
            $scope.movie = data;
        });
    };
    MovieDetailsCtrl.$inject = ['$scope', '$rootScope', '$location', 'helper', '$http', '$routeParams'];


    function partitionArray(input, partitionSize) {
        var i, j, temparray = [];
        for (i = 0, j = input.length; i < j; i += partitionSize) {
            temparray.push(input.slice(i, i + partitionSize));
        }
        return temparray;
    }

    return {
        MovieCtrl: MovieCtrl
    };

});
