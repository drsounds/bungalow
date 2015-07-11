var fs = require('fs');
var SpotifyNodeApi = require('spotify-web-api-node');

var SpotifyPlayer = function () {
    var self = this;
    this.spotify = spotifyEngine;
    this.cache = {};
    this.isPlaying = false;
    
    this.resources = {};
    
    this.callbacks = {};
    this.apikeys = JSON.parse(fs.readFileSync('./spotify.key.json'));
    this.nodeSpotifyApi = new SpotifyNodeApi(this.apikeys);
    this.spotifyAPI = new SpotifyWebApi();
    this.me = null;

};

SpotifyPlayer.prototype.getAccessToken = function () {
    return JSON.parse(localStorage.getItem("accessToken", null));
}

SpotifyPlayer.prototype.setAccessToken = function (accessToken, callback) {
    localStorage.setItem('accessToken', JSON.encode(accessToken));
    this.spotifyAPI.setAccessToken(acccessToken);
    this.nodeSpotifyAPI.setAccessToken(acccessToken.accessToken);
    this.nodeSpotifyAPI.setRefreshToken(accessToken.refreshToken);

}

SpotifyPlayer.prototype.isAccessTokenValid = function () {
    return new Date() < new Date() + this.getAccessToken().expiresIn;
}

SpotifyPlayer.prototype.renewAccessToken = function (callback) {
    this.nodeSpotifyAPI.refreshAccessToken().then(function () {
        callback();
    })
}
SpotifyPlayer.prototype.getMe = function () {
    return JSON.parse(localStorage.getItem("me"));
}
SpotifyPlayer.prototype.request = function (method, url, data) {
    var self = this;
    var promise = new Promise(function (resolve, fail) {
        var activity = function () {
            var xhr = new XMLHttpRequest();
            xhr.onreadystatechange = function () {
                if (xhr.readyState == 4) {
                    if (xhr.status == 200) {
                        var data = JSON.parse(xhr.responseText);
                        success(data);
                    } else {
                        fail();
                    }
                }
            }
            var token = self.getAccessToken();
            xhr.open(method, "https://api.spotify.com/v1" + url, true);
            xhr.setRequestHeader("Authorization", "Bearer " + token);
            if (data instanceof Object) {
                xhr.setRequestHeader("Content-type", "application/json");
                xhr.send(JSON.stringify(object));
            } else {
                xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
                xhr.send(data);

            }
        }

        if (!this.isAccessTokenValid()) {
            this.renewAccessToken(activity);
        } else {
            activity();
        }

    });
    return promise;
}

SpotifyPlayer.prototype.requestAccessToken = function (code) {
    var self = this;
    var promise = new Promise(function (resolve, fail) {
        var xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function () {
            if (xhr.readyState == 4) {
                if (xhr.status == 200) {
                    var data = JSON.parse(xhr.responseText);
                    if (!('accessToken' in data)) {
                        fail({'error': 'Request problem'});
                        return;
                    }
                    self.nodeSpotifyAPI.setAccessToken(data);
                    self.nodeSpotifyAPI.getMe().then(function (data) {
                        localStorage.setItem("me", JSON.stringify(data.body));


                        success(data);
                    });
                } else {
                    fail();
                }
            }
        }
        xhr.open('POST', 'https://accounts.spotify.com/api/token', true);
        xhr.setRequestHeader("Authorization", "Basic " + new Buffer(self.apikeys.clientId).toString() + ':' + new Buffer(self.apikeys.clientSecret));

        xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
        xhr.send("grant_type=authorization_code&code=" + code + "&redirect_uri=" + encodeURI(self.apikeys.redirectURI));
    });
    return promise;
}


SpotifyPlayer.prototype.addToCache = function (resource) {
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

    return track;
}

SpotifyPlayer.prototype.stop = function () {
}

SpotifyPlayer.prototype.getImageForTrack = function (track, callback) {
    console.log(track);
    var parts = track.link.split(/\:/g);
    $.get('https://api.spotify.com/v1/tracks/' + parts[2], function (track) {
        callback(track.album.images[0].url);
    });
}

SpotifyPlayer.prototype.seek = function (position) {
}

SpotifyPlayer.prototype.login = function () {
    var self = this;
    var promise = new Promise(function (resolve, fail) {
        var win = window.open('https://accounts.spotify.com/authorize/?client_id=' + this.apikeys.clientId + '&response_type=code&redirect_uri=' + encodeURI(this.apiKeys.redirectUri) + '&scope=user-read-private%20user-read-email&state=34fFs29kd09');
        var i = setInterval(function () {
            if (!win) {
                clearInterval(i);
                var code = localStorage.getItem("code", null);
                if (code) {
                    self.requestAccessToken(code, function () {
                        resolve();
                    }, function () {
                        fail();
                    })
                }
            }
        }, 100);
    });
}

SpotifyPlayer.followPlaylist = function (playlist) {
    
}

var Uri = function (uri) {
    this.parts = uri.split(/\:/g);
    this.user = parts[2];
    this.playlist = parts[4];
    this.id = parts[3];
}

/**
 * Adds songs to a playlist
 **/
SpotifyPlayer.prototype.addTracksToPlaylist = function (uris, position, playlistURI) {
    var parts = playlistURI.split(/\:/g);
    this.request("POST", "/users/" + parts[1] + "/playlists/" + parts[3] + "/tracks", {
            "uris": uris, position: position
    }).then(function () {

    });

}

SpotifyPlayer.constants = spotifyEngine.constants;

SpotifyPlayer.prototype.getAlbumTracks = function (uri, callback) {
    uri = new Uri();
    this.request("GET", "/albums/" + uri.id + "/tracks").then(function (data) {
        callback(data);
    })
    
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
    uri = new Uri(uri);
    this.request("GET", "/users/" + uri.user + "/playlists/" + uri + "/tracks").then(function (tracklist) {
       this.request("GET",  "/users/" + uri.user + "/playlists/" + uri).then(function (playlist) {
            playlist.tracks = tracklist.tracks.items;
            callback(playlist);
       });
    });
}

SpotifyPlayer.prototype.createPlaylist = function (title, callback) {
    var me = this.getMe();
    this.request("GET", "/users/" + me.id + "/playlists", {name: title}).then(function (object) {

    });

};

SpotifyPlayer.prototype.getTopList = function (uri, callback) {

}

SpotifyPlayer.prototype.getUserPlaylists = function (callback, callback2) {
    var user = this.getMe();
    this.request("GET", "/users/" + user.id + '/playlists').then(function (data) {
        callback({
            'objects': data.items
        });
    });
}

SpotifyPlayer.prototype.getArtist = function (uri, callback) {
    var uri = new Uri(uri);
    this.request("GET", "/artists/" + uri.id).then(function (data) {
       callback(data);
    });
}

SpotifyPlayer.prototype.getAlbum = function (uri, callback) {
    uri = new Uri(uri);
    $.getJSON('https://api.spotify.com/v1/albums/' + parts[2], function (album) {
        album.image = album.images[0].url;
        album.tracks = [];
        this.request('GET', '/albums/' + uri.id + '/tracks').then(function (data) {
            album.tracks = data.tracks.items;
            callback(album);

        });
    });
}

SpotifyPlayer.prototype.resolveTracks = function (uris, callback) {

}

SpotifyPlayer.prototype.getPlaylistTracks = function (uri, page, callback) {
    uri = new Uri(uri);
    this.request('GET', '/users/' + uri.user + '/playlists/' + uri.playlist).then(function (data) {
      callback({
          'objects': data.tracks.items
      });
    })
}

SpotifyPlayer.prototype.playPause = function () {
    if (this.isPlaying) {
        this.pause();
    } else {
        this.resume();
    }
}
SpotifyPlayer.prototype.pause = function () {
    this.isPlaying = false;
    this.notify(new CustomEvent('trackpaused'));
}
SpotifyPlayer.prototype.resume = function () {
    this.isPlaying = true;
    this.notify(new CustomEvent('trackresumed'));
}
SpotifyPlayer.prototype.reorderTracks = function (playlistUri, indices, newPosition) {
    console.log("Spotify is now reordering tracks");
    console.log("Done successfully");
}

SpotifyPlayer.prototype.removeTracks = function (playlist, indices) {
    playlist.reorderTracks(indices, newPosition);
}

SpotifyPlayer.prototype.addTracks = function (playlist, tracks, position) {
    playlist.addTracks(tracks, position);
}