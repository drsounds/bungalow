require(['$api/views', '$api/models'], function (views, models) {
	var playlists = {};

	window.addEventListener('message', function (event) {
		console.log("Event data", event.data);
		if (event.data.action === 'tracksadded') {
			var playlistURI = Event.data.uri;
			var position = event.data.position;
			var tracks = event.data.tracks;
		}
		if (event.data.action === 'navigate') {
			views.showThrobber();
			console.log(event.data.arguments);
			var username = event.data.arguments[0];
			console.log("USER", username);
			var id = event.data.arguments[2];
			var uri = 'bungalow:user:' + username + ':playlist:' + id;
			models.Playlist.fromUserId(username, id).load().then(function (playlist) {
				console.log(playlist);

				var header = new views.SimpleHeader(playlist);



				var playlistView = document.createElement('div');

				playlistView.appendChild(header.node);

				var contextView = new views.TrackContext(playlist, {headers:true, sticky: true, reorder:true, fields: [ 'title', 'artist' , 'duration', 'album']});
				contextView.node.classList.add('sp-playlist');
				playlistView.appendChild(contextView.node);

				contextView.show();
				
				$('#sp-playlist').html("");
				$('#sp-playlist').append(playlistView);
				views.hideThrobber();
			});
		
		}

	});
});