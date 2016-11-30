var meetingsApp = angular.module('meetings.app', [ 'ngRoute', 'meetings.controllers']);

var meetingsControllers = angular.module('meetings.controllers', []);

/**
 * Configuration: defines the routes..
 */
meetingsApp.config([ '$routeProvider', '$httpProvider', function($routeProvider, $httpProvider) {
	// disable IE caching, as per
    // -
    // http://www.oodlestechnologies.com/blogs/AngularJS-caching-issue-for-Internet-Explorer
    // -
    // http://stackoverflow.com/questions/16971831/better-way-to-prevent-ie-cache-in-angularjs
    // else IE frequently decides it doesn't need to actually make the $http
    // call, and uses the cache instead.
    $httpProvider.defaults.cache = false;
    if (!$httpProvider.defaults.headers.get) {
        $httpProvider.defaults.headers.get = {};
    }
    $httpProvider.defaults.headers.get['If-Modified-Since'] = '0';

    $routeProvider.when('/admin', {
    	templateUrl : 'templates/admin.html',
    	controller : 'AdminCtrl',
    	controllerAs : 'adminCtrl'
    }).when('/:meetingId', {
        templateUrl : 'templates/meeting.html',
        controller : 'MeetingCtrl',
        controllerAs : 'meetingCtrl'
    }).otherwise({
        templateUrl : 'templates/meetingList.html',
        controller : 'MeetingsCtrl',
        controllerAs : 'meetingCtrl'
    });
} ]);

meetingsControllers.controller('AdminCtrl', ['$scope', '$location', '$http', function AdminCtro($scope, $location, $http) {
	$http.get('rest/admin/users', {"withCredentials": true}).
      success(function(data, status, headers, config) {
        // this callback will be called asynchronously
        // when the response is available
    	  $scope.users = data;
      }).
      error(function(data, status, headers, config) {
	  	alert("Error " + status + " received retrieving users");
      });
	
	$scope.deleteUser = function(user) {
		$http.delete('rest/admin/users/' + user, {"withCredentials" : true}).
		  success(function(data, status, headers, config) {
			  // should do something smarter, should just remove locally
				$http.get('rest/admin/users', {"withCredentials": true}).
			      success(function(data, status, headers, config) {
			        // this callback will be called asynchronously
			        // when the response is available
			    	  $scope.users = data;
			      }).
			      error(function(data, status, headers, config) {
				  	alert("Error " + status + " received retrieving users");
			      });
		  }).
		  error(function(data, status, headers, config) {
			  	alert("Error " + status + " received deleting " + user);
		  });
	}
}]);

meetingsControllers.controller('MeetingCtrl', [ '$scope', '$location', '$http', '$window', function MeetingCtrl($scope, $location, $http, $window) {
	$scope.view = 'templates/meetingLoading.html';

	console.log("Fetching details for %s", $location.path());
	$http.get('rest/meetings' + $location.path()).
    success(function(data, status, headers, config) {
      // this callback will be called asynchronously
      // when the response is available
    	$scope.meeting = data;
    	if (data.hasOwnProperty('joinURL') && data.joinURL != null) {
    		 $scope.redirectToMeeting(data.joinURL);
    	} else {
    		$scope.view = 'templates/aMeeting.html';
    		var dir = $window.location.pathname.substring(0, $window.location.pathname.lastIndexOf('/'));
    		var webSocketEndpoint = "ws://" + $window.document.location.host + dir + '/notifier';
    		$scope.webSocket = new WebSocket(webSocketEndpoint);
    		$scope.webSocket.onmessage = function(event) {
	     	    $scope.webSocket.close();
	     	    $scope.redirectToMeeting(event.data);
    		};

    		var startWaiting = function() {
        		if ($scope.webSocket.readyState == WebSocket.CONNECTING) {
        			console.log("waiting");
        			setTimeout(startWaiting, 500);
        		} else if ($scope.webSocket.readyState == WebSocket.OPEN) {
        			$scope.webSocket.send($scope.meeting.id);
        		}
    		}
    		startWaiting();
    	}
    }).
    error(function(data, status, headers, config) {
		alert("Error " + status + " received retrieving meetings");
    });

	$scope.beginMeeting = function(meeting) {
		$scope.webSocket.close();
		$http.post('rest/meetings/' + meeting.id, meeting).
	    success(function(data, status, headers, config) {
	    	$http.get('rest/meetings/' + meeting.id).
	        success(function(data, status, headers, config) {
	    		$window.location.href= data.meetingURL;
	        });
	    });
	}

	$scope.startMeeting = function(meeting) {
//		$http.post('rest/user', {"meetingId" : meeting.meetingId});
		$scope.beginMeeting(meeting);
	}

	$scope.hideStartButton = function() {
		$scope.view = 'templates/joinMeeting.html';
	}
	
	$scope.findMeetingURL = function() {
//		$http.get('rest/user', {"withCredentials" : true}).success(function(data, status, headers, config) {
//			if (data.hasOwnProperty("meetingId") && data.meetingId != null) {
//				$scope.meeting.meetingId = data.meetingId;
//				$scope.beginMeeting($scope.meeting);
//			} else {
				$scope.view = 'templates/provideMeeting.html';
//			}
//		});
	}

	$scope.redirectToMeeting = function(meetingURL) {
	    $window.location.href = meetingURL;
	}
} ]);

meetingsControllers.controller('MeetingsCtrl', [ '$scope', '$http', function MeetingsCtrl($scope, $http) {

	$http.get('rest/meetings').
    success(function(data, status, headers, config) {
      // this callback will be called asynchronously
      // when the response is available
    	$scope.meetings = data;
    }).
    error(function(data, status, headers, config) {
		alert("Error " + status + " received retrieving meetings");
    });


	$scope.endMeeting = function(meeting) {
		if (meeting.hasOwnProperty("meetingId") && meeting.meetingId != null) {
			theMeeting = {"id" : meeting.id, "title": meeting.title, "duration": meeting.duration};
			
			$http.post('rest/admin/meeting/', theMeeting, {"withCredentials" : true}).success(function(data, status, headers, config) {
				meeting.meetingId = null;
			});
		}
	}
	
  $scope.addMeeting = function(meeting) {
	$http.put("rest/meetings", meeting).
    success(function(data, status, headers, config) {
        meeting.id = '';
        meeting.duration = 0;
        meeting.title = '';
        // this callback will be called asynchronously
        // when the response is available
    	$http.get('rest/meetings').
        success(function(data, status, headers, config) {
          // this callback will be called asynchronously
          // when the response is available
        	$scope.meetings = data;
        });
      }).
    error(function(data, status, headers, config) {
		alert("Error " + status + " received adding a meeting");
	});
  };
} ]);
