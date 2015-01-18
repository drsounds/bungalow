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
	console.log("Start playing track");
	var sel = '.sp-table[data-uri="' + uri.replace(/\:/g, '__') + '"]';
	$(sel + ' .sp-track').removeClass('sp-now-playing');

	var tracks = document.querySelectorAll('.sp-track');

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

var context = new Context();
(function ($) {
	$.fn.spotifize = function (options) {
		console.log("Spotifize");
		var self = this;

		

		
	};
}(jQuery));

$(document).on('dblclick', '.sp-track', function (event) {
	var uri = $(event.target).parent().attr('data-uri');
	var parent = $(event.target).parent().parent().parent();
	console.log("Uri", uri);
	console.log("Parent", parent[0]);
	var index = $('.sp-track').index(this);
	console.log(context);
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

var TrackView = function (track, options) {
	this.node = document.createElement('tr');
	this.node.classList.add('sp-track');
	this.node.setAttribute('data-uri', track.uri);
	this.node.setAttribute('data-object', JSON.stringify(track));
	this.node.setAttribute('draggable', true);
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
			td3.innerHTML = toMS(track.duration);
			this.node.appendChild(td3);
		}
		if (field === 'album') {
			var td4 = document.createElement('td');
			td4.innerHTML = '<a href="javascript:void()" data-uri="' + track.album.link + '">' + track.album.name + '</a>';
			this.node.appendChild(td4);
		}
		if (field === 'popularity') {
			var td4 = document.createElement('td');
			td4.innerHTML = '<meter style="width:100%" min="0" max="100" value="' + track.popularity + '">';
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


var ContextView = function (playlist, options) {
	var headers = false;
	var fields = ['title', 'artist', 'duration', 'album'];
	if (options && options.headers) {
		headers = options.headers;

	}
	if (options && options.fields) {
		fields = options.fields;
	}
	this.node = document.createElement('table');
	var tbody = document.createElement('tbody');
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
		var trackView = new TrackView(playlist.tracks[i], {'fields': fields});
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
		Search.lists[event.data.query] = search;
		console.log(event.data.query);
	}
	if (event.data.action === 'trackstarted') {
		var uri = event.data.uri;

		$('.sp-table[data-uri="' + uri + '"] .sp-track').removeClass('sp-now-playing');
		$('.sp-table[data-uri="' + uri + '"] .sp-track').get(event.data.index).classList.add('sp-now-playing');
		
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


Search.search = function (query, limit, offset, callback) {
	var self = this;
	console.log("Asking shell for getting artist");
	window.parent.postMessage({'action': 'search', 'query': query, 'limit': limit, 'offset': offset}, '*');
	var checker = setInterval(function () {
		if (query in Search.lists) {
			console.log("Search ready for consumption");
			clearInterval(checker);
			callback(Search.lists[query]);
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
	table.classList.add('sp-album');
	table.setAttribute('data-uri', album.uri);
	var td1 = document.createElement('td');
	td1.innerHTML = '<img src="' + album.image + '" width="64">';
	td1.setAttribute('valign', 'top');
	var td2 = document.createElement('td');
	td2.setAttribute('valign', 'top');
	td2.innerHTML = '<h3><a href="' + album.uri + '">' + album.name + '</a></h3>';
	//alert(album.tracks);
	var contextView = new ContextView(album, {'fields': ['title', 'duration', 'popularity']});
	td2.appendChild(contextView.node);
	table.appendChild(td2);
	this.node = table;
}

$(document).on('click', 'a', function (event) {
	console.log("clicked link", event.target);
	parent.postMessage({'action': 'navigate', 'uri': event.target.getAttribute('data-uri')}, '*');
});

$(document).on('mousedown', '.sp-track', function (event) {
	$('.sp-track').removeClass('sp-track-selected');
	$(this).addClass('sp-track-selected');
});