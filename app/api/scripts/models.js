/* Rerun without minification for verbose metadata */
require(['$api/cosmos'], function (Cosmos) {
    (function (undefined) {
        Element.prototype.closest = function (t) {
            for (var e = this; e;) {
                if (e.matches(t))return e;
                e = e.parentElement
            }
            return null
        };
        Object.assign = function (r, t) {
            for (var n, e, a = 1; a < arguments.length; ++a) {
                e = arguments[a];
                for (n in e)Object.prototype.hasOwnProperty.call(e, n) && (r[n] = e[n])
            }
            return r
        };
        String.prototype.includes = function (t, e) {
            if ("object" == typeof t && t instanceof RegExp)throw new TypeError("First argument to String.prototype.includes must not be a regular expression");
            return -1 !== this.indexOf(t, e)
        };
    }).call('object' === typeof window && window || 'object' === typeof self && self || 'object' === typeof global && global || {});


    /**
     * @module
     */


    var Collection = function (endpoint, uri, type) {
        this.endpoint = endpoint;
        this.uri = uri;
        this.objects = [];
        this.limit = 10;
        this.offset = 0;
        this.type = type;
        this.complete = false;
    };

    Collection.prototype.next = function () {
        var self = this;
        return new Promise(function (resolve, fail) {
            if (self.complete) {
                return;
            }
            console.log("Collection", self);
            Cosmos.request('GET', self.endpoint + '&offset=' + self.offset + '&limit=' + self.limit + '&type=' + self.type).then(function (result) {
                console.log(result);
                for (var i = 0; i < result.objects.length; i++) {
                    self.objects = self.objects.concat(result.objects);
                }
                if (self.objects.length < 1) {
                    self.complete = true;
                }
                resolve(self);
                self.offset += self.limit;
            });
        });
    };

    var collection_stack = {};

    window.sp = {
        require: function (url) {
            var xmlHttp = new XMLHttpRequest();
            xmlHttp.onreadystatechange = function (event) {

            }
        }
    }


    /**
     * Represents a playlist
     * @class
     */
    var Playlist = function (data) {
        Object.assign(this, data);
        this.tracks = new Collection('/music/users/' + this.user.id + '/playlists/' + this.id + '/tracks?', 'bungalow:user:' + this.user.id + ':playlist:' + this.id, 'track');
    }

    exports.Playlist = Playlist;

    /**
     * Represents an album
     */
    var Album = function (data) {
        Object.assign(this, data);
        this.tracks = new Collection('/music/albums/' + this.id + '/tracks', 'bungalow:album:' + this.id, 'track');
    }

    exports.Album = Album;

    window.addEventListener('message', function (event) {
        if (event.data.action == 'gotPlaylist') {
            console.log("Received playlist from shell");
            var playlist = (event.data.data);
            Playlist.lists[playlist.uri] = playlist;
        }
        if (event.data.action == 'gotAlbum') {
            console.log("Received album from shell");
            var album = (event.data.data);
            Album.lists[album.uri] = album;
            console.log(album.uri);
        }
        if (event.data.action == 'gotTopList') {
            console.log("Received top list from shell");
            var toplist = (event.data.data);
            TopList.lists[toplist.uri] = toplist;
            console.log(toplist.uri);
        }
        if (event.data.action == 'gotArtist') {
            console.log("Received artist from shell");
            var artist = (event.data.data);
            Artist.lists[artist.uri] = artist;
            console.log(artisturi);
        }
        if (event.data.action == 'gotSearch') {
            console.log("Received search result from shell");
            var search = (event.data.data);
            Search.lists[event.data.type + ':' + event.data.query] = search;
            console.log(event.data.query);
        }
        if (event.data.action == 'gotAlbumTracks') {
            console.log("Received search result from shell");
            albumTracks[event.data.uri] = event.data.tracks;
        }
        if (event.data.action === 'trackstarted') {
            var uri = event.data.uri;

            $('.sp-track').removeClass('sp-now-playing');
            $('.sp-table[data-uri="' + uri + '"] .sp-track[data-track-index="' + event.data.index + '"]').addClass('sp-now-playing');

        }
    });

    /**
     * Represents an artist
     */

    var Artist = function (data) {
        Object.assign(this, data);

        this.albums = new Collection('/music/artists/' + this.id + '/albums?', 'bungalow:artist:' + this.id + ':albums', 'album');
        this.tracks = new Collection('/music/artists/' + this.id + '/tracks?', 'bungalow:artist:' + this.id + ':tracks', 'track');
    }

    exports.Artist = Artist;

    Playlist.lists = {};
    Album.lists = {};
    Artist.lists = {};

    /**
     * Creates a playlist from URI
     * @param  {String}   uri      [description]
     * @return {Promise}            [description]
     */
    Playlist.fromUserId = function (user, id) {
        return new Promise(function (resolve, fail) {
            Cosmos.request('GET', '/music/users/' + user + '/playlists/' + id).then(function (playlist) {
                resolve(new Playlist(playlist));
            });
        });
    };

    Album.fromId = function (id) {
        return new Promise(function (resolve, fail) {
            Cosmos.request('GET', '/music/albums/' + id).then(function (album) {
                resolve(new Album(album));
            });
        });
    };

    var User = function (data) {
        Object.assign(this, data);
        this.playlists = new Collection('/music/users/' + this.id + '/playlists?', 'bungalow:user:' + this.id + ':playlists', 'playlist');
    }

    User.fromId = function (id) {
        return new Promise(function (resolve, fail) {
            var parts = uri.split(/\:/g);
            var user = parts[2];
            Cosmos.request('GET', '/music/users/' + id).then(function (user) {
                resolve(new User(user));
            });
        });
    }

    exports.User = User;

    var albumTracks = {};


    Artist.fromId = function (id) {
        return new Promise(function (resolve, fail) {
            Cosmos.request('GET', '/music/artists/' + id).then(function (response) {
                resolve(new Artist(response));
            });
        });
    };

    Search = function (data) {
        Object.assign(this, data);
        this.uri = 'bungalow:search:' + this.query;
        this.tracks = new Collection('/music/search?q=' + this.q , this.uri + ':tracks', 'track');
        this.artists = new Collection('/music/search?q=' + this.q , this.uri + ':artists', 'artist');
        this.albums = new Collection('/music/search?q=' + this.q, this.uri + ':albums', 'album');
        this.users = new Collection('/music/search?q=' + this.q, this.uri + ':users', 'user');
        this.playlists = new Collection('/music/search?q=' + this.q, this.uri + ':playlists', 'playlist');
    };

    Search.lists = {};


    Search.search = function (query) {
        return new Search({
            query: query
        });
    };


    exports.Search = Search;

    var Chart = function (data) {
        Object.assign(this, data);

        this.tracks = new Collection('/music/charts/' + this.id + '/tracks?', 'bungalow:chart:' + this.id + ':tracks', 'track');
        this.albums = new Collection('/music/charts/' + this.id + '/albums?', 'bungalow:chart:' + this.id + ':albums', 'album');
    }

    exports.Chart = Chart;


    Chart.fromId = function (id) {
        return new Promise(function (resolve, fail) {
            console.log("Asking shell for getting artist");

            Cosmos.request('GET', '/music/charts/' + id.split(/\:/g)[2]).then(function (result) {
                resolve(new Chart(result));
            })
        });
    }

    var Year = function (data) {
        Object.assign(this, data);
        this.albums = new Collection('/music/years/' + this.year + '/albums?', 'bungalow:year:' + this.year + ':tracks');
        this.tracks = new Collection('/music/years/' + this.year + '/tracks?', 'bungalow:year:' + this.year + ':tracks');
    }

    Year.fromYear = function (year) {
        return new Promise(function (resolve, fail) {
            Cosmos.request('GET', '/music/years/' + year).then(function (result) {
                resolve(new Year(year));
            });
        });
    }

    var Country = function (data) {
        Object.assign(this, data);

    }

    Country.fromCode = function (countryCode) {
        return new Promise(function (resolve, fail) {
            Cosmos.request('GET', '/music/countries/' + countryCode).then(function (result) {
                resolve(new Country(result));
            });
        });
    }

    exports.Country = Country;


    var App = function (data) {
        this.data = data;
    };


    App.find = function (callback) {
        console.log("Listing app");
        $.getJSON('http://appfinder.aleros.webfactional.com/api/index.php', function (apps) {
            callback(apps);
        });
    }

    App.fromId = function (appId) {

    }

    App.prototype.install = function () {
        var settings = bungalow_get_settings();
        settings.apps.push(this.data);
    }

});