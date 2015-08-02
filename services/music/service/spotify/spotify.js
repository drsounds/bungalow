var fs = require('fs');
var SpotifyNodeApi = require('spotify-web-api-node');
var SpotifyWebApi = require('spotify-web-api');
var request = require('request');
var SpotifyPlayer = function () {
    var self = this;
    this.cache = {};
    this.isPlaying = false;
    
    this.resources = {};
    
    this.callbacks = {};
    this.apikeys = JSON.parse(fs.readFileSync('./public_new/scripts/spotify.key.json'));
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

            var token = self.getAccessToken();
            var headers = {};
            headers["Authorization"] = "Bearer " + token;
            if (data instanceof Object) {
                headers["Content-type"] = "application/json";

            } else {
                headers["Content-type"] = ("application/x-www-form-urlencoded");


            }
            request({
                method: method,
                url: "https://api.spotify.com/v1" + url,
                headers: headers,
                form: "grant_type=authorization_code&code=" + code + "&redirect_uri=" + encodeURI(self.apikeys.redirectURI),
            },
                function (error, response, body) {
                    if (!error) {
                        var data = JSON.parse(body);
                        resolve(data);
                    } else {
                        fail();
                    }
                }
            );

        };

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
        var headers = {};
        headers["Authorization"] = "Basic " + new Buffer(self.apikeys.clientId).toString() + ':' + new Buffer(self.apikeys.clientSecret);

        headers["Content-type"] = ("application/x-www-form-urlencoded");


        request({
                url: 'https://accounts.spotify.com/api/token',
                headers: headers, form: "grant_type=authorization_code&code=" + code + "&redirect_uri=" + encodeURI(self.apikeys.redirectURI)},
            function (error, response, body) {
                var data = JSON.parse(body);
                if (!('accessToken' in data)) {
                    fail({'error': 'Request problem'});
                    return;
                }
                self.nodeSpotifyAPI.setAccessToken(data);
                self.nodeSpotifyAPI.getMe().then(function (data) {
                    localStorage.setItem("me", JSON.stringify(data.body));


                    resolve(data);
                });

            }
        );
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

SpotifyPlayer.prototype.getImageForTrack = function (id, callback) {
    this.request('GET', 'https://api.spotify.com/v1/tracks/' + id).then(function (track) {
        callback(track.album.images[0].url);
    });
}

SpotifyPlayer.prototype.seek = function (position) {
}

SpotifyPlayer.prototype.login = function () {
    console.log("Log in");
    var self = this;
    var promise = new Promise(function (resolve, fail) {
        alert("AFFF");
        var win = gui.Window.get(window.open('https://accounts.spotify.com/authorize/?client_id=' + this.apikeys.clientId + '&response_type=code&redirect_uri=' + encodeURI(this.apiKeys.redirectUri) + '&scope=user-read-private%20user-read-email&state=34fFs29kd09', {
            "position": "center",
            "focus": true,
            "toolbar": false,
            "frame": true
        }));
        console.log(win);
        alert(win);
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
    return promise;
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
SpotifyPlayer.prototype.addTracksToPlaylist = function (user, playlist_id, uris, position) {
    var self = this;
    var promise = new Promise(function (resolve, fail) {
        self.request("POST", "/users/" + user + "/playlists/" + playlist_id + "/tracks", {
                "uris": uris, position: position
        }).then(function () {
            resolve();
        });

    });
    return promise;

}

SpotifyPlayer.prototype.getAlbumTracks = function (id, callback) {

    var self = this;
    var promise = new Promies(function (resolve, fail) {
        self.request("GET", "/albums/" + id + "/tracks").then(function (data) {
            resolve(data);
        })
    });
    return promise;
    
};


SpotifyPlayer.prototype.search = function (query, limit, offset, type, callback) {
    var self = this;
    var promise = new Promise(function (resolve, fail) {
        self.request('GET', 'https://api.spotify.com/v1/search?q=' + encodeURI(query) + '&type=' + type + '&limit=' + limit + '&offset=' + offset).then(function (data) {
            if ('tracks' in data) {
                var tracks = data.tracks.items.map(function (track) {
                    track.duration = track.duration_ms / 1000;
                    return track;
                });
                resolve(data.tracks.items);
            } else {
                resolve(data[type + 's'].items);
            }
        });
    });
    return promise;
};
SpotifyPlayer.prototype.loadPlaylist = function (user, id, callback) {
    var self = this;
    var promise = new Promies(function (resolve, fail) {
        self.request("GET", "/users/" + user + "/playlists/" + id + "/tracks").then(function (tracklist) {
            self.request("GET", "/users/" + uri.user + "/playlists/" + uri).then(function (playlist) {
                playlist.tracks = tracklist.tracks.items;
                resolve(playlist);
            });
        });
    });
    return promise;
}

SpotifyPlayer.prototype.createPlaylist = function (title) {
    var self = this;

    var promise = new Promise(function (resolve, fail) {
        var me = self.getMe();
        self.request("POST", "/users/" + me.id + "/playlists", {name: title}).then(function (object) {
            resolve(object);
        });
    });
    return promise;
};

SpotifyPlayer.prototype.getTopList = function (uri, callback) {

}

SpotifyPlayer.prototype.getUserPlaylists = function () {
    var self = this;
    var promise = new Promise(function (resolve, fail) {
        var user = self.getMe();
        self.request("GET", "/users/" + user.id + '/playlists').then(function (data) {
            resolve({
                'objects': data.items
            });
        });
    });
    return promise;
}


SpotifyPlayer.prototype.getPlaylistsForUser = function (id) {
    var self = this;
    var promise = new Promise(function (resolve, fail) {

        self.request("GET", "/users/" + id + '/playlists').then(function (data) {
            resolve({
                'objects': data.items
            });
        });
    });
    return promise;
}

SpotifyPlayer.prototype.getArtistById = function (id, callback) {
    var self = this;

    var promise = new Promies(function (resolve, fail) {
        self.request("GET", "/artists/" + id).then(function (data) {
            resolve(data);
        });
    });
    return promise;
}

SpotifyPlayer.prototype.getAlbum = function (id) {
    var self = this;
    var promise = new Promise(function (resolve, fail) {
        self.request('https://api.spotify.com/v1/albums/' + id).then(function (album) {
            album.image = album.images[0].url;
            album.tracks = [];
            this.request('GET', '/albums/' + uri.id + '/tracks').then(function (data) {
                album.tracks = data.tracks.items;
                resolve(album);

            });
        });
    });
    return promise;
}

SpotifyPlayer.prototype.resolveTracks = function (uris, callback) {

}

SpotifyPlayer.prototype.getPlaylistTracks = function (user, playlist_id, page, callback) {
    var self = this;
    var promise = new Promise(function (resolve, fail) {
         self.request('GET', '/users/' + user + '/playlists/' + playlist_id).then(function (data) {
             resolve({
                 'objects': data.tracks.items
             });
         });
    });
    return promise;
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
}
SpotifyPlayer.prototype.resume = function () {
    this.isPlaying = true;
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

module.exports = SpotifyPlayer;
