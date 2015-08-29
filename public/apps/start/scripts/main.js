
	console.log("Getting songs from playlist");
	Playlist.fromUserId('drsounds', '1TdnOdTVGt82cUGmbH9s5Q').then(function (playlist) {
		var contextView = new TrackContextView(playlist);
		$('#featuredSongs').append(contextView.node);
	});
