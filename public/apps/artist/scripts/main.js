var artists = {};

window.addEventListener('message', function (event) {
	console.log("Event data", event.data);
	if (event.data.action === 'navigate') {
		showThrobber();
		console.log(event.data.arguments);
		var id = event.data.arguments[0];
		var uri = 'spotify:artist:' + id;

		$('.sp-artist').hide();
		Artist.fromURI(uri, function (artist) {
			$('#name').html(artist.name);
			$('#albums').html("");
			
			for (var i = 0; i < artist.albums.length; i++) {
				var album = artist.albums[i];
				var div = document.createElement('li');
				div.innerHTML = '<a data-uri="' + album.uri + '"><img src="' + album.image + '" width="120pt"><br>' + album.name + '</a>';
				$('#albums').append(div);
			}
			hideThrobber();
		});
		
	}

})