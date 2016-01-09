var albums = {};
require(['$api/models', '$api/views'], function (models, views) {
	window.addEventListener('message', function (event) {
		console.log("Event data", event.data);
		if (event.data.action === 'navigate') {
			views.showThrobber();
			console.log(event.data.arguments);
            var header = new views.SimpleHeader({
                name: 'Hipster',
                type: 'hipster'
            });
			var id = event.data.arguments[0];
			$('#year').html(id);
			var uri = 'bungalow:hipster:' + id;
			$('.sp-album').hide();
			var search = models.Search.search('tag:hipster ' + id, 130, 0, 'tracks');
			$('#playlist').html("");
			var contextView = new views.TrackContext(search, {headers: true, 'fields': ['title', 'artist', 'duration', 'album', 'popularity']});
			contextView.node.classList.add('sp-album');
			contextView.node.setAttribute('id', 'search_result_' + uri.replace(/\:/g, '__'));
			$('#playlist').append(contextView.node);
			views.hideThrobber();
            var tabBar = new views.TabBar({
              views: [{id: 'overview', title: 'Hipster'}]
            });
            $('#tabbar').append(tabBar.node);
            $('#header').append(header.node);
		}

	});
});