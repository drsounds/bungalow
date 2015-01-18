var spotifyEngine = require('node-spotify')({ appkeyFile: 'spotify_appkey.key',traceFile: 'trace.txt'  });
	
var SpotifyPlayer = function () {
	var self = this;
	this.spotify = spotifyEngine;
	this.spotify.on({
		ready: function (args) {
			console.log(spotifyEngine);
			spotifyEngine.playlistContainer.on({
				'playlistAdded': function (err, newPlaylist, position) {
					console.log(self.callbacks);
					alert(self.callbacks);
					self.callbacks['playlistAdded'].call(self, newPlaylist, position);
				}
			});
			self.notify(new CustomEvent('ready'));
		}
	});
	this.spotify.player.on({
	    endOfTrack: function() {
	    	self.notify(new CustomEvent('trackended'))
	    }
	});
	this.resources = {};
	
	this.callbacks = {};
};

SpotifyPlayer.prototype.events = {};

SpotifyPlayer.prototype.notify = function (event) {
	var type = event.type;
	if (type in this.events) {
		this.events[type].call(this, event);
	}
}

SpotifyPlayer.prototype.addEventListener = function (event, callback) {
	this.events[event] = callback;
}

SpotifyPlayer.prototype.ready = function () {

}

SpotifyPlayer.prototype.getPosition = function () {
	return this.spotify.player.currentSecond;
}

SpotifyPlayer.prototype.logout = function () {
	this.spotify.logout();
}

SpotifyPlayer.prototype.playTrack = function (uri) {
	console.log(uri);
	var track = this.spotify.createFromLink(uri);
	//alert(uri);
	console.log(track);
	console.log("Start playing song");
	this.spotify.player.play(track);
	return track;
}

SpotifyPlayer.prototype.getImageForTrack = function (track, callback) {
	var parts = track.uri.split(/\:/g);
	$.get('https://api.spotify.com/v1/tracks/' + parts[1], function (track) {
		callback(track.album.images[0].url);
	});
}

SpotifyPlayer.prototype.seek = function (position) {
	this.spotify.player.seek(position);
}

SpotifyPlayer.prototype.login = function (username, password) {
	if (this.spotify.rememberedUser) {
		alert("A");
		this.spotify.login(username, password, false, true);
	} else {
		this.spotify.login(username, password, true, false);
	}
	
}



SpotifyPlayer.prototype.search = function (query, limit, offset, callback) {
	var self = this;
	$.getJSON('https://api.spotify.com/v1/search?q=' + encodeURI(query) + '&type=track&limit=' + limit + '&offset=' + offset, function (data) {
		var tracks = data.tracks.items.map(function (track) {
			track.duration = track.duration_ms / 1000;
			return track;
		});
		callback(data.tracks.items);
	});
};
SpotifyPlayer.prototype.loadPlaylist = function (uri, callback) {
	console.log("Loading playlist", uri);
	var playlist = this.spotify.createFromLink(uri);
	
	console.log("Waiting for playlist to load", playlist);
	this.spotify.waitForLoaded([playlist], function (playlist) {
		playlist.on({
		    playlistRenamed: function(err, playlist) {

		    },
		    tracksAdded: function(err, playlist, track, position) {

		    },
		    tracksMoved: function(err, playlist, trackIndices, newPosition) {},
		    tracksRemoved: function(err, playlist, trackIndices) {},
		    trackCreatedChanged: function(err, playlist, position, user, date) {},
		    trackSeenChanged: function(err, playlist, position, seen) {},
		    trackMessageChanged: function(err, playlist, position, message) {}
		});
		console.log("Playlist loaded", playlist);
		console.log(playlist);
		callback(playlist);
	});
}

SpotifyPlayer.prototype.createPlaylist = function (title, callback) {
	console.log(title);
	this.spotify.playlistContainer.addPlaylist(title);
	var self = this;
	console.log("Adding playlist");
	this.callbacks['playlistAdded'] = function (_playlist, position) {
		playlist = {
			'name': _playlist.name,
			'uri': _playlist.link
		};
		self.spotify.movePlaylist(position, 0);
		callback(playlist, position);
	};
};

SpotifyPlayer.prototype.getTopList = function (uri, callback) {
	var parts = uri.split(/\:/g);
	var country = parts[3];
	$.getJSON('https://api.spotify.com/v1/search?q=year:' + new Date().getFullYear()  + ( country !== 'world' ? '&market=' + country.toUpperCase() : '') + '&type=track&limit=50', function (data) {
		var tracks = data.tracks.items;
		tracks = tracks.sort(function (t1, t2) {
			return t1.popularity > t2.popularity;
		});
		tracks = tracks.map(function (track) {
			track.duration = track.duration_ms / 1000;
			return track;
		});
		callback({
			'title': 'Top list',
			'tracks': tracks,
			'uri': uri
		});
	});
}

SpotifyPlayer.prototype.getUserPlaylists = function (callback, callback2) {
	console.log("Getting user playlists");
	var _playlists = this.spotify.playlistContainer.getPlaylists();
	var playlists = [];
//	console.log(_playlists);
	for (var i = 0; i < _playlists.length; i++) {
		//console.log(_playlists[i].name);
		var playlist = {
			'name': _playlists[i].name,
			'uri': _playlists[i].link
		};
		playlists.push(playlist);

	}
	callback(playlists);
/*	this.spotify.waitForLoaded(_playlists, function (playlist) {
		callback2({
			'name': playlist.name,
			'uri': playlist.link
		});
	});*/
}

SpotifyPlayer.prototype.getArtist = function (uri, callback) {
	var parts = uri.split(/\:/g);
	$.getJSON('https://api.spotify.com/v1/artists/' + parts[2], function (artist) {
		$.getJSON('https://api.spotify.com/v1/artists/' + parts[2] + '/albums?album_type=single', function (singles) {
			$.getJSON('https://api.spotify.com/v1/artists/' + parts[2] + '/albums?album_type=album', function (albums) {
				artist.singles = singles.items;
				artist.albums = albums.items;
				//alert(artist.singles[0].name == artist.albums[0].name);
				artist.image = artist.images[0].url;
				callback(artist);
			});
		});
	});
}

SpotifyPlayer.prototype.getAlbum = function (uri, callback) {
	var parts = uri.split(/\:/g);
	$.getJSON('https://api.spotify.com/v1/albums/' + parts[2], function (album) {
		album.image = album.images[0].url;
		album.tracks = album.tracks.items;
		album.tracks = album.tracks.map(function (track) {
			track.duration = track.duration_ms / 1000;
			return track;
		});
		callback(album);
	});
}

SpotifyPlayer.prototype.getPlaylistTracks = function (playlist, callback) {
	console.log("Waiting for loaded tracks");
	var tracks = [];
	var _tracks = playlist.getTracks();
	console.log(_tracks.length);
	this.spotify.waitForLoaded(_tracks, function (track) {
		//console.log("Got tracks");	
		tracks.push({
			'name': track.name + '',
			'uri': track.link + '',
			'artists': JSON.parse(JSON.stringify(track.artists)),
			'album': JSON.parse(JSON.stringify(track.album)),
			'user': {
				'link': 'spotify:user:drsounds',
				'canoncialName': 'drsounds',
				'name': 'Dr. Sounds'
			}
		});
	});
	var inz = setInterval(function () {
		console.log(tracks.length);
		if (tracks.length === playlist.numTracks) {
			clearInterval(inz);
			console.log("Sending back tracks callback");
			callback(tracks);
		}
	}, 100);
}
SpotifyPlayer.prototype.reorderTracks = function (playlist, indices, newPosition) {
	playlist.reorderTracks(indices, newPosition);
}

SpotifyPlayer.prototype.removeTracks = function (playlist, indices) {
	playlist.reorderTracks(indices, newPosition);
}

SpotifyPlayer.prototype.addTracks = function (playlist, tracks, position) {
	playlist.addTracks(tracks, position);
}