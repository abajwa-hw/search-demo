'use strict';

/* Controllers */

angular.module('myApp.controllers', []).controller('WelcomeCtrl',
		[ '$scope', '$log', 'WebSocket', function($scope, $log, WebSocket) {
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
		'DocSearchCtrl',
		[
				'$scope',
				'$log',
				'Solr',
			    '$modal',
				'$filter',
				function($scope, $log, Solr, $modal, $filter) {
					$log.log("Entered Search Controller")
					$scope.master = {};
					// Search Function
					$scope.search = function(query) {
						$scope.master = query;
						var solrQuery = "query=" + query.q
								+ "&facet.field=last_author&highlight.field=body";
						if ($scope.currentPage > 1) {
							solrQuery += "&start=" + ($scope.currentPage - 1)
									* 10;
						}
						Solr.query({
							core : "rawdocs",
							query : solrQuery
						}, function(value, responseHeaders) {
							$log.log(value);
							$scope.results = value.results;
							$scope.totalItems = value.numFound;
							$scope.highlights = value.highlights;
							$scope.error = false;
							$scope.chart = {
								"type" : "PieChart",
								"data" : [ [ "Authors", "Count" ] ]
										.concat(value.facets["last_author"]),
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
					$scope.open = function (docBody, highlights) {
				    var modalInstance = $modal.open({
				      templateUrl: 'docModalContent.html',
				      controller: 'ModalInstanceCtrl',
				      size: 'lg',
						resolve:  {
							docBody: function() {
								if (highlights && highlights[0]) {
									docBody = $filter('highlight-highlights')(docBody, highlights)
								}
								return $filter('nl2br')(docBody);
							}
						}
				    });

				    modalInstance.result.then(function (selectedItem) {
				      $scope.selected = selectedItem;
				    }, function () {
				    });
				  };
					$scope.updateModal = function(docBody) {
						$scope.docBody = docBody;
					}
				} ]).controller('ModalInstanceCtrl', function ($scope, $modalInstance, docBody) {
				  $scope.docBody = docBody;

				  $scope.close = function () {
				    $modalInstance.dismiss('Close');
				  };
				});
