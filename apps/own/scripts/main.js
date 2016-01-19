var albums = {};
require(['$api/models', '$api/views'], function (models, views) {
	window.addEventListener('message', function (event) {
		console.log("Event data", event.data);
		if (event.data.action === 'navigate') {
			views.showThrobber();
			console.log(event.data.arguments);
			var id = event.data.arguments[1];
            models.User.fromId(id).load().then(function (user) {
                console.log(user);
                $('#year').html(id);
                var uri = 'bungalow:year:' + id;
                $('.sp-album').hide();
                var search = models.Search.search('artist:"Dr. Sounds"', 50, 50);
                $('#playlist').html("");
                var contextView = new views.TrackContext(search, {headers: true, 'fields': [ 'title', 'artist', 'album']});
                contextView.node.classList.add('sp-album');
                contextView.node.setAttribute('id', 'search_result_' + uri.replace(/\:/g, '__'));
                $('#playlist').append(contextView.node);
                var header = new views.SimpleHeader({
                    type: 'Own music',
                    name: 'Own music',
                    description: 'Your own music you have published on Spotify'
                });
                $('#header').html("");
                $('#header').append(header.node);
                $('#tabbar').html("");
                views.hideThrobber();
                var tabBar = new views.TabBar({
                views: [{id: 'overview', title: 'Own music'}]
            });
                $('#tabbar').append(tabBar.node);
            });
		}

	});
});