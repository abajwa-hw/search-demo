'use strict';


// Declare app level module which depends on filters, and services
angular.module('myApp', [
  'ngRoute',
  'ngResource',
  'myApp.filters',
  'myApp.services',
  'myApp.directives',
  'myApp.controllers',
  'ui.bootstrap',
  'angular-websocket',
  'googlechart',
  'google-maps'
  //'leaflet-directive'
]).
config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/welcome', {templateUrl: 'partials/welcome.html', controller: 'WelcomeCtrl'});
  $routeProvider.when('/logs', {templateUrl: 'partials/logs/welcome.html', controller: 'LogsCtrl'});
  $routeProvider.when('/logs/search', {templateUrl: 'partials/logs/search.html', controller: 'LogsSearchCtrl'});
  $routeProvider.when('/logs/hbaseGet', {templateUrl: 'partials/logs/hbaseGet.html', controller: 'HbaseGetCtrl'});
  $routeProvider.when('/logs/hbaseScan', {templateUrl: 'partials/logs/hbaseScan.html', controller: 'HbaseScanCtrl'});
  $routeProvider.otherwise({redirectTo: '/welcome'});
}]).config(function(WebSocketProvider){
    WebSocketProvider
    .prefix('')
    .uri('ws://localhost:9091/');
});
