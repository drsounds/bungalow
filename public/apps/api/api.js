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
	var track = {
		'uri': this.getAttribute('data-uri'),
		'duration': this.getAttribute('data-duration'),
		'name': this.getElementsByTagName('td')[0].textContent,
		'artists': [{
			'name': this.getElementsByTagName('td')[1].getElementsByTagName('a')[0].innerText,
			'uri': this.getElementsByTagName('td')[1].getElementsByTagName('a')[0].getAttribute('data-uri')
		}]
	};
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
		$(this).find('.sp-track').mousedown(function (event) {
			$('.sp-track').removeClass('sp-track-selected');
			$(this).addClass('sp-track-selected');
		});
		$(this).find('.sp-track').dblclick(function (event) {
			var uri = $(event.target).parent().parent().parent().attr('data-uri');
			var parent = $(event.target).parent().parent().parent();
			console.log("Uri", uri);
			console.log("Parent", parent[0]);
			var target = event.target;
			console.log(self);
			var index = $(self).find('.sp-track').index(this);
			console.log(context);
			context.play(uri, index);
		
		});
	};
}(jQuery));


var TrackView = function (track) {
	this.node = document.createElement('tr');
	this.node.classList.add('sp-track');
	this.node.setAttribute('data-uri', track.uri);
	var td1 = document.createElement('td');
	td1.innerHTML = track.name;
	var td2 = document.createElement('td');
	td2.innerHTML = '<a href="javascript:void()" data-uri="' + track.artists[0].uri + '">' + track.artists[0].name + '</a>';
	this.node.appendChild(td1);
	this.node.appendChild(td2);
	var td3 = document.createElement('td');
	td3.innerHTML = '-:--';
	this.node.appendChild(td3);
	var td4 = document.createElement('td');
	td4.innerHTML = '<a href="javascript:void()" data-uri="' + track.album.link + '">' + track.album.name + '</a>';
	this.node.appendChild(td4);
	var td4 = document.createElement('td');
	td4.innerHTML = '<a href="javascript:void()" data-uri="' + track.user.link + '">' + track.user.name + '</a>';
	this.node.appendChild(td4);
	
}	

var ContextView = function (playlist, options) {
	var headers = false;
	if (options && options.headers) {
		headers = options.headers;
	}
	this.node = document.createElement('table');
	var tbody = document.createElement('tbody');
	this.node.appendChild(tbody);
	this.node.setAttribute('width', '100%');
	this.node.classList.add('sp-table');
	var thead = document.createElement('thead');
	thead.innerHTML = '<tr><th>Title</th><th>Artist</th><th>Duration</th><th>Album</th><th>User</th></tr>';
	this.node.setAttribute('data-uri', playlist.uri);
	this.node.appendChild(thead);
	for (var i = 0; i < playlist.tracks.length; i++) {
		var trackView = new TrackView(playlist.tracks[i]);
		$(tbody).append(trackView.node);
	}
	console.log(this.node);
	$(this.node).spotifize();
	var tableY = 0;
	// To make the header hovering
	window.addEventListener('scroll', function (event) {
		var absolutePos = $(thead).offset();
		if (tableY == 0) {
			tableY = absolutePos.top;
		}
		if ($(window).scrollTop() >= tableY) {
			var scrollOffset = $(window).scrollTop() - tableY;
			$(thead).css({'transform': 'translate(0px, ' + scrollOffset + 'px)', 'border': '1px solid black'});
		} else {
			$(thead).css({'transform': 'none', 'border': 'none'});

		}
	});
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
		Album.lists[album.uri] = playlist;
	}
	if (event.data.action === 'trackstarted') {
		var uri = event.data.uri;

		$('.sp-table[data-uri="' + uri + '"] .sp-track').removeClass('sp-now-playing');
		$('.sp-table[data-uri="' + uri + '"] .sp-track').get(event.data.index).classList.add('sp-now-playing');
		
	}
});

Playlist.lists = {};

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