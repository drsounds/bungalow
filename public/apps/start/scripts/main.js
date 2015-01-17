
	console.log("Getting songs from playlist");
	Playlist.fromURI('spotify:user:drsounds:playlist:1TdnOdTVGt82cUGmbH9s5Q', function (playlist) {
		var contextView = new ContextView(playlist);
		$('#featuredSongs').append(contextView.node);
	});
