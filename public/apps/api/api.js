var Context = function () {
	this.currentApp = window.location.href.split(/\//g)[5];
	console.log("Current app", this.currentApp);
	this.currentIndex = 0;
	var self = this;
	window.onmessage = function (event) {
		if (event.data.action === 'trackended') {
			var index = event.data.newIndex;
			var tracks = document.querySelectorAll('.sp-track');
			$('#context_' + event.data.uri.replace(/\:/g, '__') + ' > .sp-track').removeClass('sp-now-playing');
	
		}
	}

};

var activeViews = {};


console.log(window.location.href.split(/\//g)[4]);

HTMLTableRowElement.prototype.toTrack = function () {
	/*var track = {
		'uri': this.getAttribute('data-uri'),
		'duration': this.getAttribute('data-duration'),
		'name': this.getElementsByTagName('td')[0].textContent,
		'artists': [{
			'name': this.getElementsByTagName('td')[1].getElementsByTagName('a')[0].innerText,
			'uri': this.getElementsByTagName('td')[1].getElementsByTagName('a')[0].getAttribute('data-uri')
		}]
	};*/
	var track = JSON.parse(this.getAttribute('data-object'));
	console.log("Converted tr to track", track);
	return track;
}

function context_to_tracks(elements) {
	var tracks = [];
	for( var i = 0 ; i < elements.length; i++) {
		var elm = elements[i];
		tracks.push(elm.toTrack());
	}
	return tracks;
}

Context.prototype.play = function (uri, index) {
	console.log("Playing from context with uri" + uri);
	console.log("Start playing track");
	var sel = '.sp-table[data-uri="' + uri + '"]';
	$(sel + ' .sp-track').removeClass('sp-now-playing');
	console.log(sel + ' .sp-track');
	var tracks = document.querySelectorAll(sel + ' .sp-track');
	console.log("tracks in context " ,tracks);
	var tracklist = {
		'tracks': context_to_tracks(tracks),
		'currentIndex': index,
		'uri': uri
	};

	console.log(this.currentApp, "Current app");
	window.parent.postMessage({'action': 'activateApp', 'app': this.currentApp}, '*');
	window.parent.postMessage({'action': 'play', 'data': JSON.stringify(tracklist)}, '*');
	this.currentIndex = 0;
	track.classList.add('sp-now-playing');

}

var track_contexts = new Context();
(function ($) {
	$.fn.spotifize = function (options) {
		console.log("Spotifize");
		var self = this;

		

		
	};
}(jQuery));

$(document).on('dblclick', '.sp-track', function (event) {
	var parent = $(event.target).parent().parent().parent();
	var uri = $(parent).attr('data-uri');
	console.log("Uri", uri);
	console.log("Parent", parent[0]);
	var index = $(this).attr('data-track-index');
	context.play(uri, index);

});

function deci (number) {
	return Math.round(number) < 10 ? '0' + Math.round(number) : Math.round(number);
}

function toMS (seconds) {
	var minutes = Math.floor(seconds / 60);
	var seconds = (seconds % 60) / 60;
	return deci(minutes) + ':' + deci(seconds);
}

/**
 * Update track contexts
 **/
function bungalow_update_context(uri, tracks, position) {
	var context = track_contexts[uri];
	context.insertTracks(tracks, position);
}



var TrackView = function (track, index, options) {
	this.node = document.createElement('tr');
	this.node.classList.add('sp-track');
	this.node.setAttribute('data-uri', track.uri);
	this.node.setAttribute('data-object', JSON.stringify(track));
	this.node.setAttribute('draggable', true);
	this.node.setAttribute('data-track-index', index);
	this.node.addEventListener('dragstart', function (event) {
		console.log("Begin drag");
		var uris = "";
        event.dataTransfer.effectAllowed = 'copy';

		var $tracks = $('.sp-track-selected').each(function (i) {
			uris += $(this).attr('data-uri') + "\n";
		});
		console.log("Assigning data");
		event.dataTransfer.setData('text/uri-list', uris);
		event.dataTransfer.setData('text', uris);
	}, false);

	for (var i = 0; i < options.fields.length; i++) {
		var field = options.fields[i];
		if (field === 'title') {
			var td1 = document.createElement('td');
			td1.innerHTML = track.name;
			this.node.appendChild(td1);
		}
		if (field === 'artist') {
			var td2 = document.createElement('td');

			td2.innerHTML = '<a data-uri="' + track.artists[0].uri + '">' + track.artists[0].name + '</a>';
			this.node.appendChild(td2);
		}
		if (field === 'duration') {
			var td3 = document.createElement('td');
			td3.innerHTML = '<span class="fade" style="float: right">' + toMS(track.duration) + '</span>';
			this.node.appendChild(td3);
		}
		if (field === 'album') {
			var td4 = document.createElement('td');
			td4.innerHTML = '<a href="javascript:void()" data-uri="' + track.album.link + '">' + track.album.name + '</a>';
			this.node.appendChild(td4);
		}
		if (field === 'popularity') {
			var td4 = document.createElement('td');
			td4.innerHTML = '<meter min="0" style="max-width:35px; margin-top: -18px !important" max="100" value="' + track.popularity + '">';
			this.node.appendChild(td4);
		}
		if (field === 'user') {
			var td4 = document.createElement('td');
			td4.innerHTML = '<a href="javascript:void()" data-uri="' + track.user.link + '">' + track.user.name + '</a>';
			this.node.appendChild(td4);
		}
		
	}

	var td5 = document.createElement('td');
		td5.innerHTML = '&nbsp;';
		this.node.appendChild(td5);
		
}	

var fieldTypes = {
	'title': 'Title',
	'album': 'Album',
	'artist': 'Artist',
	'duration': 'Duration',
	'popularity': 'Popularity',
	'user': 'User'
};

/**
 * From http://stackoverflow.com/questions/391314/jquery-insertat
 **/
$.fn.insertAt = function(index, $parent) {
    return this.each(function() {
        if (index === 0) {
            $parent.prepend(this);
        } else {
            $parent.children().eq(index - 1).after(this);
        }
    });
}

var ContextView = function (playlist, options) {
	var headers = false;
	var fields = ['title', 'artist', 'duration', 'album'];
	if (options && options.headers) {
		headers = options.headers;

	}
	this.uri = playlist.uri;
	if (options && options.fields) {
		fields = options.fields;
	}
	this.fields = fields;
	this.node = document.createElement('table');
	var tbody = document.createElement('tbody');
	this.node.classList.add('sp-playlist');
	this.node.setAttribute('id', 'context_' + playlist.uri.replace(/\:/g, '__'));
	this.tbody = tbody;
	this.node.appendChild(tbody);
	this.node.setAttribute('width', '100%');
	this.node.classList.add('sp-table');
	var thead = document.createElement('thead');
	var c = "";
	for (var i = 0; i < fields.length; i++) {
		var field = fields[i];
		c += "<th>" + fieldTypes[field] + '</th>';
	}
	thead.innerHTML = '<tr>' + c + '<th style="width:10%"></th></tr>';
	this.node.setAttribute('data-uri', playlist.uri);
	for (var i = 0; i < playlist.tracks.length; i++) {
		var trackView = new TrackView(playlist.tracks[i], i, {'fields': fields});
		$(tbody).append(trackView.node);
	}
	console.log(this.node);
	var tableY = 0;
	// To make the header hovering
	if (headers) {
		this.node.appendChild(thead);
		
		window.addEventListener('scroll', function (event) {
			var tabbar = $('.sp-tabbar');

			var absolutePos = $(thead).offset();
			if (tableY == 0) {
				tableY = absolutePos.top;
			}
			if ($(window).scrollTop() >= tableY - tabbar.height() - 2) {
				var scrollOffset = $(window).scrollTop() - tableY + tabbar.height() + 2;
				$(thead).css({'transform': 'translate(0px, ' + (scrollOffset) + 'px)'});
			} else {
				$(thead).css({'transform': 'none'});

			}
		});
	}
	$(this.node).spotifize();
	track_contexts[playlist.uri] = this; // Register context here
}

var context = new Context();
/** 
 * Inserts a set of track into the given context and notify the parent
 **/
ContextView.prototype.insertTracks = function (tracks, position) {
	for (var i = 0; i < tracks.length; i++) {
		var trackView = new TrackView(tracks, i, this.fields);
		$(this.tbody).eq(position + i).after(trackView.node);
		

	}

	// Announce context update
	window.parent.postMessage({
		'action': 'contextupdated',
		'uri': this.playlist.uri,
		'position': position,
		'tracks': tracks
	}, '*');
		
	
}

var Playlist = function () {

}

var Album = function () {

}

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

var Artist = function () {

}

Playlist.lists = {};
Album.lists = {};
Artist.lists = {};
Playlist.fromURI = function (uri, callback) {
	console.log("Asking shell for getting playlist");
	window.parent.postMessage({'action': 'getPlaylist', 'uri': uri}, '*');
	var checker = setInterval(function () {
		if (uri in Playlist.lists) {
			console.log("Playlist ready for consumption");
			clearInterval(checker);
			callback(Playlist.lists[uri]);
		}
	}, 100);
};

Album.fromURI = function (uri, callback) {
	console.log("Asking shell for getting playlist");
	window.parent.postMessage({'action': 'getAlbum', 'uri': uri}, '*');
	var checker = setInterval(function () {
		if (uri in Album.lists) {
			console.log("Album ready for consumption");
			clearInterval(checker);
			callback(Album.lists[uri]);
		}
	}, 100);
	
};

var albumTracks = {};

var getAlbumTracks = function (uri, callback) {
	console.log("Asking shell for getting artist");
	window.parent.postMessage({'action': 'getAlbumTracks', 'uri': uri}, '*');
	var checker = setInterval(function () {
		if (uri in albumTracks) {
			console.log("Artist ready for consumption");
			clearInterval(checker);
			callback(albumTracks[uri]);
		}
	}, 100);
}

Artist.fromURI = function (uri, callback) {
	console.log("Asking shell for getting artist");
	window.parent.postMessage({'action': 'getArtist', 'uri': uri}, '*');
	var checker = setInterval(function () {
		if (uri in Artist.lists) {
			console.log("Artist ready for consumption");
			clearInterval(checker);
			callback(Artist.lists[uri]);
		}
	}, 100);
	

};

Search = function () {

};

Search.lists = {};


Search.search = function (query, limit, offset, type, callback) {
	var self = this;
	console.log("Asking shell for getting artist");
	window.parent.postMessage({'action': 'search', 'query': query, 'type': type, 'limit': limit, 'offset': offset}, '*');
	var checker = setInterval(function () {
		if (type + ':' + query in Search.lists) {
			console.log("Search ready for consumption");
			clearInterval(checker);
			callback(Search.lists[type + ':' + query]);
		}
	}, 100);
};

var TopList = function () {

}

TopList.lists = {};

TopList.fromURI = function (uri, callback) {
	console.log("Asking shell for getting artist");
	window.parent.postMessage({'action': 'getTopList', 'uri': uri}, '*');
	var checker = setInterval(function () {
		if (uri in TopList.lists) {
			console.log("Artist ready for consumption");
			clearInterval(checker);
			callback(TopList.lists[uri]);
		}
	}, 100);
}

function  showThrobber() {
	$('#throbber').show();
}


function hideThrobber() {
	$('#throbber').hide();
}


var AlbumView = function (album, options) {
	var table = document.createElement('table');
	table.setAttribute('width', '100%');
	table.classList.add('sp-album');
	console.log("ALBUM", album);
	table.setAttribute('data-uri', album.uri);
	var td1 = document.createElement('td');
	td1.innerHTML = '<img class="shadow" src="' + album.images[0].url + '" width="170px">';
	td1.setAttribute('valign', 'top');
	td1.setAttribute('width', '170px');
	td1.style.paddingRight = '13pt';
	var td2 = document.createElement('td');
	td2.setAttribute('valign', 'top');
	td2.innerHTML = '<h3 style="margin-bottom: 10px"><a data-uri="' + album.uri + '">' + album.name + '</a></h3>';
	console.log(td2.innerHTML);
	//alert(album.tracks);
	var self = this;
	getAlbumTracks(album.uri, function (tracks) {
		album.tracks = tracks;
		var contextView = new ContextView(album, {'fields': ['title', 'duration', 'popularity']});
		td2.appendChild(contextView.node);
	});
	table.appendChild(td1);
	table.appendChild(td2);
	console.log(table);
	this.node = table;
	this.node.style.marginBottom = '26pt';
	this.node.style.marginTop = '26pt';
	this.node.style.paddingLeft = '26pt';

}

$(document).on('click', 'a', function (event) {
	console.log("clicked link", event.target);
	parent.postMessage({'action': 'navigate', 'uri': event.target.getAttribute('data-uri')}, '*');
});

$(document).on('mousedown', '.sp-track', function (event) {
	$('.sp-track').removeClass('sp-track-selected');
	$(this).addClass('sp-track-selected');
});


/**
 * Bungalow specification
 **/
var Bungalow = function (section, options) {
	this.node = document.createElement('iframe');
}

var App = function (data) {
	this.data = data;
};

App.find = function (callback) {
	console.log("Listing apps");
	$.getJSON('http://appfinder.aleros.webfactional.com/api/index.php', function (apps) {
		callback(apps);
	});
}

App.fromURI = function (uri) {
	var parts = uri.split(/\:/g);
	var appId = parts[2];

}

App.prototype.install = function () {
	var settings = bungalow_get_settings();
	settings.apps.push(this.data);
}


