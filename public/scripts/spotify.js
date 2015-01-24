var spotifyEngine = require('node-spotify')({ appkeyFile: 'spotify_appkey.key',traceFile: 'trace.txt'  });
	
var SpotifyPlayer = function () {
	var self = this;
	this.spotify = spotifyEngine;
	this.cache = {};
	this.isPlaying = false;
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



SpotifyPlayer.prototype.addToCache = function (resource) {
	this.cache[resource.uri] = resource;
}

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
	var data = {
		track: {
			'name': track.name,
			'uri': track.link
		}
	};
	var event = new CustomEvent('trackstarted');
	event.data = data;
	this.notify(event);
	return track;
}

SpotifyPlayer.prototype.stop = function () {
	this.spotify.player.stop();
}

SpotifyPlayer.prototype.getImageForTrack = function (track, callback) {
	console.log(track);
	var parts = track.link.split(/\:/g);
	$.get('https://api.spotify.com/v1/tracks/' + parts[2], function (track) {
		callback(track.album.images[0].url);
	});
}

SpotifyPlayer.prototype.seek = function (position) {
	this.spotify.player.seek(position);
}

SpotifyPlayer.prototype.login = function (username, password) {
	if (this.spotify.rememberedUser) {
		//alert("A");
		this.spotify.login(username, password, false, true);
	} else {
		this.spotify.login(username, password, true, false);
	}
	
}

SpotifyPlayer.followPlaylist = function (playlist) {
	
}

/**
 * Adds songs to a playlist
 **/
SpotifyPlayer.prototype.addTracksToPlaylist = function (uris, position, playlistURI) {
	var tracks = [];
	console.log("Adding tracks to playlist " + playlistURI);
	for (var i = 0; i < uris.length; i++) {
		var uri = uris[i];
		var track = this.spotify.createFromLink(uris[i]);
		tracks.push(track);
		console.log("Added track " + uri);
	}
	var playlist = this.spotify.createFromLink(playlistURI);
	console.log("Got playlist " + playlistURI);
	console.log("Adding tracks to playlist " + playlistURI);
	playlist.addTracks(tracks, position);
	console.log("Added tracks to playlist " + playlistURI);

}

SpotifyPlayer.prototype.getAlbumTracks = function (uri, callback) {
	var parts = uri.split(/\:/g);
	var album = this.spotify.createFromLink(uri);
	var tracks = [];
	album.browse( function(err, browsedAlbum) {
			for (var i = 0; i < browsedAlbum.tracks.length; i++) {
				var track = browsedAlbum.tracks[i];
				var track = ({
					'name': track.name + '',
					'uri': track.link + '',
					'artists': JSON.parse(JSON.stringify(track.artists)),
					'album': JSON.parse(JSON.stringify(track.album)),
					'availability': track.availability,
					'popularity': track.popularity,
					'duration': track.duration
				});
				tracks.push(track);
			}
		console.log(tracks);	
		callback(tracks);
	});
	
};


SpotifyPlayer.prototype.search = function (query, limit, offset, type, callback) {
	var self = this;
	$.getJSON('https://api.spotify.com/v1/search?q=' + encodeURI(query) + '&type=' + type + '&limit=' + limit + '&offset=' + offset, function (data) {
		if ('tracks' in data) {
			var tracks = data.tracks.items.map(function (track) {
				track.duration = track.duration_ms / 1000;
				return track;
			});
			callback(data.tracks.items);
		} else {
			callback(data[type + 's'].items);
		}
	});
};
SpotifyPlayer.prototype.loadPlaylist = function (uri, callback) {
	console.log("Loading playlist", uri);
	var playlist = this.spotify.createFromLink(uri);
	
	console.log("Waiting for playlist to load", playlist);
	if (playlist == null) {
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
			callback({
			'uri': playlist.link,
			'collaborative': playlist.collaborative,
			'name': playlist.name,
			'user': {
				'uri': playlist.owner.link,
				'canoncialName': playlist.owner.canoncialName,
				'displayName': playlist.owner.displayName
			},
			'description': playlist.description
			});
		});
	} else {

		callback(playlist);
	}
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
	var self = this;
	$.getJSON('https://api.spotify.com/v1/search?q=year:' + new Date().getFullYear()  + ( country !== 'world' ? '&market=' + country.toUpperCase() : '') + '&type=track&limit=50', function (data) {
		var tracks = data.tracks.items;
		tracks = tracks.sort(function (t1, t2) {
			return t1.popularity > t2.popularity;
		});
		tracks = tracks.map(function (track) {
			track.duration = track.duration_ms / 1000;
			return track;
		});
		for (var i = 0; i < tracks.length; i++) {
			self.addToCache(tracks[i]);
		}
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
			'uri': _playlists[i].link,
			'user': {
				'uri': _playlists[i].owner.link,
				'canoncialName': _playlists[i].owner.canoncialName,
				'displayName': _playlists[i].owner.displayName
			},
			'description': _playlists[i].description		
		};
		playlists.push(playlist);

	}
	callback(playlists);
	this.spotify.waitForLoaded(_playlists, function (playlist) {
		callback2({
			'name': playlist.name,
			'uri': playlist.link
		});
	});
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
	var self = this;

	$.getJSON('https://api.spotify.com/v1/albums/' + parts[2], function (album) {
		album.image = album.images[0].url;
		album.tracks = [];
		var _album = self.spotify.createFromLink(uri);
		var tracks = [];
		_album.browse( function(err, browsedAlbum) {
			for (var i = 0; i < browsedAlbum.tracks.length; i++) {
				var track = browsedAlbum.tracks[i];
				var track = ({
					'name': track.name + '',
					'uri': track.link + '',
					'artists': JSON.parse(JSON.stringify(track.artists)),
					'album': JSON.parse(JSON.stringify(track.album)),
					'availability': track.availability,
					'popularity': track.popularity,
					'duration': track.duration
				});
				album.tracks.push(track);
			}
			console.log(tracks);	
			callback(album);
		});
	});
}

SpotifyPlayer.prototype.resolveTracks = function (uris, callback) {
	var _tracks = [];
	var tracks = [];
	for (var i = 0; i < uris.length; i++) {
		_tracks.push(this.spotify.createFromLink(uris[i]));
	}
	var countTracks = uris.length;
	this.spotify.waitForLoaded(_tracks, function (track) {
		//console.log("Got tracks");	
		var track = ({
			'name': track.name + '',
			'uri': track.link + '',
			'artists': JSON.parse(JSON.stringify(track.artists)),
			'album': JSON.parse(JSON.stringify(track.album)),
			'availability': track.availability
		});
		self.addToCache(track);
		tracks.push(track);
	});
	var inz = setInterval(function () {
		if (countTracks <= tracks.length) {
			clearInterval(inz);
			console.log("Sending back tracks callback");
			callback(tracks);
		}
	}, 1000);
}

SpotifyPlayer.prototype.getPlaylistTracks = function (playlist, callback) {
	console.log("Waiting for loaded tracks");
	var tracks = [];
	var _tracks = playlist.getTracks();
	var self = this;
	console.log(_tracks.length);
	var countTracks = playlist.numTracks;
	if (countTracks > 1000) {
		countTracks = 1000;
	}
	for (var i = 0; i < countTracks; i++) {
		var track = _tracks[i];
		//console.log("Got tracks");	
		var track = ({
			'name': track.name + '',
			'uri': track.link + '',
			'artists': JSON.parse(JSON.stringify(track.artists)),
			'album': JSON.parse(JSON.stringify(track.album)),
			'user': {
				'uri': track.creator.link,
				'canoncialName': track.creator.canoncialName,
				'name': track.creator.displayName
			},
			'availability': track.availability,
			'added': track.createTime
		});
		self.addToCache(track);
		tracks.push(track);
	}
	var inz = setInterval(function () {
		if (tracks.length <= countTracks) {
			clearInterval(inz);
			console.log("Sending back tracks callback");
			callback(tracks);
		}
	}, 1000);
}

SpotifyPlayer.prototype.playPause = function () {
	if (this.isPlaying) {
		this.pause();
	} else {
		this.resume();
	}
}
SpotifyPlayer.prototype.pause = function () {
	this.spotify.player.pause();
	this.isPlaying = false;
	this.notify(new CustomEvent('trackpaused'));
}
SpotifyPlayer.prototype.resume = function () {
	this.isPlaying = true;
	this.spotify.player.resume();
	this.notify(new CustomEvent('trackresumed'));
}
SpotifyPlayer.prototype.reorderTracks = function (playlistUri, indices, newPosition) {
	console.log("Spotify is now reordering tracks");
	var playlist = this.spotify.createFromLink(playlistUri);
	playlist.reorderTracks(indices, newPosition);
	console.log("Done successfully");
}

SpotifyPlayer.prototype.removeTracks = function (playlist, indices) {
	playlist.reorderTracks(indices, newPosition);
}

SpotifyPlayer.prototype.addTracks = function (playlist, tracks, position) {
	playlist.addTracks(tracks, position);
}