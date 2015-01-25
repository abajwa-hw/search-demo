'use strict';

/* Controllers */

angular.module('myApp.controllers', []).controller('WelcomeCtrl',
		[ '$scope', '$log', 'WebSocket', function($scope, $log, WebSocket) {
			// Show Default Map
			$scope.map = {
				center : {
					latitude : 37.4345604,
					longitude : -122.1089708
				},
				zoom : 10
			};
			$scope.showMap = true;
			// Register callback and wait until the browser get's our GPS
			// coordinates
			window.navigator.geolocation.getCurrentPosition(function(position) {
				$scope.position = position;
				$log.log($scope.position.coords.latitude);
				$scope.map = {
					center : {
						latitude : $scope.position.coords.latitude,
						longitude : $scope.position.coords.longitude
					},
					zoom : 15
				};
			}, function(error) {
				alert(error);
			});
			// Register with the Websocket instance on the server to start
			// handling events
			WebSocket.onopen(function() {
				$log.log('connection');
				WebSocket.send('Test Message')
			});

			WebSocket.onmessage(function(event) {
				$log.log('Recieved Message: ', event.data);
				$('#alert_placeholder').html('<div class="alert alert-warning alert-dismissible" role="alert"><button type="button" class="close" data-dismiss="alert"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button><strong>404!</strong> - '+event.data+'</div>')
			});
		} ]).controller('LogsCtrl', [ '$scope', function($scope) {

} ]).controller(
		'LogsSearchCtrl',
		[
				'$scope',
				'$log',
				'Solr',
				function($scope, $log, Solr) {
					$log.log("Entered Search Controller")
					$scope.master = {};
					// Search Function
					$scope.search = function(query) {
						$scope.master = query;
						var solrQuery = "query=" + query.q
								+ "&facet.field=status";
						if ($scope.currentPage > 1) {
							solrQuery += "&start=" + ($scope.currentPage - 1)
									* 10;
						}
						Solr.query({
							core : "access_logs",
							query : solrQuery
						}, function(value, responseHeaders) {
							$log.log(value);
							$scope.results = value.results;
							$scope.totalItems = value.numFound;
							$scope.error = false;
							$scope.chart = {
								"type" : "PieChart",
								"data" : [ [ "Status", "Count" ] ]
										.concat(value.facets["status"]),
								"options" : {
									"displayExactValues" : true,
									"width" : 400,
									"height" : 120,
									"is3D" : true,
									"chartArea" : {
										"left" : 10,
										"top" : 10,
										"bottom" : 0,
										"height" : "100%"
									}
								},
								"formatters" : {},
								"displayed" : true
							}
						}, function(httpResponse) {
							$log.log(httpResponse)
							$scope.results = [];
							$scope.error = true;
							$scope.errorStatus = httpResponse.status;
							$scope.errorMessage = httpResponse.data;
						});
					};
					// Pagination with a max page size of 25 pages in the
					// carousel
					$scope.maxSize = 25
					if ($scope.currentPage < 1) {
						$scope.currentPage = 1;
					}
					$scope.pageChanged = function() {
						console.log('Page changed to: ' + $scope.currentPage);
						$scope.search($scope.query)
					};
				} ]).controller('HbaseGetCtrl',
		[ '$scope', '$log', '$routeParams', 'Hbase', function($scope, $log, $routeParams, Hbase) {
			$scope.get = function(rowKey) {
				Hbase.getter.get({
					table : 'access_logs',
					rowKey : rowKey
				}, function(value, responseHeaders) {
					$scope.row = value;
					$log.log($scope.row);
					$scope.error = false;
				}, function(httpResponse) {
					$log.log(httpResponse);
					$scope.error = true;
					$scope.errorStatus = httpResponse.status;
					$scope.errorMessage = httpResponse.data;
				})
			}
			if ($routeParams.rowKey != undefined) {
				$scope.rowKey = $routeParams.rowKey
				$scope.get($scope.rowKey)
			}
		} ]).controller('HbaseScanCtrl',
		[ '$scope', '$log', 'Hbase', function($scope, $log, Hbase) {
			$scope.scan = function(startRow, endRow) {
				Hbase.scanner.scan({
					table : 'access_logs',
					startRow : startRow,
					endRow: endRow
				}, function(value, responseHeaders) {
					$scope.results = value;
					$scope.error = false;
				}, function(httpResponse) {
					$log.log(httpResponse);
					$scope.error = true;
					$scope.errorStatus = httpResponse.status;
					$scope.errorMessage = httpResponse.data;
				})
			}
		} ]);
