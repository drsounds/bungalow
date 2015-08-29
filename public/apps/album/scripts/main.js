require(['$api/models', '$api/views'], function (models, views) {

	window.addEventListener('message', function (event) {
		console.log("Event data", event.data);
		if (event.data.action === 'navigate') {
			views.showThrobber();
			console.log(event.data.arguments);
			var id = event.data.arguments[0];
			$('.sp-album').hide();
			models.Album.fromId(id).load().then(function (album) {
				$('#album').html("");
				var albumView = new views.AlbumView(album);
				$('#album').append(albumView.node);
				views.hideThrobber();
			});

		}

	})
});