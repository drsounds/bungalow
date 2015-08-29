require(['$api/models', '$api/views'], function (models, views) {
	var playlists = {};
	window.addEventListener('message', function (event) {
		
		if (event.data.action === 'navigate') {
			$('#search').html("");
			$('#artists').html("");
			$('#albums').html("");
			views.showThrobber();
			console.log("A");
			alert("A");

			var query = event.data.arguments.join(':');
			var uri = 'bungalow:search:' + query;
			$('.sp-table').hide();
			if (uri in playlists) {
				playlists[uri].show();
				return;
			}

			var search = models.Search.search(query, 50, 0);
			console.log("Query ", query);
			var contextView = new views.TrackContextView(search, {
				headers: true,
				fields: ['title', 'artist', 'duration', 'popularity', 'album']
			});
			playlists['bungalow:search:' + query] = contextView;
			$('#search').append(contextView.node);
			contextView.show();	
			alert("A");


			views.hideThrobber();
		}
	});
});