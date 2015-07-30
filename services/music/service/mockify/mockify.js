var fs = require('fs');
var request = require('request');
var Promise = require('promise');
var MockifyPlayer = function () {
    var self = this;
    this.cache = {};
    this.isPlaying = false;

    this.resources = {};

    this.callbacks = {};
    this.me = null;

};

MockifyPlayer.prototype.getAccessToken = function () {
    return JSON.parse(localStorage.getItem("accessToken", null));
}

MockifyPlayer.prototype.setAccessToken = function (accessToken, callback) {
    localStorage.setItem('accessToken', JSON.encode(accessToken));

}

MockifyPlayer.prototype.isAccessTokenValid = function () {
    return new Date() < new Date() + this.getAccessToken().expires_in;
}

MockifyPlayer.prototype.renewAccessToken = function (callback) {
    this.nodeMockifyAPI.refreshAccessToken().then(function () {
        callback();
    })
}
MockifyPlayer.prototype.getMe = function () {
    return JSON.parse(localStorage.getItem("me"));
}
MockifyPlayer.prototype.request = function (method, url, payload) {
    var self = this;
    self.url = url;
    var hasQuery =  url.indexOf('?') > -1;
    var promise = new Promise(function (resolve, fail) {
        var activity = function () {

            var parts = url.split(/\//g);
            if (parts[0] == 'search') {
                request({
                        url: 'https://api.spotify.com/v1/search?q=' + payload.q + '&type=' + payload.type + '&limit=' + payload.limit + '&offset=' + payload.offset
                    },
                    function (error, response, body) {
                        body = body.replace(/spotify\:/, 'bungalow:');
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
                                body = body.replace('spotify:', 'bungalow:');
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
                    request({
                            url: 'https://api.spotify.com/v1/albums/' + parts[1] + '/tracks?limit=' + payload.limit + '&offset=' + payload.offset
                        },
                        function (error, response, body) {
                            body = body.replace('spotify:', 'bungalow:');
                            var data = JSON.parse(body);
                            try {
                                resolve({
                                    'objects': data.items
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
            if (parts[0] == 'users') {
                var userid = parts[1];
                if (parts.length > 2) {
                    if (parts[2] == 'playlists') {
                        if (parts.length < 4) {
                            var playlists = [];
                            for (var i = 0; i < playlists.length; i++) {
                                playlists.push({
                                    'id': 'pls' + i,
                                    'name': 'Playlist ' + i,
                                    'user': {
                                        'id': userid,
                                        'name': 'User ' + userid
                                    },
                                    'description': 'Text',
                                    'uri': 'bungalow:playlist:pls' + i,
                                    'followers': 1920000
                                });
                            }
                            resolve({
                                'objects': playlists
                            });
                        } else {
                            if (parts[4] == 'followers') {
                                var users = [];
                                for (var i = 0; i < 10; i++) {
                                    uesrs.push({
                                        'id': 'follower' + i,
                                        'name': 'Track ' + i,
                                        'uri': 'bungalow:user:follower' + i
                                    });
                                }
                                resolve({
                                    'objects': users
                                });
                            } else if (parts[4] == 'tracks') {
                                var tracks = [];
                                for (var i = 0; i < 10; i++) {
                                    tracks.push({
                                        'id': 'track' + i,
                                        'name': 'Track ' + i,
                                        'artists': [{
                                            'id': 'artist1',
                                            'name': 'Artist ' + userid
                                        }],
                                        'album': {
                                            'name': 'Text',
                                            'id': 'album'
                                        },
                                        'uri': 'bungalow:playlist:track' + i,
                                        'user':{
                                            'id': 'drsounds',
                                            'name': 'Dr. Sounds',
                                            'uri': 'bungalow:user:drsounds'
                                        }
                                    });
                                }
                                resolve({
                                    'objects': tracks
                                })

                            } else {
                                var playlist_id = parts[4];
                                var playlist = {
                                    'id': playlist_id,
                                    'name': 'Playlist ' + i,
                                    'user': {
                                        'id': userid,
                                        'name': 'User ' + userid
                                    },
                                    'description': 'Text',
                                    'uri': 'bungalow:playlist:' + playlist_id
                                };
                                resolve(playlist);
                            }
                        }
                    }

                } else {
                    resolve({
                        'id': userid,
                        'name': userid,
                        'canonicalName': userid
                    });
                }
            }


        };


        activity();


    });
    return promise;
}

MockifyPlayer.prototype.requestAccessToken = function (code) {
    var self = this;
    var promise = new Promise(function (resolve, fail) {
        setTimeout(function () {
            var data = {
                'access_token': '145123412',
                'expires_in': 1000000,
            }
            localStorage.setItem("me", JSON.stringify(data.body));


            resolve(data);


        }, 1000);
    });
    return promise;
}


MockifyPlayer.prototype.addToCache = function (resource) {
}

MockifyPlayer.prototype.events = {};

MockifyPlayer.prototype.notify = function (event) {
    var type = event.type;
    if (type in this.events) {
        this.events[type].call(this, event);
    }
}

MockifyPlayer.prototype.addEventListener = function (event, callback) {
    this.events[event] = callback;
}

MockifyPlayer.prototype.ready = function () {

}

MockifyPlayer.prototype.getPosition = function () {
    return this.Mockify.player.currentSecond;
}

MockifyPlayer.prototype.logout = function () {
    this.Mockify.logout();
}

MockifyPlayer.prototype.playTrack = function (uri) {
    return track;
}

MockifyPlayer.prototype.stop = function () {
}

MockifyPlayer.prototype.getImageForTrack = function (id) {
    var promise = new Promise(function (resolve, fail) {

    });
    return promise;
}

MockifyPlayer.prototype.seek = function (position) {
}

MockifyPlayer.prototype.login = function () {
    console.log("Log in");
    var self = this;
    var promise = new Promise(function (resolve, fail) {
        setTimeout(function () {
            self.requestAccessToken(code, function () {
                resolve();
            }, function () {
                fail();
            })


        }, 100);
    });
    return promise;
}

MockifyPlayer.followPlaylist = function (playlist) {

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
MockifyPlayer.prototype.addTracksToPlaylist = function (user, playlist_id, uris, position) {
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

MockifyPlayer.prototype.getAlbumTracks = function (id, callback) {

    var self = this;
    var promise = new Promise(function (resolve, fail) {
        self.request("GET", "/albums/" + id + "/tracks").then(function (data) {
            resolve(data);
        })
    });
    return promise;

};


MockifyPlayer.prototype.search = function (query, type, limit, offset) {
    var self = this;
    var promise = new Promise(function (resolve, fail) {
        resolve({
            'objects': []
        });
    });
    return promise;
};
MockifyPlayer.prototype.getPlaylistById = function (user, id, callback) {
    var self = this;
    var promise = new Promise(function (resolve, fail) {
        self.request("GET", "/users/" + uri.user + "/playlists/" + uri).then(function (playlist) {

            resolve(playlist);

        });
    });
    return promise;
}

MockifyPlayer.prototype.createPlaylist = function (title) {
    var self = this;

    var promise = new Promise(function (resolve, fail) {
        var me = self.getMe();
        self.request("POST", "/users/" + me.id + "/playlists", {name: title}).then(function (object) {
            resolve(object);
        });
    });
    return promise;
};

MockifyPlayer.prototype.getTopListById = function (id) {

}

MockifyPlayer.prototype.getUserById = function (id) {
    var self = this;
    var promise = new Promise(function (resolve, fail) {

        self.request("GET", "/users/" + id).then(function (data) {
            resolve(data);
        });
    });
    return promise;
}

MockifyPlayer.prototype.getPlaylistsForUser = function (id) {
    var self = this;
    var promise = new Promise(function (resolve, fail) {

        self.request("GET", "/users/" + id + '/playlists').then(function (data) {
            resolve(data);
        });
    });
    return promise;
}

MockifyPlayer.prototype.getArtistById = function (id) {
    var self = this;

    var promise = new Promise(function (resolve, fail) {
        self.request("GET", "/artists/" + id).then(function (data) {
            console.log(data);
            resolve(data);
        });
    });
    return promise;
}

MockifyPlayer.prototype.getAlbumById = function (id) {
    var self = this;
    var promise = new Promise(function (resolve, fail) {
        self.request('GET', '/albums/' + id).then(function (album) {
            album.image = album.images[0].url;
            album.tracks = [];
            self.request('GET', '/albums/' + album.id + '/tracks').then(function (data) {
                album.tracks = data.objects;
                resolve(album);

            });
        });
    });
    return promise;
}

MockifyPlayer.prototype.resolveTracks = function (uris, callback) {

}

MockifyPlayer.prototype.getPlaylistTracks = function (user, playlist_id, page, callback) {
    var self = this;
    var promise = new Promise(function (resolve, fail) {
        self.request('GET', '/users/' + user + '/playlists/' + playlist_id).then(function (data) {
            resolve(data);
        });
    });
    return promise;
}

MockifyPlayer.prototype.playPause = function () {
    if (this.isPlaying) {
        this.pause();
    } else {
        this.resume();
    }
}
MockifyPlayer.prototype.pause = function () {
    this.isPlaying = false;
}
MockifyPlayer.prototype.resume = function () {
    this.isPlaying = true;
}
MockifyPlayer.prototype.reorderTracks = function (playlistUri, indices, newPosition) {
    console.log("Mockify is now reordering tracks");
    console.log("Done successfully");
}

MockifyPlayer.prototype.removeTracks = function (playlist, indices) {
    playlist.reorderTracks(indices, newPosition);
}

MockifyPlayer.prototype.addTracks = function (playlist, tracks, position) {
    playlist.addTracks(tracks, position);
}

module.exports = MockifyPlayer;
