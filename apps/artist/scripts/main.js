require(['$api/models', '$api/views'], function (models, views) {

	window.addEventListener('message', function (event) {
		console.log("Event data", event.data);
		if (event.data.action === 'navigate') {
			views.showThrobber();
			console.log(event.data.arguments);
			var id = event.data.arguments[0];
			$('.sp-tabbar').remove();
			$('.sp-artist').hide();

			console.log(models.Artist.fromId);
			console.log(views);
			models.Artist.fromId(id).load().then(function (artist) {
				
				$('#albums').html("");
				$('#singles').html("");
				$('#artistLink').html(artist.name);
				$('#artistLink').attr('data-uri', 'bungalow:artist:' + id);
				var albumCollection = new views.AlbumCollection(artist, { imageSize: 128});
				console.log("Appending album collection");
				$('#albums').append(albumCollection.node);
				views.hideThrobber();
				console.log(artist);
				var header = new views.SimpleHeader(artist, {
                        imageSize: 192
				});
				$('#header').html("");
				$('#header').append(header.node);

				var tabBar = new views.TabBar({
					views:[
						{id: 'overview', title: 'Overview'},
						{id: 'related', title: 'Related Artists'},
						{id: 'about', title: 'About'}
					]
				});
				$('#tabbar').append(tabBar.node);

				var template = document.querySelector('#template').innerHTML;

				template = _.template(template);

				$('#info').html(template({object: artist}));


			});

		}

	})
});