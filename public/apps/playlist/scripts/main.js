var playlists = {};

window.addEventListener('message', function (event) {
	console.log("Event data", event.data);
	if (event.data.action === 'tracksadded') {
		var playlistURI = event.data.uri;
		var position = event.data.position;
		var tracks = event.data.tracks;
	}
	if (event.data.action === 'navigate') {
		showThrobber();
		console.log(event.data.arguments);
		var user = event.data.arguments[0];
		var playlist = event.data.arguments[2];
		var uri = 'spotify:user:' + user + ':playlist:' + playlist;
		$('.sp-playlist').hide();
		if (uri in playlists) {
			$('#context_' + uri.replace(/\:/g, '__')).show();
			hideThrobber();
		} else {
			Playlist.fromURI(uri, function (playlist) {
				$('.sp-playlist').hide();
				playlists[uri] = playlist;
				var contextView = new ContextView(playlist, {headers:true, fields: ['title', 'artist', 'album', 'user']});
				contextView.node.classList.add('sp-playlist');
				$('#list').append(contextView.node);
				$('#title').html(playlist.name);
				//$('#author').html(playlist.user.displayName);
				$('#description').html(playlist.description);
				hideThrobber();
			});
		}
	}

})

function followPlaylist () {
	Playlist.fromURI(uri, function (playlist) {
		Playlist.follow(uri);
	});
}