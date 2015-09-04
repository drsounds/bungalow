var Music = function () {

};

var XHR = function () {

}

XHR.prototype.request = function (method, url, params, data) {
	return new Promise(function (resolve, fail) {
		var xhr = new XMLHttpRequest();

		xhr.onreadystatechange = function () {
			if (xhr.readyState == 4 && xhr.status == 200) {
				var data = xhr.response;
				resolve(data);
			}
		};
		xhr.responseType = 'json';
		xhr.open(method, url, true);
		xhr.send(data);
	});
};

Music.prototype.request = function (method, url) {
	return new Promise(function (resolve, fail) {
		new XHR().request(method, '/api/music' + url).then(function (result) {
			resolve(result);
		});
	});
};

Music.prototype.addEventListener = function () {

}

Music.prototype.getAlbum = function (id) {
	return new Promise(function (resolve, fail) {
		new XHR().request('GET', '/api/albums/' + id).then(function (data) {
			new XHR().request('GET', '/api/albums/' + id + '/tracks').then(function (album) {
				data.tracks = album.tracks;
				resolve(data);
			});
		});
	});
}

Music.prototype.login = function () {
	return new Promise(function (resolve, fail) {
		/* var loginWindow = window.open('https://accounts.spotify.com/authorize?client_id=d4dc306c3fe643a6933b35ee18ed4d89&scope=user-read-private&response_type=code&redirect_uri=' + encodeURI('http://play.bungalow.qi/callback.html'));
		var t = setInterval(function () {
			if (!loginWindow) {
				clearInterval(t);

			}
		}); */
	});
}

var music = new Music();

// http://stackoverflow.com/questions/391314/jquery-insertat
$.fn.insertAt = function(index, $parent) {
    return this.each(function() {
        if (index === 0) {
            $parent.prepend(this);
        } else {
            $parent.children().eq(index - 1).after(this);
        }
    });
}


/**
 * from http://stackoverflow.com/questions/586182/insert-item-into-array-at-a-specific-index
 **/
Array.prototype.insert = function (index, item) {
  this.splice(index, 0, item);
};

var LocalSettings = function () {

}

var Shell = function () {
	var self = this;
	this.mashcast = new Mashcast();

	this.mashcast.addEventListener('episodestopped', function (event) {
	});


	var self = this;
	$(document).keydown = function (event) {
		if (event.keyCode === 171) {
			// On Windows X
			self.playPause();
		}
	}
	// Global music resource buffer
	this.resourceBuffer = {};

	this.cache = {};
	this.history = [];
	this.forward = [];
	this.uri = "";
	this.loadedResources = {};
	$(window).load(function () {
		shell.login();
	});
	$(document).on('dragover', '.menu li', function (event) {
		$.event.props.push('dataTransfer');
		//if (event.originalEvent.dataTransfer.getData('text/uri-list').match(/((music\:user\:(.*)\:playlist\:(.*))+)/)) {
			event.preventDefault();
   			event.originalEvent.dataTransfer.dropEffect = 'copy';
   			var data = event.originalEvent.dataTransfer.getData('text/uri-list');
   			console.log(data);
   			$(this).addClass('sp-dragover');
		//}
	});
	$(document).on('dragleave', '.menu li', function (event) {
		
		$(this).removeClass('sp-dragover');
	
	});
	$(document).on('drop', '.menu li', function (event) {
		$.event.props.push('dataTransfer');
		var droppedURI = event.originalEvent.dataTransfer.getData('text/uri-list');
		console.log(droppedURI);
		$(event.originalEvent.target).removeClass('sp-dragover');
		console.log("Adding tracks to playlist");
		var uris = droppedURI.split(/\n/g);
		console.log(event.originalEvent.target);
		var playlistURI = event.originalEvent.target.getAttribute('data-uri');
		music.addTracksToPlaylist(uris, 0, playlistURI);
		music.resolveTracks(uris, function (tracks) {

			var playlistIframe = document.querySelector('iframe#app_playlist');
			if (playlistIframe) {
				playlistIframe.postMessage({
					'action': 'tracksadded',
					'uri': playlistURI,
					'position': 0,
					'tracks': tracks
				});
			}
		});
	});
	$(document).on('click', '.menu li', function (event) {
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
	music.addEventListener('trackstarted', function () {
		$('#btnPlay').removeClass('fa-play');
		$('#btnPlay').addClass('fa-pause');
		
	});
	music.addEventListener('trackresumed', function () {

		$('#btnPlay').removeClass('fa-play');
		$('#btnPlay').addClass('fa-pause');
	});
	music.addEventListener('trackpaused', function () {

		$('#btnPlay').addClass('fa-play');
		$('#btnPlay').removeClass('fa-pause');
	});
	
	music.addEventListener('trackended', function () {
		console.log(self.currentApp);
		$('#btnPlay').removeClass('fa-pause');
		$('#btnPlay').addClass('fa-play');
		self.context.currentIndex++;
		if (self.context.currentIndex < self.context.tracks.length) {
			var track = self.context.tracks[self.context.currentIndex];
			music.playTrack(track.uri);
			self.apps[self.currentApp].contentWindow.postMessage({'action': 'trackstarted', 'index': self.context.currentIndex, 'uri': self.context.uri}, '*');
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
			music.getTopList(event.data.uri, function (toplist) {
				event.source.postMessage({'action': 'gotTopList', 'data': toplist}, '*');
			});
		}

		if (event.data.action === 'reorderedtracks') {
			// Occurs on reorder tracks
			var playlistUri = event.data.context;
			var oldIndex = event.data.oldIndex;
			var newIndex = event.data.newIndex;
			var indicies = event.data.indicies;
			var numTracks = event.data.uris.length;
			console.log("Reordering ", indicies, 	" in playlist " + playlistUri + ' from position ' + oldIndex + ' to position ' + newIndex);

			music.reorderTracks(playlistUri, indicies, newIndex);
		}

		if (event.data.action === 'contextupdate') {
			if (self.context.uri === event.data.uri) {
				
			}
		}

		if (event.data.action === 'play') {
			console.log(event.data.track.availability);
			if (event.data.track.availability !== 1) {
				alert("Track is not available");
				return;
			}

			$('#nowplaying_image').css({'background-image': 'initial'});
			console.log("Got play event");
			var context = JSON.parse(event.data.data);
			self.context = context;
			console.log(context);
			console.log("Context", context);
			//alert(context.uri);
			event.source.postMessage({'action': 'trackstarted', 'index': context.currentIndex, 'uri': context.uri}, '*');
			self.playTrack(context.tracks[context.currentIndex]);
		}
		if (event.data.action === 'hashchange') {
			window.location.hash = event.data.hash;
			console.log("Changed hash");
		}
		if (event.data.action === 'navigate') {
			self.navigate(event.data.uri);
		}
		if (event.data.action === 'getAlbum') {
			var id = event.data.uri.split(/\:/)[2];
			music.request('GET', '/albums/' + id).then(function (album) {
				music.request('GET', '/albums/' + id + '/tracks').then(function (tracklist) {
					album.tracks = tracklist.objects;
					console.log(album);
					event.source.postMessage({'action': 'gotAlbum', 'data': album}, '*');
				});
			});
		}
		if (event.data.action === 'getConfig') {
			var config = bungalow_load_settings();
			console.log(config);
			event.source.postMessage({'action': 'gotConfig', 'config': config}, '*');
		}
		if (event.data.action === 'setConfig') {

			var config = bungalow_load_settings();
			console.log(event.data.config);
			$.extend(config, event.data.config); // Merge two configs
			console.log("Config", config);
			if (config) {
				bungalow_save_settings(config);
				if (confirm("You must restart Bungalow in order apply the new settings. Want to restart?")) {
					window.reload(true);
				}
			}

		}
		if (event.data.action === 'search') {
			music.request('GET', '/search/?q=' + encodeURI(event.data.query) + '&limit=' + event.data.limit + '&offest=' + event.data.offset + '&type=' + event.data.type).then(function (search) {
				
				event.source.postMessage({'action': 'gotSearch', 'data': search, 'type': event.data.type, 'query': event.data.query}, '*');
			});
		}

		if (event.data.action === 'getAlbumTracks') {
			var id = event.data.uri.split(/\:/g)[2];
			music.request('GET', '/albums/' + id + '/tracks').then(function (tracks) {
				event.source.postMessage({'action': 'gotAlbumTracks', 'uri': event.data.uri, 'tracks': tracks}, '*');
			});
		}

		if (event.data.action === 'getArtist')
		{
			var id = event.data.uri.split(/\:/g)[2];
			music.request('GET', '/artists/' + id).then(function (artist) {
				
				console.log(artist);
				event.source.postMessage({'action': 'gotArtist', 'data': artist}, '*');
			});
		}

		if (event.data.action === 'getPlaylist') {
			//console.log("TA", event.data.uri, self.loadedResources);
				//alert(event.data.uri);

				
			var playlist = self.loadedResources[event.data.uri];
			if (typeof(playlist) !== 'undefined') {
				console.log("Loaded from buffer", playlist);
				event.source.postMessage({'action': 'gotPlaylist', 'data': (playlist)}, event.origin);
				return;
			}
			var parts = event.data.uri.split(/\:/g);

			var playlist = music.request('GET', '/users/' + parts[2] + '/playlists/' + parts[4]).then(function (playlist) {
				console.log("Playlist", playlist);

				var tracks = music.getPlaylistTracks(playlist, function (tracks) {
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
		try {
			document.querySelector('#track_position').value = music.getPosition();
			//console.log("Position");
		} catch (e) {

		}
	}, 100);
	(function ($) {
		$.fn.menulize = function (options) {
			
		}
	}( jQuery ));

}

Shell.prototype.addApp = function (app) {
	var settings = bungalow_load_settings();
	settings.apps.push(app);
	bungalow_save_settings(app);
}

Shell.prototype.cacheResource = function (uri, resource) {
	this.resourceBuffer[uri] = resource;
}

Shell.prototype.playPause = function () {
	music.playPause();
}

/**
 * Starts the process of new playlist
 **/
Shell.prototype.startCreatePlaylist = function () {
	var playlistName = prompt("Enter the name of the new playlist");
	if (playlistName) {
		console.log("Creating playlist");
		music.createPlaylist(playlistName, function (playlist, index) {
			var tr = document.createElement('tr');
			var td = document.createElement('td');
			td.setAttribute('data-uri', playlist.uri);
			tr.appendChild(td);
			td.innerHTML = '<span class="fa fa-music"></span> ' + playlist.name;
			$('#playlists tbody').insertAt(1);
		});
	}	
}

Shell.prototype.playTrack = function (track) {
	var track = music.playTrack(track.uri);
	$('#track_position').attr('max', track.duration);
	console.log("Duration", track.duration);
	$('#song_title').html(track.name);
	$('#song_arist').html(track.artists[0].name);
	console.log("Getting image for track");
	music.getImageForTrack(track, function (image) {
		console.log("Got image for track");
		$('#nowplaying_image').css({'background-image': 'url("' + image + '")'});
	});	

}

Shell.prototype.login = function (event) {
	var self = this;
	console.log("A");
	music.login().then(function (ready) {
		// Get user playlists
		music.getUserPlaylists(function (playlists) {
			for (var i =0; i < playlists.length && i < 100; i++) {
				var playlist = playlists[i];
				var listItem = document.createElement('tr');
				listItem.setAttribute('data-uri', playlist.uri);
				console.log(playlist.user);
				listItem.innerHTML = '<li data-uri="' + playlist.uri + '"><i class="fa fa-music"></i> ' + playlist.name /*+ ' <span class="fade">by ' + playlist.user.displayName + '</span></li>'*/;
				listItem.setAttribute('data-uri', playlist.uri);
				$('#playlists').append(listItem);
				$(listItem).click(function (event) {
					var uri = event.target.getAttribute('data-uri');
					console.log(event.target);
					//alert(event.target.getAttribute('data-uri'));
					self.navigate(uri);
				});
			}
		}, function (playlist) {
			var $item = $('.menu tr[data-uri="' + playlist.uri + '"]');
			$item.html('<li data-uri="' + playlist.uri + '"><i class="fa fa-music"></i> ' + playlist.name + ' <span class="fade">by ' + playlist.user.displayName + '</span></li>');

		});
	});
	$('#throbber').show();

	// Add app to sidebar
	var settings = bungalow_load_settings();
	for (var i = 0; i < settings.apps.length; i++) {
		
		var app = settings.apps[i];
		console.log(app);
		var tr = document.createElement('li');
		var icon = app.icon.indexOf('fa') === 0 ? '<span class="fa ' + app.icon + '"></span> ' : '';
		tr.setAttribute('data-uri', app.uri);
		tr.innerHTML = '' + icon + app.name + '';
		//alert(tr.innerHTML);
		$('#app').append(tr);
	}



	return false;
}


Shell.prototype.searchEnter = function (event) {
	event.preventDefault();
	this.navigate($('#search').val());
	return false;
}

Shell.prototype.navigate = function (url, nohistory) {
	var q = url;
	if (url.indexOf('#') === 0) {
		url = 'bungalow:hashtag:' + url.substr(1);
	}
	if (url.indexOf('spotify:') == 0) {
		url = 'bungalow:' + url.split(/\:/g).slice(1).join(':');
	}
	if (url.indexOf('bungalow:') !== 0) {
		url = 'bungalow:search:' + url;
	}
	if (url.indexOf('bungalow:app:') !== 0) {
		url = 'bungalow:' + url.split(/\:/g).slice(1).join(':');
	}

	if (!nohistory) {
		history.pushState(url, "Bungalow", '/' +url.split(/\:/g).slice(1).join('/'));
	}

	if (url.indexOf('bungalow:search:') === 0) {

		try {
		var uri = url;	
		console.log("Adding search query " + q);
		// Add search history
		var searchTable = document.querySelector('#searchHistory tbody');
		console.log($('li[data-uri="' + uri + '"]'));
		if ($('li[data-uri="' + uri + '"]').length < 1) {
			if (searchTable.childNodes.length < 1) {
				// Append divider
				$(searchTable).html('');
			}

			// Now append search query
			var tr = document.createElement('tr');
			tr.setAttribute('data-uri', uri);
			tr.innerHTML = '<li data-uri="' + uri + '"><i class="fa fa-search"></i> ' + q + '</li>';
			$(searchTable).eq(0).after(tr);
			if (searchTable.childNodes.length > 5) {
				$(searchTable).get(searchTable.childNodes.length - 2).remove();
			}
		}
	} catch (e) {
		console.log(e.stack);	
	}
	}

	var parts = url.substr('bungalow:'.length).split(/\:/g);
	var appId = parts[0];
	var args = parts.slice(1);
	if (url.match(/bungalow\:user\:(.*)\:playlist\:(.*)/)) {
		appId = 'playlist';
	
	}

	if (this.isAppLoaded(appId)) {
		console.log("App is already loaded", appId);
		this.activateApp(appId);
		var appFrame = this.getAppFrame(appId);
		appFrame.contentWindow.postMessage({'action': 'navigate', 'arguments': (args)}, '*');

	} else {

		var self = this;

		var appFrame = this.createApp(appId, function (appFrame) {
			appFrame.onload = function (event) {
				console.log("Sending initial message");
				appFrame.contentWindow.postMessage({'action': 'navigate', 'arguments': args}, '*');
			};
			self.activateApp(appId);
		});
	}
	this.currentApp = appId;

	$('#menu li').removeClass('active');
	$('#menu li[data-uri="' + url + '"]').addClass('active');
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


Shell.prototype.createApp = function (appId, callback) {


	var appFrame = document.createElement('iframe');
	appFrame.setAttribute('src', 'http://appfinder.bungalow.qi/' + appId + '/index.html?t=' + new Date().getTime());
	console.log('/apps/' + appId + '/index.html');
	appFrame.setAttribute('id', 'app_' + appId + '');
	appFrame.classList.add('sp-app');
	appFrame.setAttribute('nwdisable', 'nwdisable');
	appFrame.setAttribute('frameborder', '0');
	appFrame.setAttribute('width', "100%");
	appFrame.style = 'width:100%; height: 100%';
	$('#viewstack').append(appFrame);
	$(appFrame).css({'height': $('#viewstack').height()});
	self.apps[appId] = appFrame;

	callback(appFrame);


}

Shell.prototype.alert = function (message) {
	$('#alert').show();
	$('#alert').html('<p>' + message + '</p>');
}

window.onresize = function () {
	$('iframe').css({'height': $('#viewstack').height()});
}


Shell.prototype.activateApp = function (appId) {
	$('iframe.sp-app').hide();
	$('iframe.sp-app#app_' + appId).show();
	
}

var shell = new Shell();
(function ($) {
		$.fn.menulize = function (options) {

		}
}( jQuery ));
window.alert = shell.alert;

window.addEventListener('popstate', function (event) {
	var url = event.state;
	shell.navigate(url, true);
});

window.addEventListener('load', function () {
	var location = 'bungalow:' + window.location.pathname.substr(1).split(/\//g).join(':');
	if (location == 'bungalow:') {
		location = 'bungalow:start';
	}
	console.log(location);
	shell.navigate(location, true);
	setHash(window.location.hash.slice(1));
});
function setHash(hash) {
	console.log("Hash changed");
	console.log(shell.apps[shell.currentApp]);
	document.querySelector('iframe#app_' + shell.currentApp).contentWindow.postMessage({ 'action': 'hashchange', 'hash': hash}, '*');
}
window.addEventListener('hashchange', function (event) {
	setHash(window.location.hash.slice(1));
});
