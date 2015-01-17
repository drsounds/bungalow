var spotify = new SpotifyPlayer();
var Shell = function () {
	this.loadedResources = {};
	$(window).load(function () {
		$('#loginView').fadeIn();
		$('#loginView form').submit(function (event) {

		})
	});
	var self = this;
	spotify.addEventListener('trackended', function () {
		console.log(self.currentApp);
		self.context.currentIndex++;
		if (self.context.currentIndex < self.context.tracks.length) {
			var track = self.context.tracks[self.context.currentIndex];
			spotify.playTrack(track.uri);
			track = self.apps[self.currentApp].contentWindow.postMessage({'action': 'trackstarted', 'index': self.context.currentIndex, 'uri': self.context.uri}, '*');
			console.log(track.duration);

			$('#track_position').attr('max', track.duration);
		}
	});
	this.apps = {};
	window.onmessage = function (event) {
		console.log(event);
		if (event.data.action == 'activateApp') {
			console.log("Activate app event");
			self.currentApp = event.data.app;
		}
		if (event.data.action === 'play') {
			console.log("Got play event");
			var context = JSON.parse(event.data.data);
			self.context = context;
			console.log(context);
			track = spotify.playTrack(context.tracks[context.currentIndex].uri);
			console.log("Context", context);
			event.source.postMessage({'action': 'trackstarted', 'index': context.currentIndex, 'uri': context.uri}, '*');
			$('#track_position').attr('max', track.duration);
			console.log("Duration", track.duration);
		}
		if (event.data.action === 'getPlaylist') {
			console.log("TA", event.data.uri, self.loadedResources);
				//alert(event.data.uri);

				
			var playlist = self.loadedResources[event.data.uri];
			if (typeof(playlist) !== 'undefined') {
				console.log("Loaded from buffer", playlist);
				event.source.postMessage({'action': 'gotPlaylist', 'data': (playlist)}, event.origin);
				return;
			}
			var playlist = spotify.loadPlaylist(event.data.uri, function (playlist) {
				console.log("Playlist", playlist);

				var tracks = spotify.getPlaylistTracks(playlist, function (tracks) {
					playlist = {
						'name': playlist.name,
						'uri': playlist.link,
						'tracks': [],
						'isLoaded': true,
						'tracks': tracks
					};
					console.log("Sending playlist");
					self.loadedResources[playlist.uri] = playlist;
					event.source.postMessage({'action': 'gotPlaylist', 'data': (playlist)}, event.origin);
				});
			});
		}

	}
	this.currentApp = 'start';

	this.context = {
		'tracks': [],
		'currentIndex': 0,
		'uri': ''
	};
	var playback = setInterval(function () {
		$('#track_position').attr('value', spotify.getPosition());
	}, 100);
	(function ($) {
		$.fn.menulize = function (options) {
			$(this).find('td').click(function (event) {
				var uri = event.target.getAttribute('data-uri');
				console.log(event.target);
				//alert(event.target.getAttribute('data-uri'));
				self.navigate(uri);
			});
		}
	}( jQuery ));

}

Shell.prototype.login = function (event) {
	var self = this;
	event.preventDefault();
	spotify.login($('#username').val(), $('#password').val());
	spotify.addEventListener('ready', function () {
		$('#loginView').fadeOut(function () {
			$('#mainView').fadeIn();
			self.navigate('spotify:start');
		});
	});
	return false;
}


Shell.prototype.searchEnter = function (event) {
	this.navigate($('#search').val());
	event.preventDefault();
	return false;
}

Shell.prototype.navigate = function (url) {
	if (url.indexOf('spotify:') !== 0) {
		url = 'spotify:search:' + url;
	}

	var parts = url.substr('spotify:'.length).split(/\:/g);
	var appId = parts[0];
	var args = parts.slice(1);
	if (url.match(/spotify\:user\:(.*)\:playlist\:(.*)/)) {
		appId = 'playlist';
	
	}

	if (this.isAppLoaded(appId)) {
		console.log("App is already loaded", appId);
		this.activateApp(appId);
		var appFrame = this.getAppFrame(appId);
		appFrame.contentWindow.postMessage({'action': 'navigate', 'arguments': (args)}, '*');
		

	} else {



		var appFrame = this.createApp(appId);
		appFrame.onload = function (event) {
			console.log("Sending initial message");
			appFrame.contentWindow.postMessage({'action': 'navigate', 'arguments': args}, '*');
		};
		this.activateApp(appId);

	}

	$('.menu tr').removeClass('active');
	$('.menu tr[data-uri^="' + url + '"]').addClass('active');

}

/**
 * Returns if an app is loaded
 **/
Shell.prototype.isAppLoaded = function (appId) {
	return document.querySelector('iframe#app_' + appId ) != null;
}

Shell.prototype.getAppFrame = function (appId) {
	return document.querySelector('iframe#app_' + appId );
}


Shell.prototype.createApp = function (appId) {
	var path = require('path');
	var fs = require('fs');
	var appDir = 'public' + path.sep + 'apps' + path.sep + appId + path.sep;
	var manifestFilePath = appDir + path.sep + 'manifest.json';
	// check if app is already existing
	// check if directory exists
	var appName = 'notfound';
	if (fs.existsSync(appDir) && fs.existsSync(manifestFilePath)) {
			var manifest = JSON.parse(fs.readFileSync(manifestFilePath));
			appName = appId;
	}  else {
		appName = 'notfound';
	}

	var appFrame = document.createElement('iframe');
	appFrame.setAttribute('src', '/public/apps/' + appName + '/index.html');
	console.log('/apps/' + appName + '/index.html');
	appFrame.setAttribute('id', 'app_' + appId + '');
	appFrame.classList.add('sp-app');
	appFrame.setAttribute('nwdisable', 'nwdisable');
	appFrame.setAttribute('frameborder', '0');
	appFrame.setAttribute('width', "100%");
	appFrame.setAttribute('height', "100%");
	appFrame.style = 'width:100%; height: 100%';
	$('#viewstack').append(appFrame);
	this.apps[appId] = appFrame;

	return appFrame;
}


Shell.prototype.activateApp = function (appId) {
	$('iframe.sp-app').hide();
	$('iframe.sp-app#app_' + appId).show();
	
}

var shell = new Shell();


