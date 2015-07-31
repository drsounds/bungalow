
var mercyApp = angular.module('MercyApp', ['ngRoute']);
var player = new SpotifyPlayer();
mercyApp.config(['$routeProvider', '$locationProvider', '$sceDelegateProvider', function ($routeProvider, $locationProvider, $sceDelegateProvider) {
   $sceDelegateProvider.resourceUrlWhitelist(['self', new RegExp('^(app?):\/\/.+$')]);
   $locationProvider.html5Mode(true)

	
}])
.controller('LoginCtrl', ['$scope', '$route', '$rootScope', '$routeParams', '$location', function ($scope, $route, $rootScope, $routeParams, $location) {
	player.addEventListener('ready', function () {
   		$rootScope.loggedIn = true;
   		$rootScope.$apply();
   	});
	player.login('drsounds', 'carin123');

}])
.controller('MainCtrl', ['$scope', '$route', '$routeParams', '$location', function ($scope, $route, $routeParams, $location) {
	$scope.menu = [
		{
			'title': 'Start',
			'uri': 'spotify:start'
		},
		{
			'title': 'Inbox',
			'uri': 'spotify:inbox'
		},
		{
			'title': 'Library',
			'uri': 'spotify:library'
		},
		{
			'title': 'Test playlist',
			'uri': 'spotify:user:test:playlist:test'
		},
		{
			'title': 'Magic',
			'uri': 'spotify:magic'
		}
	];
	
	$scope.select = function ($event) {
		$event.preventDefault();
		var a = $event.target.parentNode;
		var uri = a.getAttribute('data-uri');
		console.log(a);
		uri = uri.substr('spotify:'.length).split(/\:/g);
		uri = '/' + uri.join('/');
		console.log(uri);
		$location.path(uri);
	};
	$scope.search = function (data) {
		var q = data.query;
		if (q.indexOf('spotify:') != 0) {
			q = 'spotify:search:' + data.query;
		}
		var uri = q;
		uri = uri.substr('spotify:'.length).split(/\:/g);
		uri = '/' + uri.join('/');
		console.log(uri);
		$location.path(uri);
		
	};
	$scope.$on('$locationChangeStart', function(event, next, current) {
		try {
			var path = next.split(/\//g);

			var uri = 'spotify:' + path.slice(3).join(':');
			console.log(uri);
			$scope.uri = uri;
		} catch (e) {
			$scope.uri = 'spotify:start';
		}
	});

}])
.controller('StartCtrl', ['$scope', '$route', '$routeParams', function ($scope, $route, $routeParams) {
	$scope.select = function ($event) {
		$event.preventDefault();
		var a = $event.target.parentNode;
		var uri = a.getAttribute('data-uri');
		console.log(a);
		uri = uri.substr('spotify:'.length).split(/\:/g);
		uri = '/' + uri.join('/');
		$location.path(uri);
	};
}])
.controller('InboxCtrl', ['$scope', '$route', '$routeParams', function ($scope, $route, $routeParams) {
	$scope.select = function ($event) {
		$event.preventDefault();
		var a = $event.target.parentNode;
		var uri = a.getAttribute('data-uri');
		console.log(a);
		uri = uri.substr('spotify:'.length).split(/\:/g);
		uri = '/' + uri.join('/');
		$location.path(uri);
	};
}])
.controller('PlaylistCtrl', ['$scope', '$timeout', '$route', '$routeParams', '$http', function ($scope, $timeout, $route, $routeParams, $http) {

	
	$scope.select = function ($event) {
		$event.preventDefault();
		var a = $event.target.parentNode;
		var uri = a.getAttribute('data-uri');
		console.log(a);
		uri = uri.substr('spotify:'.length).split(/\:/g);
		uri = '/' + uri.join('/');
		$location.path(uri);
	};
}])
.controller('LibraryCtrl', ['$scope', '$timeout', '$route', '$routeParams', function ($scope, $timeout, $route, $routeParams) {
	$timeout(function () {
		console.log("A");
		$scope.playlist = {
			'tracks': [{
				'name': 'Test',
				'artists': [{
					'name': 'Test',
					'uri': ''
				}],
				'album': {
					'name': 'Test'
				}
			}],
			'title': 'Library',
			'icon': 'home',
			'description': 'My personal library'
		};
		console.log($scope);

		$scope.$apply();
	});
	$scope.select = function ($event) {
		$event.preventDefault();
		var a = $event.target.parentNode;
		var uri = a.getAttribute('data-uri');
		console.log(a);
		uri = uri.substr('spotify:'.length).split(/\:/g);
		uri = '/' + uri.join('/');
		$location.path(uri);
	};
}])
.controller('MagicCtrl', ['$scope', '$timeout', '$route', '$routeParams', function ($scope, $timeout, $route, $routeParams) {
	$timeout(function () {
		console.log("A");
		$scope.playlist = {
			'tracks': [{
				'name': 'Test',
				'artists': [{
					'name': 'Test',
					'uri': ''
				}],
				'album': {
					'name': 'Test'
				}
			}],
			'title': 'Magic',
			'icon': 'magic',
			'description': 'My personal library'
		};
		console.log($scope);

		$scope.$apply();
	});
	$scope.select = function ($event) {
		$event.preventDefault();
		var a = $event.target.parentNode;
		var uri = a.getAttribute('data-uri');
		console.log(a);
		uri = uri.substr('spotify:'.length).split(/\:/g);
		uri = '/' + uri.join('/');
		$location.path(uri);
	};
}])
.controller('AlbumCtrl', ['$scope', '$timeout', '$route', '$routeParams', function ($scope, $timeout, $route, $routeParams) {
	
	$scope.select = function ($event) {
		$event.preventDefault();
		var a = $event.target.parentNode;
		var uri = a.getAttribute('data-uri');
		console.log(a);
		uri = uri.substr('spotify:'.length).split(/\:/g);
		uri = '/' + uri.join('/');
		$location.path(uri);
	};
}])
.controller('ArtistCtrl', ['$scope', '$timeout', '$route', '$routeParams', '$http', function ($scope, $timeout, $route, $routeParams, $http) {
	
	$scope.select = function ($event) {
		$event.preventDefault();
		var a = $event.target.parentNode;
		var uri = a.getAttribute('data-uri');
		console.log(a);
		uri = uri.substr('spotify:'.length).split(/\:/g);
		uri = '/' + uri.join('/');
		$location.path(uri);
	};
}])
.controller('SearchCtrl', ['$scope', '$timeout', '$route', '$routeParams', '$http', '$location', function ($scope, $timeout, $route, $routeParams, $http, $location) {
	
	$scope.select = function ($event) {
		$event.preventDefault();
		var a = $event.target;
		var uri = a.getAttribute('data-uri');
		console.log(a);
		uri = uri.substr('spotify:'.length).split(/\:/g);
		uri = '/' + uri.join('/');
		$location.path(uri);
	};
}])
.config(['$routeProvider', '$locationProvider', function ($routeProvider, $locationProvider) {
	$routeProvider.when('/start', {
		templateUrl: 'views/start.html',
		controller: 'StartCtrl'
	})
	.when('/inbox', {
		templateUrl: 'views/inbox.html',
		controller: 'InboxCtrl'
	})
	.when('/user/:username/playlist/:playlist', {
		templateUrl: 'views/playlist.html',
		controller: 'PlaylistCtrl'
	})
	.when('/user/:username/starred', {
		templateUrl: 'views/playlist.html',
		controller: 'UserStarredCtrl'
	})
	.when('/library', {
		templateUrl: 'views/playlist.html',
		controller: 'LibraryCtrl'
	})
	.when('/search/:query', {
		templateUrl: 'views/playlist.html',
		controller: 'SearchCtrl'
	})
	.when('/artist/:artistId', {
		templateUrl: 'views/artist.html',
		controller: 'ArtistCtrl'
	}).when('/magic', {
		templateUrl: 'views/playlist.html',
		controller: 'MagicCtrl'
	}).when('/album/:albumId', {
		templateUrl: 'views/playlist.html',
		controller: 'AlbumCtrl'
	})
}])
