'use strict';


// Declare app level module which depends on filters, and services
angular.module('myApp', [
  'ngRoute',
  'ngResource',
  'ngSanitize',
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
  $routeProvider.when('/docs/search', {templateUrl: 'partials/docs/search.html', controller: 'DocSearchCtrl'});
  $routeProvider.otherwise({redirectTo: '/welcome'});
}]).config(function(WebSocketProvider){
    WebSocketProvider
    .prefix('')
    .uri('ws://localhost:9091/');
});
