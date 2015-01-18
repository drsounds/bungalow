var SpotifyPlayer = function () {
	this.spotify = require('node-spotify')({ appkeyFile: 'spotify_appkey.key',traceFile: 'trace.txt'  });
	var self = this;
	this.spotify.on({
		ready: function (args) {
			self.notify(new CustomEvent('ready'));
		}
	});
	this.spotify.player.on({
	    endOfTrack: function() {
	    	self.notify(new CustomEvent('trackended'))
	    }
	});
	this.resources = {};

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
	console.log("TRACK", track);
	var album = this.spotify.createFromLink(track.album.link);
	console.log("ALBUM", album);
	this.spotify.waitForLoaded([album], function (album) {
		console.log(album);
		callback('data:image/jpeg;base64,' + album.getCoverBase64());
	});
}

SpotifyPlayer.prototype.seek = function (position) {
	this.spotify.player.seek(position);
}

SpotifyPlayer.prototype.login = function (username, password) {
	this.spotify.login(username, password);
		
	
}



SpotifyPlayer.search = function (query, limit, offset, callback) {
	var self = this;
	$.getJSON('https://api.spotify.com/v1/search?q=' + encodeURI(query) + '&type=track&limit=' + limit + '&offset=' + offset, function (data) {
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


SpotifyPlayer.prototype.getUserPlaylists = function (callback, callback2) {
	console.log("Getting user playlists");
	var _playlists = this.spotify.playlistContainer.getPlaylists();
	var playlists = [];
	console.log(_playlists);
	for (var i = 0; i < _playlists.length; i++) {
		console.log(_playlists[i].name);
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
	var artist = this.spotify.createFromLink(uri);
	artist.browse( this.spotify.constants.ARTISTBROWSE_NO_TRACKS, function(err, browsedArtist) {
	    var albums = [];
	    for (var i = 0; i < browsedArtist.albums.length; i++) {
	    	var album = browsedArtist.albums[i];
	    	console.log("ALBUM", album);
	    /*	var tracks = [];
	    	for (var i = 0; i < album.tracks.length; i++) {
	    		var track = album.tracks[i];
	    		tracks.append({
	    			'name': track.name,
	    			'uri': track.link,
	    			'duration': track.duration,
	    			'artists': [{
	    				'name': browsedArtist.name,
	    				'uri': uri
	    			}],
	    			'album': {
	    				'name': album.name,
	    				'uri': album.uri
	    			}
	    		})
	    	}*/
	    	albums.push({
	    		'name': album.name,
	    		'artist': {
	    			'name': browsedArtist.name,
	    			'uri': uri
	    		},
	    		// 'tracks': tracks,
	    		'uri': album.link,
	    		'image': 'data:image/jpeg;base64,' + album.getCoverBase64()
	    	});
	    }
	    var similarArtists = [];
	   	for (var i = 0; i < browsedArtist.similarArtists.length; i++) {
	    	var artist = browsedArtist.similarArtists[i];
	    	similarArtists.push({
	    		'name': artist.name,
	    		'uri': artist.uri
	    	});
	    }
	    var object = {
	    	'name':browsedArtist.name,
	    	'uri': uri,
	    	'albums': albums,
	    	'biography': browsedArtist.biography,
	    	'similarArtists': similarArtists
	    };
	   	callback(object);
	});
}

SpotifyPlayer.prototype.getAlbum = function (uri, callback) {
	var album = this.spotify.createFromLink(uri);
	album.browse(function (err, browsedAlbum) {
		var tracks = [];
		for (var i = 0; i < browsedAlbum.tracks.length; i++) {
			var track = browsedAlbum.tracks[i];
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
		}
		callback({
			'uri': uri,
			'name': browsedAlbum.name,
			'tracks': tracks,
			'review': browsedAlbum.review,
			'copyrights': browsedAlbum.copyrights,
			'artist': {
				'name': browsedAlbum.artist.name,
				'uri': browsedAlbum.artist.link
			},
			'image': 'data:image/jpeg;base64,' + album.getCoverBase64()
		});
	});
}

SpotifyPlayer.prototype.getPlaylistTracks = function (playlist, callback) {
	console.log("Waiting for loaded tracks");
	var tracks = [];
	var _tracks = playlist.getTracks();
	console.log(_tracks.length);
	this.spotify.waitForLoaded(_tracks, function (track) {
		console.log("Got tracks");	
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