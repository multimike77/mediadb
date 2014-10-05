/**
 * Movie controllers.
 */
define([], function () {
    'use strict';

    /** Controls the movies page */
    var MovieCtrl = function ($scope, $rootScope, $location, helper, $http) {
        $rootScope.pageTitle = 'Filme';

        $http.get('/movies/list').success(function (data) {
            $scope.movies = helper.partitionArray(data, 4);
        });
    };
    MovieCtrl.$inject = ['$scope', '$rootScope', '$location', 'helper', '$http'];

    var MovieDetailsCtrl = function ($scope, $rootScope, $location, helper, $http, $routeParams) {
        var movieName = $routeParams.movieName;
        $rootScope.pageTitle = 'Filme - ' + movieName;
        var path = '/movies/details/' + movieName;

        $http.get(path).success(function (data) {
            data.filePath = helper.getDownloadPath(data.filePath);
            console.log(data);
            $scope.movie = data;
        });
    };
    MovieDetailsCtrl.$inject = ['$scope', '$rootScope', '$location', 'helper', '$http', '$routeParams'];


    return {
        MovieCtrl: MovieCtrl,
        MovieDetailsCtrl: MovieDetailsCtrl
    };

});
