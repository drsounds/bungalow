require(['$api/views', '$api/models'], function (views, models) {
	var playlists = {};

	window.addEventListener('message', function (event) {
	
		if (event.data.action === 'navigate') {
			views.showThrobber();
			console.log(event.data.arguments);
			var username = event.data.arguments[0];
			console.log("USER", username);
			var appFinder = new models.AppFinder();
			console.log(playlist);

			var header = new views.Header(appFinder {type: 'appfinder'});



			var playlistView = document.createElement('div');

			playlistView.appendChild(header.node);

			var contextView = new views.TrackContext(playlist, {headers:true, sticky: true, reorder:true, fields: ['title', 'artist' , 'album', 'user']});
			contextView.node.classList.add('sp-playlist');
			playlistView.appendChild(contextView.node);

			contextView.show();
			
			$('#sp-playlist').html("");
			$('#sp-playlist').append(playlistView);
			views.hideThrobber();
			
		
		}

	});
});