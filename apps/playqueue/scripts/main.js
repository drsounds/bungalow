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
			var uri = 'bungalow:playqueue';
				console.log(playlist);

				var header = new views.SimpleHeader({
                    name: 'Play queue',
                    type: 'plaqueue',
                    uri: 'bungalow:playqueue'
                });



				var playlistView = document.createElement('div');

				$('#header').append(header.node);
/*
				var contextView = new views.TrackContext({}, {headers:true,  sticky: true, reorder:true, fields: [ 'title', 'artist' , 'duration', 'album']});
		*/
            $('#tabbar').html("");
            var tabBar = new views.TabBar({
                views:[{id: 'overview', title: 'Play Queue'}]
            });
            $('#tabbar').html("");
            $('#tabbar').append(tabBar.node);
		      views.hideThrobber();
		}

	});
});