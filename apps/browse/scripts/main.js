var playlists = {};

window.addEventListener('message', function (event) {
	console.log("Event data", event.data);
	if (event.data.action === 'navigate') {
		showThrobber();
		$.getJSON

		Playlist.fromURI(uri, function (playlist) {
			$('#list').html("");
			var contextView = new ContextView(playlist, {headers:true, fields: ['title', 'artist', 'duration', 'popularity', 'album', 'user']});
			contextView.node.classList.add('sp-playlist');
			contextView.node.setAttribute('id', 'playlist_' + uri.replace(/\:/g, '__'));
			$('#list').append(contextView.node);
			hideThrobber();
		});
	}

})