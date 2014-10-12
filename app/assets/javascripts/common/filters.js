/** Common filters. */
define(['angular'], function(angular) {
    'use strict';

    var mod = angular.module('common.filters', []);
    /**
     * Extracts a given property from the value it is applied to.
     * {{{
     * (user | property:'name')
     * }}}
     */
    mod.filter('property', function(value, property) {
        if (angular.isObject(value)) {
            if (value.hasOwnProperty(property)) {
                return value[property];
            }
        }
    });

    /**
     * Formats a number as file size
     */
    mod.filter('fileSize', function() {
        return function(bytes, precision) {
            if (isNaN(parseFloat(bytes)) || !isFinite(bytes)) {
                return '-';
            }
            if (bytes === 0) {
                return '0 bytes';
            }
            if (typeof precision === 'undefined') {
                precision = 1;
            }

            var units = ['bytes', 'KB', 'MB', 'GB', 'TB', 'PB'],
                number = Math.floor(Math.log(bytes) / Math.log(1024));

            return (bytes / Math.pow(1024, Math.floor(number))).toFixed(precision) + ' ' + units[number];
        };
    });

    return mod;
});
