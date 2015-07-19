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
exports.View = function() {
	this.node = document.createElement("div");
};
exports.List = function(collection, getItem, filter) {
	this.node = document.createElement("ul");
	this.node.setAttribute("class", "list sp-list");
	console.log(collection);
	for(var i = 0; i < collection.length; i++) {
		
		var item = getItem(collection.get(i));
		console.log(item);
		this.node.appendChild(item.node);
	}
};
exports.List.prototype = new exports.View();

exports.Track = function(track, fields) {
	this.node = document.createElement("li");
	
	console.log(this.node);
	var _selected = false;
	this.__defineGetter__("selected", function() {
		return _selected;
	});
	this.__defineSetter__("selected", function (val) {
		_selected = val;
		if(_selected) {
			this.node.setAttribute("selected", "true");
		} else {
			this.node.removeAttribute("selected");
		}
	});
	if(fields & exports.Track.FIELD.STAR) {
		var star = document.createElement("span");
		this.node.appendChild(star);
	}
	if(fields & exports.Track.FIELD.SHARE) {
		var share = document.createElement("span");
		this.node.appendChild(share);
	}
	if(fields &  exports.Track.FIELD.NAME) {	
		var name_field = document.createElement("span");
		name_field.innerHTML = track.name;
		this.node.appendChild(name_field);
	}
	if(fields & exports.Track.FIELD.ARTIST) {
		
		var artists_field = document.createElement("span");
		for(var i = 0; i < track.artists.length; i++) {
			var artist = track.artists[i];
			var artist_a = document.createElement("a");
			
			artist_a.setAttribute("href", artist.uri);
			artist_a.innerHTML = artist.name;
			artists_field.appendChild(artist_a);
		}
		this.node.appendChild(artists_field);
	}
	if(fields & exports.Track.FIELD.ALBUM) {
		
		var album_field = document.createElement("a");
		album_field.setAttribute("href", track.album.uri);
		album_field.innerHTML = track.album.name;
		this.node.appendChild(album_field);
	}
	
};
exports.Track.FIELD = {
	STAR : 0x1,
	SHARE : 0x2,
	NAME : 0x4, 
	ARTIST : 0x8,
	ALBUM : 0xf
};