var playlists = {};

window.addEventListener('message', function (event) {
	console.log("Event data", event.data);
	if (event.data.action === 'navigate') {
		console.log(event.data.arguments);
		var user = event.data.arguments[0];
		var playlist = event.data.arguments[2];
		var uri = 'spotify:user:' + user + ':playlist:' + playlist;

		$('.sp-playlist').hide();
		if (uri in playlists) {
			var playlist = playlists[uri];
			var contextView = new ContextView(playlist);
			$('#playlist_' + uri.replace(/\:/g, '__')).show();
		
		}

		Playlist.fromURI(uri, function (playlist) {
			$('#list').html("");
			var contextView = new ContextView(playlist, {headers:true});
			contextView.node.classList.add('sp-playlist');
			contextView.node.setAttribute('id', 'playlist_' + uri.replace(/\:/g, '__'));
			$('#list').append(contextView.node);
		});
	}

})