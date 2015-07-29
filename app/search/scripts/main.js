var playlists = {};

window.addEventListener('message', function (event) {

	if (event.data.action === 'navigate') {
		$('#search').html("");
		$('#artists').html("");
		$('#albums').html("");
		showThrobber();

		var query = event.data.arguments.join(':');
		var uri = 'bungalow:search:' + query;
		$('.sp-table').hide();
		if (uri in playlists) {
			playlists[uri].show();
			return;
		}

		var search = Search.search(query, 50, 0);

		var contextView = new TrackContextView(search, {headers:true, fields: ['title', 'artist', 'duration', 'popularity', 'album']});
		playlists['bungalow:search:' + query] = contextView;
		$('#search').append(contextView.node);
		contextView.show();


		hideThrobber();
	}
});