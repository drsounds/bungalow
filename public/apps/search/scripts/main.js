var playlists = {};

window.addEventListener('message', function (event) {
	console.log("Event data", event.data);
	if (event.data.action === 'navigate') {
		showThrobber();
		var query = event.data.arguments.join(':');
		Search.search(query, 50, 0, function (tracks) {
			$('#search').html("");
			var contextView = new ContextView({
				'tracks': tracks}, {headers:true, fields: ['title', 'artist', 'duration', 'album', 'popularity']});
			$('#search').append(contextView.node);
			hideThrobber();
		});
	}
});