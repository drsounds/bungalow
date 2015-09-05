var fs = require('fs');
var SpotifyNodeApi = require('spotify-web-api-node');
var SpotifyWebApi = require('spotify-web-api');
var request = require('request');
var assign = require('object-assign');
var Promise = require("es6-promise").Promise;
var SpotifyPlayer = function () {
    var self = this;
    this.cache = {};
    this.isPlaying = false;
    
    this.resources = {};
    
    this.callbacks = {};
    this.apikeys = JSON.parse(fs.readFileSync(__dirname + '/spotify.key.json'));
    this.accessToken = null;

    this.nodeSpotifyApi = new SpotifyNodeApi(this.apikeys);
    this.spotifyAPI = new SpotifyWebApi();
    this.me = null;

};

SpotifyPlayer.prototype.authenticate = function (code) {
    var self = this;
    console.log(this.apikeys);
    return new Promise(function (resolve, fail) {
        request({
            url: 'https://accounts.spotify.com/api/token',
            method: 'POST',
            form: {
                grant_type: 'authorization_code',
                code: code,
                redirect_uri: 'http://play.bungalow.qi/callback.html'
            },
            headers: {
                'Authorization': 'Basic ' + new Buffer(self.apikeys.client_id + ':' + self.apikeys.client_secret).toString('base64') 
            }
        }, function (error, response, body) {
            console.log(error);
            if (error) {
                fail(error);
                return;
            }
            self.setAccessToken(JSON.parse(body));
            resolve(JSON.parse(body));
        });
    });
    
}

SpotifyPlayer.prototype.getAccessToken = function () {
    try {
        return JSON.parse(fs.readFileSync(__dirname + '/access_token.json'));
    } catch (e) {
        return null;
    }
}

SpotifyPlayer.prototype.setAccessToken = function (accessToken) {

    accessToken.time = new Date().getTime();
    console.log(accessToken);
    fs.writeFileSync(__dirname + '/access_token.json', JSON.stringify(accessToken));

}

SpotifyPlayer.prototype.isAccessTokenValid = function () {
    var access_token = this.getAccessToken();
    return new Date() < new Date(access_token.time + access_token.expires_in);
}

SpotifyPlayer.prototype.refreshAccessToken = function () {
    var self = this;
    return new Promise(function (resolve, fail) {
        var accessToken = self.getAccessToken();
        var refresh_token = accessToken.refresh_token;
        request({
            url: 'https://accounts.spotify.com/api/token',
            method: 'POST',
            form: {
                grant_type: 'refresh_token',
                refresh_token: refresh_token,
                redirect_uri: 'http://play.bungalow.qi/callback.html'
            },
            headers: {
                'Authorization': 'Basic ' + new Buffer(self.apikeys.client_id + ':' + self.apikeys.client_secret).toString('base64')
            }
        }, function (error, response, body) {
            /*if (error || 'error' in body) {
                fail();
                return;
            }*/
            console.log(self.apikeys);
            var accessToken = JSON.parse(body);
            accessToken.refresh_token = refresh_token 
            self.setAccessToken(accessToken);
            console.log("Refresh", body);
            resolve(JSON.parse(body));
        });
    });
}
SpotifyPlayer.prototype.getMe = function () {
    return JSON.parse(localStorage.getItem("me"));
}

SpotifyPlayer.prototype.request = function (method, url, payload) {
    var self = this;
    var promise = new Promise(function (resolve, fail) {

        console.log("Got request");
        console.log("Doing request");
        
        var activity = function () {

            var token = self.getAccessToken();
            var headers = {};
            headers["Authorization"] = "Bearer " + token.access_token;
            if (payload instanceof Object) {
                headers["Content-type"] = "application/json";

            } else {
                headers["Content-type"] = ("application/x-www-form-urlencoded");


            }

            var parts = url.split(/\//g);
            if (parts[0] == 'search') {
                request({
                        url: 'https://api.spotify.com/v1/search?q=' + payload.q + '&type=' + payload.type + '&limit=' + payload.limit + '&offset=' + payload.offset
                    },
                    function (error, response, body) {
                    
                        var data = JSON.parse(body);
                        try {
                            resolve({'objects': data[payload.type + 's'].items});
                        } catch (e) {
                            fail(e);
                        }
                    }
                );
            }
            if (parts[0] == 'artists') {
                if (parts.length > 2) {
                    if (parts[2] == 'albums') {
                        request({
                                url: 'https://api.spotify.com/v1/artists/' + parts[1] + '/albums?limit=' + payload.limit + '&offset=' + payload.offset
                            },
                            function (error, response, body) {
                                var data = JSON.parse(body);
                                try {
                                    resolve({'objects': data.items});
                                } catch (e) {
                                    fail();
                                }
                            }
                        );
                    }
                } else {
                    request({
                            url: 'https://api.spotify.com/v1/artists/' + parts[1]
                        },
                        function (error, response, body) {
                            body = body.replace('spotify:', 'bungalow:');
                            var data = JSON.parse(body);
                            resolve(data);
                        }
                    );
                    return;
                }
            }

            if (parts[0] == 'albums') {
                if (parts.length > 2) {
                    console.log("Payload", payload);
                
                    var limit =  payload.limit;
                    var offset = payload.offset;
                    
                    
                    request({
                            url: 'https://api.spotify.com/v1/albums/' + parts[1] + '/tracks?limit=' + limit + '&offset=' + offset
                        },
                        function (error, response, body) {
                            body = body.replace('spotify:', 'bungalow:');
                            var data = JSON.parse(body);
                            try {
                                resolve({
                                    'objects': data.items.map(function (track) {
                                        track.popularity = 0.0;
                                        return track;
                                    })
                                });
                            } catch (e) {
                                resolve({
                                    'objects': []
                                })
                            }
                        }
                    );
                } else {
                    request({
                            url: 'https://api.spotify.com/v1/albums/' + parts[1] + ''
                        },
                        function (error, response, body) {
                            body = body.replace(/spotify\:/, 'bungalow:');
                            var data = JSON.parse(body);
                            try {
                                resolve(data);
                            } catch (e) {
                                fail();
                            }
                        }
                    );
                }
            }
            if (parts[0] == 'tracks') {
                request({
                        url: 'https://api.spotify.com/v1/tracks/' + parts[1] + ''
                    },
                    function (error, response, body) {
                        var data = JSON.parse(body);
                        try {
                            resolve(data);
                        } catch (e) {
                            fail();
                        }
                    }
                );
            }
            if (parts[0] == 'labels') {
                if (parts.length > 2) {
                    if (parts[2] == 'artists') {
                        request({
                            url: 'https://api.spotify.com/v1/search/?q=label:"' + encodeURI(parts[1]) + '"&type=artist&limit=' + payload.limit + '&offset=' + payload.offset,
                            headers: headers
                        },  function (error, response, body) {
                            resolve(body);
                        });
                        resolve({objects: labels});
                    }
                }
            }
            if (parts[0] == 'countries') {
                if (parts.length > 1) {
                    var code = parts[1];
                    if (parts.length > 2) {

                        if (parts[2] == 'charts') {
                            var chart = parts[3];
                            var type = parts[4];
                            if (type === 'tracks') {
                                resolve({'objects': []});
                            }
                        }
                        if (parts[2] == 'labels') {
                            var labels = [
                                {
                                    'id': 'substream',
                                    'name': 'Substream Music Group',
                                    'uri': 'spotify:label:substream'
                                }
                            ];
                            resolve({objects: labels});
                        }
                        
                    } else {

                        resolve({
                            'id': code,
                            'name': code,
                            'followers': {
                                'count': 5000000,
                                'href': 'spotify:country:' + code + ':followers'
                            }
                        })
                    }
                }
            }
            if (parts[0] == 'users') {
                var userid = parts[1];
                if (parts.length > 2) {
                    if (parts[2] == 'playlists') {
                        if (parts.length < 4) {
                            payload = {
                                limit: 10,
                                offset: 0
                            };
                            request({
                                url: 'https://api.spotify.com/v1/users/' + userid + '/playlists?limit=' + payload.limit + '&offset=' + payload.offset,
                                headers: headers
                            }, function (error, response, body) {
                                var result = JSON.parse(body);
                                resolve({
                                    'objects': result.items.map(function (playlist) {
                                        playlist.user = playlist.owner;
                                        return playlist;
                                    }),
                                    'source': 'spotify'
                                });
                            });
                        } else {
                            if (parts[4] == 'followers') {
                                var users = [];
                                for (var i = 0; i < 10; i++) {
                                    uesrs.push({
                                        'id': 'follower' + i,
                                        'name': 'Track ' + i,
                                        'uri': 'spotify:user:follower' + i
                                    });
                                }
                                resolve({
                                    'objects': users
                                });
                            } else if (parts[4] == 'tracks') {
                                request({
                                    url: 'https://api.spotify.com/v1/users/' + parts[1] + '/playlists/' + parts[3] + '/tracks',
                                    headers: headers
                                }, function (error, response, body) {
                                    var result = JSON.parse(body);
                                    resolve({
                                        'objects': result.items.map(function (track) {
                                            var track = assign(track, track.track);
                                            track.user = track.added_by;
                                            return track;
                                        })
                                    })
                                });
                            } else {
                                request({
                                    url: 'https://api.spotify.com/v1/users/' + parts[1] + '/playlists/' + parts[3] + '',
                                    headers: headers
                                }, function (error, response, body) {
                                    var result = JSON.parse(body);
                                    resolve(result);
                                });
                            }
                        }
                    }

                } else {
                    console.log("Getting users");
                    request({
                        url: 'https://api.spotify.com/v1/users/' + parts[1] + '',
                        headers: headers
                    },
                        function (error, response, body) {
                            if (error) {
                                fail({'error': ''});
                            }
                            var user = JSON.parse(body);
                            user.name = user.canonicalName;
                            user.images = [
                                {
                                    'url': user.image
                                }
                            ];
                            resolve(user);
                        }
                    );

                }
            }
        };
        if (!self.isAccessTokenValid()) {
            self.refreshAccessToken().then(activity);
            return;
        }
        activity();
        
    });
    return promise;
}


SpotifyPlayer.prototype.requestAccessToken = function (code) {
    var self = this;
    var promise = new Promise(function (resolve, fail) {
        var headers = {};
        headers["Authorization"] = "Basic " + new Buffer(self.apikeys.client_id).toString() + ':' + new Buffer(self.apikeys.client_secret);

        headers["Content-type"] = ("application/x-www-form-urlencoded");


        request({
                url: 'https://accounts.spotify.com/api/token',
                headers: headers, form: "grant_type=authorization_code&code=" + code + "&redirect_uri=" + encodeURI(self.apikeys.redirect_uri)},
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
        var win = gui.Window.get(window.open('https://accounts.spotify.com/authorize/?client_id=' + this.apikeys.client_id + '&response_type=code&redirect_uri=' + encodeURI(this.apiKeys.redirect_uri) + '&scope=user-read-private%20user-read-email&state=34fFs29kd09', {
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
