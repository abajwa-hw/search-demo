'use strict';

/* Services */

// Demonstrate how to register services
// In this case it is a simple value service.
angular.module('myApp.services', [ 'resource' ]).value('version', '0.1');

var app = angular.module('myApp.services', [ 'ngResource' ]);

app.factory('Solr', [ '$resource', function($resource) {
	return $resource('solr/:core/search?:query', {}, {
		query : {
			method : 'GET',
			params : {
				core : 'core',
				query : 'query'
			},
			isArray : false
		}
	});
} ]);

app.factory('Hbase', [ '$resource', function($resource) {
	var service = {};
	service.getter = $resource('hbase/:table/get?rowKey=:rowKey', {}, {
		get : {
			method : 'GET',
			params : {
				table : 'table',
				rowKey : 'rowKey'
			},
			isArray : false
		}
	})
	service.scanner = $resource('hbase/:table/scan?startRow=:startRow&endRow=:endRow', {}, {
		scan : {
			method : 'GET',
			params : {
				table : 'table',
				startRow : 'startRow',
				endRow : 'endRow'
			},
			isArray : true
		}
	})
	return service;
} ]);

app.config(function($httpProvider) {
	$httpProvider.responseInterceptors.push('myHttpInterceptor');

	var spinnerFunction = function spinnerFunction(data, headersGetter) {
		$("#spinner").show();
		return data;
	};

	$httpProvider.defaults.transformRequest.push(spinnerFunction);
});

app.factory('myHttpInterceptor', function($q, $window) {
	return function(promise) {
		return promise.then(function(response) {
			$("#spinner").hide();
			return response;
		}, function(response) {
			$("#spinner").hide();
			return $q.reject(response);
		});
	};
});