/***************************************************
Copyright (C) 2012 Alexander Forselius

Permission is hereby granted, free of charge, to any person obtaining a copy of this 
software and associated documentation files (the "Software"), to deal in the Software
 without restriction, including without limitation the rights to use, copy, modify, merge,
 publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons 
 to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or 
substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING 
BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*********************************************************/
/***
@module models
***/
/***
@class Event
****/
exports.Event = function (type, callback) {
	this.type = type;
	this.callback = callback;
};
exports.EVENT = {
	ARGUMENTSCHANGED : 0,
	SUBSCRIBED: 1,
	CHAGNE: 2
}; 
/***
@interface Observable
****/
exports.Observable = function () {
	var events = [];
	this.observe = function(evt, callback) {	
		var evt = new exports.Event(evt, callback);
		events.push(evt);
	};
	this.notify = function(type, data) {
		for(var i = 0; i < events.length; i++) {
			if(events[i].type == type) {
				return events[i].callback(data);
			}
		}
	};
};
/***
@interface Album
****/
exports.IAlbum = function(uri, name, artist, tracks) {
	this.uri = uri;
	this.name = name;
	this.artist = artist;
	this.tracks = tracks;
};
/***
@interface Artist
****/
exports.IArtist = function(uri, name) {
	this.name = name;
	this.uri = uri;
};
/***
@interface Track
****/
exports.ITrack = function (uri, name, artists, album) {
	this.name = name;
	this.uri = uri;
	this.artists = artists;
	this.album = album;
};
/***
@class Application
****/
exports.Application = function(){};
exports.Application.prototype = new exports.Observable();
exports.Application.prototype.constructor = function () {
};
/**
@class Search
**/
exports.Search = function(query) {
	var internalPaging = 0;
	var _tracks = [];
	this.__defineGetter__("tracks", function() {
		return _tracks;
	});
	var __this = this;
	this.appendNext = function(query, options) {
		// if spotify track

		var xmlHttp = new XMLHttpRequest();
		xmlHttp.onreadystatechange = function() {
		
			var tracks = [];
			var z_tracks = xmlHttp.responseXML.getElementsByTagName("track");
			for(var i = 0; i < z_tracks.length; i++) {
				var __track = z_tracks[i];
				console.log(__track);
				var track = {
					name : __track.getElementsByTagName("name")[0].firstChild.nodeValue,
					uri: __track.getAttribute("uri"),
					artists:[{
						name: __track.getElementsByTagName("artist")[0].getElementsByTagName("name")[0].firstChild.nodeValue,
						
					}],
					album: {
						name: __track.getElementsByTagName("album")[0].getElementsByTagName("name")[0].firstChild.nodeValue,
						uri: __track.getElementsByTagName("album")[0].getAttribute("href")
					}
				};
				_tracks.push(track);
			}
			
			__this.notify(exports.EVENT.CHANGE, null);
		
		};			
		xmlHttp.open("GET", "http://ws.spotify.com/search/1/track?q=" + query , true);
		xmlHttp.send(null);
		internalPaging++;
		
	};
};
exports.Search.prototype = new exports.Observable();/**
exports.Search.prototype.constructor = function (query, options) {
	// Mockup for now
	var primary_artist = new IArtist("Dr. Sounds");
	this.tracks = [
		new ITrack("Cosmosphere", [primary_artist], new IAlbum("Subspace", primary_artist), "spotify:track:4SlERcgo0XzBrSUpmF96WT"),
		new ITrack("Beta", [primary_artist], new IAlbum("Subspace", primary_artist), "spotify:track:4SlERcgo0XzBrSUpmF96WT"),
		new ITrack("Aquasphere", [primary_artist], new IAlbum("Subspace", primary_artist), "spotify:track:4SlERcgo0XzBrSUpmF96WT"),
		new ITrack("Delta", [primary_artist], new IAlbum("Subspace", primary_artist), "spotify:track:4SlERcgo0XzBrSUpmF96WT")
	];
	this.appendNext = function() {
		this.notify(models.EVENT.CHANGED, this.tracks);
	};
};**/

/*********
@class Collection
***********/
exports.Collection = function (data, length) {
	this.data = data;
	this.length = length;
	this.getRange = function(start, length) {
		var items = [];
		for(var i = start; i < start + length; i++) {
			items.push(this.data[i]);
		}
		return items;
	};
	this.add = function(item) {
		this.data.push(item);
	};
	this.get = function(index) {
		return this.data[index];
	};
	this.indexOf = function(item) {
		for(var i = 0; i < this.data.length; i++) {
			if(item == this.data[i]) {
				return i;
			}
		}
	};
	this.remove = function(index) {
		this.data.splice(index, 1);
	};
};
exports.Collection.prototype = new exports.Observable();

/***********
@class Track
************/
exports.Tracks = {
	fromUri: function(uri, callback) {
		// if spotify track
		if(uri.indexOf("spotify:track:") == 0) {
			var xmlHttp = new XMLHttpRequest();
			xmlHttp.onreadystatechange = function() {
				if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
					var name = xmlHttp.responseXml.getElementsByTagName("name")[0].firstChild.nodeValue;
					
					
					var _artist = xmlHttp.responesXml.getElementsByTagName("artist")[0];
					var _album = xmlHttp.responesXml.getElementsByTagName("album")[0];
					var artists = [{
						name : _artist.getElementsByTagName("name")[0].firstChild.nodeValue,
						uri : _artist.getAttribute("href")
					}];
					var album = {
						name : _album.getElementsByTagName("name")[0].firstChild.nodeValue,
						uri : _album.getAttribute("uri")
					};
					var track = new models.ITrack(uri, name, artists, album);
					callback(track);
				}
			};			
			xmlHttp.open("GET", "http://ws.spotify.com/lookup/1/?uri=" + uri, true);
			xmlHttp.send(null);
		}
	}
};
exports.Album = {
	fromUri: function(uri, callback) {
		// if spotify track
		if(uri.indexOf("spotify:album:") == 0) {
			var xmlHttp = new XMLHttpRequest();
			xmlHttp.onreadystatechange = function() {
				if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
					var name = xmlHttp.responseXml.getElementsByTagName("name")[0].firstChild.nodeValue;
					
					
					var _artist = xmlHttp.responesXml.getElementsByTagName("artist")[0];
					var _album = xmlHttp.responesXml.getElementsByTagName("album")[0];
					var artist = [{
						name : _artist.getElementsByTagName("name")[0].firstChild.nodeValue,
						uri : _artist.getAttribute("href")
					}];
					var tracks = [];
					var _tracks = xmlHttp.responseXml.getElementsByTagName("track");
					for(var i = 0; i < _tracks.length; i++) {
						var __track = _tracks[i];
						var track = {
							name : __track.getElementsByTagName("name")[0].firstChild.nodeValue,
							uri: __track.getAttribute("uri"),
							artist: artist,
							album: album
						};
						tracks.push(track);
					}
					var album = new models.IAlbum(uri, name, artist, tracks);
					callback(album);
				}
			};			
			xmlHttp.open("GET", "http://ws.spotify.com/lookup/1/?uri=" + uri + "&extras=albumdetail", true);
			xmlHttp.send(null);
		}
	}
};
if(!___app) {
	___app = new exports.Application();
}
exports.application = ___app;