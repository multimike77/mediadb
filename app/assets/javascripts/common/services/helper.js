/** Common helpers */
define(['angular'], function(angular) {
    'use strict';

    var mod = angular.module('common.helper', []);
    mod.service('helper', function() {
        return {
            sayHi: function() {
                return 'hi';
            },
            partitionArray: function(array, partitionSize) {
                var i, j, temparray = [];
                for (i = 0, j = array.length; i < j; i += partitionSize) {
                    temparray.push(array.slice(i, i + partitionSize));
                }
                return temparray;

            },
            getDownloadPath: function(filePath) {
                var pattern = new RegExp("/media/data([12])/share/");
                var updatedPath = filePath.replace(pattern, "/download/$1/");
                return updatedPath;
            }
        };
    });
    return mod;
});
