/**
 * Movie controllers.
 */
define([], function () {
    'use strict';

    /** Controls the movies page */
    var MovieCtrl = function ($scope, $rootScope, $location, helper, $http, $filter) {
        $rootScope.pageTitle = 'Filme';
        $scope.sorting = 'alpha';
        var movies = [];
        var orderBy = $filter('orderBy');

        $scope.sortMovies = function() {
            var sortField = $scope.sorting === 'alpha' ? 'details.title' : '-creationDate';
            var sortedMovies = orderBy(movies, sortField, false);
            console.log(sortedMovies);
            $scope.movies = helper.partitionArray(sortedMovies, 4);
        };

        $http.get('/api/movies/list').success(function (data) {
            movies = data;
            $scope.sortMovies();
        });
    };
    MovieCtrl.$inject = ['$scope', '$rootScope', '$location', 'helper', '$http', '$filter'];

    var MovieDetailsCtrl = function ($scope, $rootScope, $location, helper, $http, $routeParams) {
        var movieName = $routeParams.movieName;
        $rootScope.pageTitle = 'Filme - ' + movieName;
        var path = '/api/movies/details/' + movieName;

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
