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
			$('#singles').html("");
			
			for (var i = 0; i < artist.albums.length; i++) {
				var album = artist.albums[i];
				//var div = document.createElement('div');
				//div.innerHTML = '<a data-uri="' + album.uri + '"><img src="' + album.images[0].url + '" width="120pt"><br>' + album.name + '</a>';
			
				var albumView = new AlbumView(album);
				$('#albums').append(albumView.node);
				if (i < artist.albums.length - 1)
				$('#albums').append(document.createElement('hr'));
				
			}
			for (var i = 0; i < artist.singles.length; i++) {
				var single = artist.singles[i];
				//var div = document.createElement('div');
				//div.innerHTML = '<a data-uri="' + single.uri + '"><img src="' + single.images[0].url + '" width="120pt"><br>' + single.name + '</a>';
		
				var albumView = new AlbumView(single);
				$('#singles').append(albumView.node);
				if (i < artist.singles.length - 1)
				$('#singles').append(document.createElement('hr'));
			
			}
			hideThrobber();
		});
		
	}

})