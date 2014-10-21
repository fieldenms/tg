require.config({
	baseUrl: '/' // the baseUrl is set to correspond to server configuration, which states that main App feature is located in 'localhost:1692/'
});

require(['/resources/config.js'], function() {
	require(['angular', 'angular.route', 'angular.resource', 'bootstrap.ui', 'css!resources/app'], function(angular) {
		var appModule = angular.module('app', ['ngRoute', 'ngResource', 'ui.bootstrap']); // create main angular application module which depends on GisModule

		appModule.config(function($routeProvider) {

			$routeProvider.
			when('/', {
				templateUrl: 'resources/myProfile.html'
			}).
			when('/centre/:centreName', {
				templateUrl: 'resources/centre/centre.html',
				controller: 'CentreController',
				resolve: {
					centre: function(CentreEntity, $route, $q) {
						var deferred = $q.defer();
						CentreEntity.get({
							centreName: $route.current.params.centreName
						}, function(centre) {
							deferred.resolve(centre);
						});
						return deferred.promise;
					}
				}
			}).
			otherwise({
				redirectTo: '/'
			});
		});

		appModule.factory("CentreEntity", function($resource) {
			return $resource("users/SU/centre/:centreName");
		});

		appModule.factory("QueryRunner", function($resource) {
			return $resource("users/SU/QueryRunner", {}, {
				action: {
					method: 'POST'
				}
			});
		});

		appModule.controller('navCtrl', ['$scope', '$location',
			function($scope, $location) {
				$scope.navClass = function(page) {
					var currentRoute = $location.path().substring(1) || '';
					return page === currentRoute ? 'active' : '';
				};
			}
		]);

		appModule.controller("TaskBarController", ["$scope",
			function($scope) {
				$scope.status = {
					isOpen: true
				};
			}
		]);

		appModule.controller("CentreController", ["$scope", "$routeParams", "centre", "QueryRunner",
			function($scope, $routeParams, centre, QueryRunner) {

				var emptyPage = {
					pageNo: 0,
					numberOfPages: 0,
					summary: {},
					results: []
				};

				$scope.centre = centre;
				$scope.pageCapacity = 5;
				$scope.pageData = {

					page: emptyPage,
					query: null,
					pageCapacity: 0,

					getPageFeedBack: function() {
						return "page: " + (this.page.numberOfPages == 0 ? 0 : this.page.pageNo + 1) + " of " + this.page.numberOfPages;
					},

					hasNext: function() {
						return this.page.pageNo < this.page.numberOfPages - 1;
					},

					hasPrev: function() {
						return this.page.pageNo > 0;
					},

					first: function() {
						if (this.hasPrev()) {
							this.navigatePage(0);
						}
					},

					prev: function() {
						if (this.hasPrev()) {
							this.navigatePage(this.page.pageNo - 1);
						}
					},

					next: function() {
						if (this.hasNext()) {
							this.navigatePage(this.page.pageNo + 1);
						}
					},

					last: function() {
						if (this.hasNext()) {
							this.navigatePage(this.page.numberOfPages - 1);
						}
					},

					navigatePage: function(pageNo) {
						var oldSummary = this.page.summary;
						var context = this;
						QueryRunner.getPage({
							centreName: $routeParams.centreName,
							pageNo: pageNo,
							pageCapacity: this.pageCapacity,
							query: this.query
						}, function(data) {
							context.page = data;
							context.page.summary = oldSummary;
						}, function(error) {
							context.page = emptyPage;
						});
					}
				};

				$scope.run = function() {
					//Setting initial parameters for page.
					$scope.pageData.query = JSON.parse(JSON.stringify($scope.centre.query));
					$scope.pageData.pageCapacity = $scope.pageCapacity;

					var queryRunner = new QueryRunner();
					queryRunner.key = "QueryRunner";
					queryRunner["@entityType"] = "ua.com.fielden.platform.entity.functional.centre.QueryRunner";
					queryRunner.pageCapacity = $scope.pageData.pageCapacity;
					queryRunner.query = $scope.pageData.query;
					queryRunner.$action(function(result) {
						//result is a JavaScript represtation of the appripriate Java class named Result.
						if (!result.ex){
							$scope.pageData.page = result.instance.page;	
						} else {
							alert(result.message);
						}
						
					}, function(error) {
						alert(error);
						$scope.pageData.page = emptyPage;
					});
				}

				$scope.getValueForEntityGrid = function(entity, prop) {

					function concatWithDot(str1, str2) {
						return str1 && str2 ? str1 + "." + str2 : str1 || str2;
					}

					function get(obj, str) {
						return str.split(".").reduce(function(o, x) {
							return o[x]
						}, obj);
					}

					if (prop.type == "Entity") {
						return get(entity, concatWithDot(prop.propertyName, "key"));
					}
					return get(entity, prop.propertyName);
				}

			}
		]);

		appModule.directive('tgSelectionCriteria', function($compile) {

			function initMetrics(scope, prefWidth) {
				scope.metric = {};
				scope.metric.width = prefWidth;
				scope.metric.gapWidth = 20;
				scope.metric.toPartWidth = getHtmlDimensions('to').width + 10;
				scope.metric.labelWidth = getLabelWidth(getMinEditorWidth());
				scope.metric.singleEditorWidth = getSingleEditorWidth();
				scope.metric.doubleEditorWidth = getDoubleEditorWidth();
				scope.metric.editorHeight = getEditorHeight();

				function getEditorHeight() {
					return getHtmlDimensions("<input type='text'>").height;
				}

				function getDoubleEditorWidth() {
					return (scope.metric.singleEditorWidth - scope.metric.toPartWidth) / 2;
				}

				function getMinEditorWidth() {
					var checkBoxWidth = getHtmlDimensions("<label class='checkbox'><input type='checkbox'>yes</label>").width;
					return scope.metric.toPartWidth + checkBoxWidth * 2;
				}

				function getSingleEditorWidth() {
					var columns = scope.centre.centreConfig.criteria.columns;
					return (prefWidth - scope.metric.labelWidth * columns - (columns - 1) * scope.metric.gapWidth) / columns;
				}

				function getLabelWidth(minEditorWidth) {
					var maxWidth = -1;
					scope.centre.centreConfig.criteria.criteriaProperties.forEach(function(elem, index, array) {
						if (elem != null) {
							var htmlText = elem.title + ":";
							var htmlDim = getHtmlDimensions(htmlText);
							maxWidth = Math.max(maxWidth, htmlDim.width + 5);
						}
					});
					var columns = scope.centre.centreConfig.criteria.columns;
					var maxAvailableWidth = (prefWidth - (columns - 1) * scope.metric.gapWidth) / (columns * 2);
					return Math.min(maxWidth, Math.max(maxAvailableWidth, minEditorWidth));
				}

				//Calculates the width and height of the specified html text
				function getHtmlDimensions(htmlText) {

					var div = document.createElement('div');
					div.setAttribute('class', 'htmlDimensionCalculation');

					$(div).html(htmlText);

					document.body.appendChild(div);

					var dimensions = {
						width: $(div).outerWidth(),
						height: $(div).outerHeight()
					};

					div.parentNode.removeChild(div);
					return dimensions;
				}
			}

			function createTemplate(scope, prefWidth) {

				var columns = scope.centre.centreConfig.criteria.columns;
				var rows = scope.centre.centreConfig.criteria.criteriaProperties.length / columns;

				function createTable() {
					var rowStr = "";
					for (var row = 0; row < rows; row++) {
						rowStr += "<tr style='height:{{metric.editorHeight}}px'>" + createRow(row) + "</tr>";
					}
					return rowStr;
				}

				function createRow(rowInd) {
					var row = ""
					var columns = scope.centre.centreConfig.criteria.columns;
					for (var col = 0; col < columns; col++) {
						if (col != 0) {
							row += createBetweenEditorsGap();
						}
						row += createCol(rowInd, col);
					}
					return row;
				}

				function createBetweenEditorsGap() {
					return '<td style="width:{{metric.gapWidth}}px"></td>';
				}

				function createCol(row, col) {
					var index = row * columns + col, critName;

					if (scope.centre.centreConfig.criteria.criteriaProperties[index] == null) {
						return '<td style="width:{{metric.labelWidth}}px"></td>' +
							'<td style="width:{{metric.singleEditorWidth}}px"></td>';
					} else {
						critName = scope.centre.centreConfig.criteria.criteriaProperties[index].propertyName;
						return '<td class="elipsedText" style="width:{{metric.labelWidth}}px">' +
							'{{centre.centreConfig.criteria.criteriaProperties[' + index + '].title}}:</td>' +
							'<td style="width:{{metric.singleEditorWidth}}px">' +
							"<tg-editor " +
							"config='centre.query.criteriaProperties[" + '"' + critName + '"' + "]'" +
							"metric='metric'></tg-editor></td>";
					}
				}

				initMetrics(scope, prefWidth);
				return "<table style='width:100%;height:100%'>" + createTable() + "</table>";
			}

			return {
				restrict: "E",
				scope: {
					centre: "="
				},
				compile: function(tElement, tAttrs, transclude) {
					return function(scope, iElement, iAttrs, controller) {
						var selCritElem = angular.element(createTemplate(scope, iElement.parent().width()));
						iElement.replaceWith(selCritElem);
						$compile(selCritElem)(scope);
					}
				}
			}
		});

		appModule.directive("tgEditor", function($compile) {

			function getEditorConfig() {
				return "class='input-block-level form-control' style='margin-top:5px;margin-bottom:5px'";
			}

			var createEntityEditor = function(config) {
				return "<input " + getEditorConfig() + " type='text' ng-model='config.value1' ng-list>";
			};

			var createStringEditor = function(config) {
				return "<input " + getEditorConfig() + " type='text' ng-model='config.value1'>";
			};

			var createIntegerEditor = function(config) {
				if (config.single) {
					return "<input " + getEditorConfig() + " type='text' ng-model='config.value1'>";
				} else {
					var firstInput = "<td style='width:{{metric.doubleEditorWidth}}px;padding:0px'>" +
						"<input " + getEditorConfig() + " type='text' ng-model='config.value1'>" +
						"</td>";
					var toPart = "<td style='width:{{metric.toPartWidth}}px;text-align:center;padding:0px'>to</td>";
					var secondInput = "<td style='width:{{metric.doubleEditorWidth}}px;padding:0px'>" +
						"<input " + getEditorConfig() + " type='text' ng-model='config.value2'>" +
						"</td>";
					return "<table style='width:100%;height:100%'><tr>" +
						firstInput + toPart + secondInput + "</tr></table>";
				}
			};

			var createBooleanEditor = function(config) {
				var firstInput = "<td class='checkbox' style='width:{{metric.doubleEditorWidth}}px;padding:0px'>" +
					"<label style='margin-top:5px;margin-bottom:5px'>" +
					"<input type='checkbox' ng-model='config.value1'>yes</label>" +
					"</td>";
				var emptyPart = "<td style='width:{{metric.toPartWidth}}px;padding:0px'></td>";
				var secondInput = "<td class='checkbox' style='width:{{metric.doubleEditorWidth}}px;padding:0px'>" +
					"<label style='margin-top:5px;margin-bottom:5px'>" +
					"<input type='checkbox' ng-model='config.value2'>no</label>" +
					"</td>";
				return "<table style='width:100%;height:100%'><tr>" +
					firstInput + emptyPart + secondInput + "</tr></table>";
			};

			var editorsMap = {
				"Entity": createEntityEditor,
				"String": createStringEditor,
				"Integer": createIntegerEditor,
				"Date": createIntegerEditor,
				"Money": createIntegerEditor,
				"BigDecimal": createIntegerEditor,
				"Long": createIntegerEditor,
				"Boolean": createBooleanEditor
			};

			return {
				restrict: 'E',
				scope: {
					config: "=",
					metric: "="
				},
				compile: function(tElement, tAttrs, transclude) {
					return function(scope, iElement, iAttrs, controller) {
						var elemStr = editorsMap[scope.config.propType](scope.config);
						var newElem = angular.element(elemStr);
						iElement.replaceWith(newElem);
						$compile(newElem)(scope);
					}
				}
			}
		});

		angular.bootstrap(document.getElementsByTagName('body')[0], ['app']); // boot angular application with main module
	});
});
