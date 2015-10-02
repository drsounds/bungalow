(function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){
'use strict';

var models = require('@bungalow/models');
exports2.resolveUri = function (uri) {
    return new Promise(function (resolve, fail) {
        var parts = uri.split(/\:/g);
        var id = parts[2];

        resolve({
            id: id,
            icon: 'people',
            name: id,
            uri: 'bungalow:user:' + id
        });
    });
};

},{"@bungalow/models":3}],2:[function(require,module,exports){
"use strict";

var Cosmos = function Cosmos() {};

Cosmos.request = function (method, url, params, data) {
    return new Promise(function (resolve, fail) {
        var xhr = new XMLHttpRequest();
        console.log("Sending request");
        xhr.onreadystatechange = function () {
            if (xhr.readyState == 4 && xhr.status == 200) {
                var data = xhr.response;
                console.log("Resolved url " + url);
                resolve(data);
            }
        };
        xhr.responseType = 'json';
        console.log(method, url);
        xhr.open(method, '/api' + url, true);
        xhr.send(data);
        console.log(data);
    });
};

module.exports = Cosmos;

},{}],3:[function(require,module,exports){
(function (global){
"use strict";

var Cosmos = require('@bungalow/cosmos');
var _exports = {};
(function (undefined) {
    Element.prototype.closest = function (t) {
        for (var e = this; e;) {
            if (e.matches(t)) return e;
            e = e.parentElement;
        }
        return null;
    };
    Object.assign = function (r, t) {
        for (var n, e, a = 1; a < arguments.length; ++a) {
            e = arguments[a];
            for (n in e) Object.prototype.hasOwnProperty.call(e, n) && (r[n] = e[n]);
        }
        return r;
    };
    String.prototype.includes = function (t, e) {
        if ("object" == typeof t && t instanceof RegExp) throw new TypeError("First argument to String.prototype.includes must not be a regular expression");
        return -1 !== this.indexOf(t, e);
    };
}).call('object' === typeof window && window || 'object' === typeof self && self || 'object' === typeof global && global || {});

var Uri = function Uri(uri) {
    this.uri = uri;
    var parts = uri.split(/\:/g);
    this.protocol = parts[0];
    this.app = parts[1];
    this.parameters = parts.slice(2);
};

_exports.Uri = Uri;

var Observable = function Observable() {
    this.events = [];
};

Observable.prototype.addEventListener = function (event, callback) {
    this.events.push({
        event: event,
        callback: callback
    });
};

Observable.prototype.dispatchEvent = function (event) {
    for (var i = 0; i < this.events.length; i++) {
        if (this.events[i].type == event.type) {
            this.events[i].callback.call(this, event);
        }
    }
};

/**
 * @module
 */

String.prototype.capitalize = function () {
    return this.replace(/^./, function (match) {
        return match.toUpperCase();
    });
};

var Loadable = function Loadable(data) {
    Object.assign(this, data);
};

Loadable.prototype.load = function () {
    var model = this.type;
    var self = this;
    return new Promise(function (resolve, fail) {
        Cosmos.request('GET', '/music/' + model + 's/' + self.id).then(function (result) {

            resolve(new (_exports[model.capitalize()])(result));
        });
    });
};

_exports.Loadable = Loadable;

var MdL = function MdL(data) {
    Loadable.call(this, data);
};

MdL.prototype = Object.create(Loadable.prototype);
MdL.prototype.constructor = Loadable;

_exports.MdL = MdL;

var Collection = function Collection(endpoint, uri, type) {
    this.endpoint = endpoint;
    this.uri = uri;
    this.objects = [];
    this.limit = 20;
    this.offset = 0;
    this.type = type;
    this.complete = false;
    this.fetching = false;
};

Collection.prototype.next = function () {
    var self = this;
    if (self.fetching || self.complete) {
        return new Promise(function (resolve, fail) {});
    }
    return new Promise(function (resolve, fail) {
        console.log(self.complete);

        var url = self.endpoint + '&offset=' + self.offset + '&limit=' + self.limit + '&type=' + self.type;

        self.offset += self.limit;

        if (self.fetching) {
            return;
        }
        console.log("Requesting tracks");
        self.fetching = true;
        Cosmos.request('GET', url).then(function (result) {
            console.log("SELF", self);
            console.log("GOT REPLY FROM " + url);
            // console.log(result);
            console.log(result);
            console.log(result.objects);
            if (result.objects.length < 1) {
                self.complete = true;
            }
            for (var i = 0; i < result.objects.length; i++) {
                self.objects = result.objects;
            }
            self.objects = self.objects.map(function (object) {
                if ('track' in object) {
                    return Object.assign(object, object.track);
                }

                return object;
            });

            resolve(self);
            self.fetching = false;
        });
    });
};

var collection_stack = {};

window.sp = {
    require: function require(url) {
        var xmlHttp = new XMLHttpRequest();
        xmlHttp.onreadystatechange = function (event) {};
    }
};

/**
 * Represents an album
 */
var Album = function Album(data) {
    MdL.call(this, data);
    console.log(data);
    this.type = 'album';

    this.tracks = new Collection('/music/albums/' + this.id + '/tracks?', 'bungalow:album:' + this.id, 'track');
};

Album.prototype = Object.create(MdL.prototype);

Album.constructor = Loadable;

Album.fromId = function (id) {
    return new Album({ 'id': id, type: 'album' });
};

Album.prototype.load = function () {
    var self = this;
    console.log(self.id);
    return new Promise(function (resolve, fail) {
        Cosmos.request('GET', '/music/albums/' + self.id).then(function (result) {
            console.log(result);
            resolve(new Album(result));
        });
    });
};

_exports.Album = Album;

/**
 * Represents an artist
 */

var Artist = function Artist(data) {
    MdL.call(this, data);

    this.albums = new Collection('/music/artists/' + this.id + '/albums?', 'bungalow:artist:' + this.id + ':albums', 'album');
    this.tracks = new Collection('/music/artists/' + this.id + '/tracks?', 'bungalow:artist:' + this.id + ':tracks', 'track');
};

Artist.fromURI = function (uri) {
    return new Artist({ id: uri.split(/\:/g)[2], type: 'artist' });
};

Album.fromURI = function (uri) {
    return new Album({ id: uri.split(/\:/g)[2], type: 'artist' });
};

Artist.prototype = Object.create(Loadable.prototype);
Artist.prototype.constructor = Loadable;

Artist.fromId = function (id) {
    return new Artist({ 'id': id, type: 'artist' });
};

_exports.Artist = Artist;

var Track = function Track(data) {
    MdL.call(this, data);
};

Track.prototype = new MdL();
Track.prototype.constructor = MdL;

Track.fromURI = function (uri) {
    return new Track({ id: uri.split(/\:/g)[2], type: 'track' });
};

Track.load = function () {
    return new Promise(function (resolve, fail) {
        Cosmos.request('GET', '/music/tracks/' + self.id).then(function (result) {
            resolve(new Track(result));
        });
    });
};

_exports.Track = Track;

/**
 * Represents a playlist
 * @class
 */
var Playlist = function Playlist(data) {
    Loadable.prototype.constructor.call(this, data);
    console.log("DATA", data);
    Object.assign(this, data);
    console.log("OWNER", this.owner);
    this.tracks = new Collection('/music/users/' + this.owner.id + '/playlists/' + this.id + '/tracks?', 'bungalow:user:' + this.owner.id + ':playlist:' + this.id, 'track');
    console.log(this.tracks);
    this.type = 'playlist';
};

Playlist.prototype = Object.create(Playlist.prototype);
Playlist.prototype.constructor = Loadable;

/**
 * Creates a playlist from URI
 * @param  {String}   uri      [description]
 * @return {Promise}            [description]
 */
Playlist.fromUserId = function (username, id) {
    console.log(username);
    return new Playlist({ owner: { id: username }, id: id, type: 'playlist' });
};

Playlist.fromURI = function (uri) {
    var parts = uri.split(/\:/g);
    return new Playlist({ 'id': parts[4], owner: { id: parts[2] }, type: 'playlist' });
};
Playlist.prototype.load = function () {
    var self = this;
    return new Promise(function (resolve, fail) {
        Cosmos.request('GET', '/music/users/' + self.owner.id + '/playlists/' + self.id).then(function (result) {
            resolve(new Playlist(result));
        });
    });
};

_exports.Playlist = Playlist;

var User = function User(data) {

    Loadable.call(this, data);
    this.type = 'user';
    this.playlists = new Collection('/music/users/' + this.id + '/playlists?', 'bungalow:user:' + this.id + ':playlists', 'playlist');
    this.followers = new Collection('/music/users/' + this.id + '/followers?', 'bungalow:user:' + this.id + ':followers', 'user');
};

User.prototype = Object.create(Loadable.prototype);
User.prototype.constructor = Loadable;

User.fromId = function (id) {
    return new User({ 'id': id, type: 'user' });
};

User.fromURI = function (uri) {
    var id = uri.split(/\:/g)[2];
    return new User({ 'id': id, type: 'user' });
};

User.prototype.load = function () {
    var self = this;
    console.log(self.id);
    return new Promise(function (resolve, fail) {
        Cosmos.request('GET', '/music/users/' + self.id).then(function (result) {
            console.log(result);
            resolve(new User(result));
        });
    });
};

_exports.User = User;

var albumTracks = {};

var Search = function Search(data) {
    Object.assign(this, data);
    this.uri = 'bungalow:search:' + this.query;
    this.tracks = new Collection('/music/search?q=' + this.q, this.uri + ':tracks', 'track');
    this.artists = new Collection('/music/search?q=' + this.q, this.uri + ':artists', 'artist');
    this.albums = new Collection('/music/search?q=' + this.q, this.uri + ':albums', 'album');
    this.users = new Collection('/music/search?q=' + this.q, this.uri + ':users', 'user');
    this.playlists = new Collection('/music/search?q=' + this.q, this.uri + ':playlists', 'playlist');
};

Search.lists = {};

Search.search = function (query) {
    return new Search({
        q: query
    });
};

_exports.Search = Search;

var Chart = function Chart(data) {
    Loadable.call(this, data);

    this.tracks = new Collection('/music/charts/' + this.id + '/tracks?', 'bungalow:chart:' + this.id + ':tracks', 'track');
    this.albums = new Collection('/music/charts/' + this.id + '/albums?', 'bungalow:chart:' + this.id + ':albums', 'album');
};

Chart.prototype = new Loadable();
Chart.prototype.constructor = new Loadable();

_exports.Chart = Chart;

Chart.fromId = function (id) {
    return new Chart({ 'id': id, 'type': 'chart' });
};

var Year = function Year(data) {
    Loadable.call(this, data);
    this.albums = new Collection('/music/years/' + this.year + '/albums?', 'bungalow:year:' + this.year + ':tracks');
    this.tracks = new Collection('/music/years/' + this.year + '/tracks?', 'bungalow:year:' + this.year + ':tracks');
};

Year.prototype = Object.create(Loadable.prototype);
Year.prototype.constructor = Loadable;

Year.fromYear = function (year) {
    return new Promise(function (resolve, fail) {
        Cosmos.request('GET', '/music/years/' + year).then(function (result) {
            resolve(new Year(year));
        });
    });
};

var Track = function Track(data) {
    Object.assign(this, data);
};

Track.fromId = function (id) {
    resolve(new Track(id));
};

_exports.Track = Track;

var Country = function Country(data) {
    Object.assign(this, data);
};

Country.fromCode = function (countryCode) {
    return new Country({ 'id': countryCode, type: 'country' });
};
Country.prototype = new Loadable();
Country.prototype.constructor = new Loadable();

_exports.Country = Country;

var App = function App(data) {
    this.data = data;
};

App.find = function (callback) {
    // console.log("Listing app");
    $.getJSON('http://appfinder.aleros.webfactional.com/api/index.php', function (apps) {
        callback(apps);
    });
};

var Hashtag = function Hashtag(data) {
    Object.assign(this, data);
    this.uri = 'bungalow:hashtag:' + this.id;
    this.posts = new Collection('/social/hashtag/' + this.id + '/posts?', 'bungalow:hashtag:' + this.id + ':posts', 'post');
};

Hashtag.fromTag = function (id) {
    return new Hashtag({ id: id, type: 'hashtag' });
};

_exports.Hashtag = Hashtag;

App.fromId = function (appId) {};

App.prototype.install = function () {
    var settings = bungalow_get_settings();
    settings.apps.push(this.data);
};

App.resolveUri = function (url) {
    var promise = new Promise(function (resolve, fail) {
        var uri = new Uri(url);
        var xmlHttp = new XMLHttpRequest();
        xmlHttp.open('GET', '/apps/' + uri.app + '/resolver.js', false);
        xmlHttp.send(null);
        if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
            var code = "(function () { var exports2 = {}; " + xmlHttp.responseText + "; return exports2;  })();";
            var app = eval(code);
            app.resolveUri(uri).then(resolve);
        }
    });
    return promise;
};

var Application = function Application() {
    this.events = [];
};
_exports.App = App;
Application.prototype = Object.create(Observable.prototype);
Application.prototype.constructor = Observable;

_exports.application = new Application();
console.log("T");
window.addEventListener('message', function (event) {
    var data = event.data;
    console.log(event);
    if (data.action == 'navigate') {
        var evt = new CustomEvent('argumentschanged');
        evt.data = data.arguments;
        console.log("A");
        _exports.application.dispatchEvent(evt);
    }
});

module.exports = _exports;

}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})
},{"@bungalow/cosmos":2}]},{},[1]);
