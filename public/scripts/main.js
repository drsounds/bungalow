var spotify = new SpotifyPlayer();
var Shell = function () {
	this.history = [];
	this.forward = [];
	this.uri = "";
	this.loadedResources = {};
	$(window).load(function () {
		$('#loginView').fadeIn();
		$('#loginView form').submit(function (event) {

		})
	});
	$(document).on('dragover', '.menu td', function (event) {
		$.event.props.push('dataTransfer');
		//if (event.originalEvent.dataTransfer.getData('text/uri-list').match(/((spotify\:user\:(.*)\:playlist\:(.*))+)/)) {
			event.preventDefault();
   			event.originalEvent.dataTransfer.dropEffect = 'copy';
   			var data = event.originalEvent.dataTransfer.getData('text/uri-list');
   			console.log(data);
   			$(this).addClass('sp-dragover');
		//}
	});
	$(document).on('dragleave', '.menu td', function (event) {
		
		$(this).removeClass('sp-dragover');
	
	});
	$(document).on('drop', '.menu td', function (event) {
		$.event.props.push('dataTransfer');
		var droppedURI = event.originalEvent.dataTransfer.getData('text/uri-list');
		console.log(droppedURI);

	});
	$(document).on('click', '.menu td', function (event) {
		$.event.props.push('dataTransfer');
		var uri = event.target.getAttribute('data-uri');
		console.log(event.target);
		//alert(event.target.getAttribute('data-uri'));
		self.navigate(uri);
	});
	$(document).on('dragstart', '.sp-track td', function (event) {
		$.event.props.push('dataTransfer');
		console.log("Begin drag");
		var uris = "";
		var $tracks = $('.sp-track-selected').each(function (i) {
			uris += $(this).attr('data-uri') + "\n";
		});


		event.dataTransfer.setData('text/uri-list', uris);
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
		if (event.data.action === 'getTopList') {
			spotify.getTopList(event.data.uri, function (toplist) {
				event.source.postMessage({'action': 'gotTopList', 'data': toplist}, '*');
			});
		}
		if (event.data.action === 'play') {
			$('#nowplaying_image').css({'background-image': 'initial'});
			console.log("Got play event");
			var context = JSON.parse(event.data.data);
			self.context = context;
			console.log(context);
			console.log("Context", context);
			event.source.postMessage({'action': 'trackstarted', 'index': context.currentIndex, 'uri': context.uri}, '*');
			self.playTrack(context.tracks[context.currentIndex]);
		}
		if (event.data.action === 'navigate') {
			self.navigate(event.data.uri);
		}
		if (event.data.action === 'getAlbum') {
			spotify.getAlbum(event.data.uri, function (album) {
				
				console.log(album);
				event.source.postMessage({'action': 'gotAlbum', 'data': album}, '*');
			});
		}

		if (event.data.action === 'search') {
			spotify.search(event.data.query, event.data.limit, event.data.offset, event.data.type, function (search) {
				
				console.log(search);
				event.source.postMessage({'action': 'gotSearch', 'data': search, 'type': event.data.type, 'query': event.data.query}, '*');
			});
		}

		if (event.data.action === 'getArtist') {
			spotify.getArtist(event.data.uri, function (artist) {
				
				console.log(artist);
				event.source.postMessage({'action': 'gotArtist', 'data': artist}, '*');
			});
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
			
		}
	}( jQuery ));

}

/**
 * Starts the process of new playlist
 **/
Shell.prototype.startCreatePlaylist = function () {
	var playlistName = prompt("Enter the name of the new playlist");
	if (playlistName) {
		console.log("Creating playlist");
		spotify.createPlaylist(playlistName, function (playlist, index) {
			var tr = document.createElement('tr');
			var td = document.createElement('td');
			td.setAttribute('data-uri', playlist.uri);
			tr.appendChild(td);
			td.innerHTML = '<span class="fa fa-music"></span> ' + playlist.name;
			$('#playlists tbody').prepend(tr);
		});
	}	
}

Shell.prototype.playTrack = function (track) {
	var track = spotify.playTrack(track.uri);
	$('#track_position').attr('max', track.duration);
	console.log("Duration", track.duration);
	$('#song_title').html(track.name);
	$('#song_arist').html(track.artists[0].name);
	spotify.getImageForTrack(track, function (image) {
		$('#nowplaying_image').css({'background-image': 'url("' + image + '")'});
	});	

}

Shell.prototype.login = function (event) {
	var self = this;
	event.preventDefault();
	spotify.login($('#username').val(), $('#password').val());
	$('#throbber').show();
	spotify.addEventListener('ready', function () {
		$('#loginView').fadeOut(function () {
			$('.darken').fadeOut(function () {
				self.navigate('spotify:search:friday');

				// Get user playlists
				spotify.getUserPlaylists(function (playlists) {
					for (var i =0; i < playlists.length && i < 100; i++) {
						var playlist = playlists[i];
						var listItem = document.createElement('tr');
						listItem.setAttribute('data-uri', playlist.uri);
						listItem.innerHTML = '<td data-uri="' + playlist.uri + '"><i class="fa fa-music"></i> ' + playlist.name + ' <span class="fade">by someone</span></td>';
						listItem.setAttribute('data-uri', playlist.uri);
						$('#playlists tbody').append(listItem);
						$(listItem).click(function (event) {
							var uri = event.target.getAttribute('data-uri');
							console.log(event.target);
							//alert(event.target.getAttribute('data-uri'));
							self.navigate(uri);
						});
					}
				}, function (playlist2) {
					var $item = $('.menu tr[data-uri="' + playlist.uri + '"]');
					$item.html(playlist2.name);
				});
				$('#throbber').hide();
			});
		});
	});
	return false;
}


Shell.prototype.searchEnter = function (event) {
	this.navigate($('#search').val());
	event.preventDefault();
	return false;
}

Shell.prototype.navigate = function (url, nohistory) {

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

	$('#menu td').removeClass('active');
	$('#menu td[data-uri="' + url + '"]').addClass('active');
	if (!nohistory) {
		this.future = [];
		this.history.push(url);
	}
	this.uri = url;
}

Shell.prototype.goBack = function () {
	var url = this.history.pop();
	if (url) {
		this.future.push(this.uri);
		this.navigate(url, true);
	}
}

Shell.prototype.goForward = function () {
	var url = this.future.pop();
	if (url) {
		this.history.push(this.uri);
		this.navigate(url, true);
	}
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
	var appURL = '';
	if (fs.existsSync(appDir) && fs.existsSync(manifestFilePath)) {
			var manifest = JSON.parse(fs.readFileSync(manifestFilePath));
			appName = appId;
			appURL = '/public/apps/' + appName + '/index.html';
	}  else {

	}

	var appFrame = document.createElement('iframe');
	appFrame.setAttribute('src', appURL);
	console.log('/apps/' + appName + '/index.html');
	appFrame.setAttribute('id', 'app_' + appId + '');
	appFrame.classList.add('sp-app');
	appFrame.setAttribute('nwdisable', 'nwdisable');
	appFrame.setAttribute('frameborder', '0');
	appFrame.setAttribute('width', "100%");
	appFrame.style = 'width:100%; height: 100%';
	$('#viewstack').append(appFrame);
	$(appFrame).css({'height': $('#viewstack').height()});
	this.apps[appId] = appFrame;

	return appFrame;
}

window.onresize = function () {
	$('iframe').css({'height': $('#viewstack').height()});
}


Shell.prototype.activateApp = function (appId) {
	$('iframe.sp-app').hide();
	$('iframe.sp-app#app_' + appId).show();
	
}

var shell = new Shell();


