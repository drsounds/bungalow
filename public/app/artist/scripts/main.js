require(['$api/models', '$api/views'], function (models, views) {

	window.addEventListener('message', function (event) {
		console.log("Event data", event.data);
		if (event.data.action === 'navigate') {
			views.showThrobber();
			console.log(event.data.arguments);
			var id = event.data.arguments[0];

			$('.sp-artist').hide();

			var tabBar = new views.TabBar({
				'views':[
					{id: 'overview', title: 'Overview'},
					{id: 'biography', title: 'Biography'}
				]
			});


			$(tabBar.node).insertAfter('#header');

			models.Artist.fromId(id).then(function (artist) {
				console.log("Loaded artist");
				$('#artist_image').removeClass('shadow');
				$('#name').html(artist.name);
				$('#name').attr('data-uri', 'bungalow:artist:' + id);
				$('#artist_image').attr('src', artist.images[0].url);
				if (artist.images[0].url) {
					$('#artist_image').addClass('shadow');
				}
				$('#albums').html("");
				$('#singles').html("");
				$('#artistLink').html(artist.name);
				$('#artistLink').attr('data-uri', 'bungalow:artist:' + id);
				var albumCollection = new views.AlbumCollectionView(artist);
				console.log("Appending album collection");
				$('#albums').append(albumCollection.node);
				views.hideThrobber();
				console.log(artist);
				var header = new views.Header(artist, "Artist");
				$('#header').html("");
				$('#header').append(header.node);


			});

		}

	})
});