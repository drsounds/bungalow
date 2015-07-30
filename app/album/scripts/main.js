require(['$api/models', '$api/views'], function (models, views) {

	window.addEventListener('message', function (event) {
		console.log("Event data", event.data);
		if (event.data.action === 'navigate') {
			views.showThrobber();
			console.log(event.data.arguments);
			var id = event.data.arguments[0];
			$('.sp-album').hide();
			models.Album.fromId(id).then(function (album) {
				$('#list').html("");
				console.log("Got album", album);
				var contextView = new views.TrackContextView(album, {'fields': ['title', 'duration', 'popularity']});
				contextView.node.classList.add('sp-album');
				contextView.node.setAttribute('id', 'album_bungalow__album__' + id);
				$('#playlist').append(contextView.node);
				$('#copyrights').html(album.copyrights);
				$('#name').html('<a>' + album.name + '</a> <a class="fade" data-uri="' + album.artists[0].uri + '"> by ' + album.artists[0].name + '</a>');
				$('#image').attr('src', album.images[0].url);
				views.hideThrobber();
			});

		}

	})
});