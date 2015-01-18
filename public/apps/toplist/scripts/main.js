var albums = {};

window.addEventListener('message', function (event) {
	console.log("Event data", event.data);
	if (event.data.action === 'navigate') {
		showThrobber();
		console.log(event.data.arguments);
		var id = event.data.arguments[1];
		if (event.data.arguments.length < 1)
			id = 'world';

		var uri = 'spotify:toplist:country:' + id;

		$('.sp-album').hide();
		TopList.fromURI(uri, function (album) {
			$('#playlist').html("");
			console.log("Got album", album);
			var contextView = new ContextView(album, {'headers': true, 'fields': ['title', 'artist',  'duration', 'popularity', 'album']});
			contextView.node.classList.add('sp-toplist');
			contextView.node.setAttribute('id', 'toplist_' + uri.replace(/\:/g, '__'));
			$('#playlist').append(contextView.node);
			
			hideThrobber();
		});
		
	}

})