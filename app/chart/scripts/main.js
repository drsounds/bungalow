var albums = {};

window.addEventListener('message', function (event) {
	console.log("Event data", event.data);
	if (event.data.action === 'navigate') {
		showThrobber();
		console.log(event.data.arguments);
		var id = event.data.arguments[1];
		if (event.data.arguments.length < 1)
			id = 'world';


		$('.sp-album').hide();
		Chart.fromId(id, function (album) {
			$('#playlist').html("");
			console.log("Got album", album);
			var contextView = new ContextView(album, {'headers': false, 'fields': ['title', 'artist',  'popularity']});
			contextView.node.classList.add('sp-chart');
			contextView.node.setAttribute('id', 'toplist_' + uri.replace(/\:/g, '__'));
			$('#toplist1').append(contextView.node);
			
			hideThrobber();
		});
	}

})