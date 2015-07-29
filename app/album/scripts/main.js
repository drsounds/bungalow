var albums = {};

window.addEventListener('message', function (event) {
	console.log("Event data", event.data);
	if (event.data.action === 'navigate') {
		showThrobber();
		console.log(event.data.arguments);
		var id = event.data.arguments[0];
		$('.sp-album').hide();
		var t = Album.byId(id);
		console.log(t);
		Album.byId(id).then(function (album) {
			$('#list').html("");
			console.log("Got album", album);
			var contextView = new TrackContextView(album, {'fields': ['title', 'duration', 'popularity']});
			contextView.node.classList.add('sp-album');
			contextView.node.setAttribute('id', 'album_bungalow__album__' + id);
			$('#playlist').append(contextView.node);
			$('#copyrights').html(album.copyrights);
			$('#name').html('<a>' + album.name + '</a> <a class="fade" data-uri="' + album.artists[0].uri + '"> by ' + album.artists[0].name + '</a>');
			$('#image').attr('src', album.images[0].url);
			hideThrobber();
		});
		
	}

})