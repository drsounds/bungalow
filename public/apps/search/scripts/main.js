var playlists = {};

window.addEventListener('message', function (event) {

	if (event.data.action === 'navigate') {
		$('#search').html("");
		$('#artists').html("");
		$('#albums').html("");
		showThrobber();
		var query = event.data.arguments.join(':');
		Search.search(query, 50, 0, 'track', function (tracks) {

			var contextView = new ContextView({
				'uri': 'spotify:search:' + query,
				'tracks': tracks
			}, {headers:true, fields: ['title', 'artist', 'duration', 'popularity', 'album']});
			$('#search').append(contextView.node);
			Search.search(query, 50, 0, 'artist', function (artists) {
				for (var i = 0; i < artists.length && i < 10; i++) {
					var artist = artists[i];
					var li = document.createElement('li');
					li.innerHTML = '<a data-uri="' + artist.uri + '">' + artist.name + '</a>';
					$('#artists').append(li);
				}
			});
			Search.search(query, 50, 0, 'album', function (albums) {
				for (var i = 0; i < albums.length && i < 10; i++) {
					var album = albums[i];
					var li = document.createElement('li');
					li.innerHTML = '<a data-uri="' + album.uri + '">' + album.name + '</a>';
					$('#albums').append(li);
				}
			});
		});
		hideThrobber();
	}
});