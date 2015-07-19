var albums = {};

window.addEventListener('message', function (event) {
	console.log("Event data", event.data);
	if (event.data.action === 'navigate') {
		showThrobber();
		console.log(event.data.arguments);
		var id = event.data.arguments[0];
		var uri = 'spotify:album:' + id;
		$('.sp-album').hide();
		Album.fromURI(uri, function (album) {
			$('#list').html("");
			console.log("Got album", album);
			var contextView = new ContextView(album, {'fields': ['title', 'duration', 'popularity']});
			contextView.node.classList.add('sp-album');
			contextView.node.setAttribute('id', 'album_' + uri.replace(/\:/g, '__'));
			$('#playlist').append(contextView.node);
			$('#copyrights').html(album.copyrights);
			$('#name').html('<a>' + album.name + '</a> <a class="fade" data-uri="' + album.artists[0].uri + '"> by ' + album.artists[0].name + '</a>');
			$('#image').attr('src', album.images[0].url);
			hideThrobber();
		});
		
	}

})