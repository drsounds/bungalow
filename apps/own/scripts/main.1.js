var albums = {};
require(['$api/models', '$api/views'], function (models, views) {
	window.addEventListener('message', function (event) {
		console.log("Event data", event.data);
		if (event.data.action === 'navigate') {
			views.showThrobber();
			console.log(event.data.arguments);
			var id = event.data.arguments[0];
			$('#year').html(id);
			var uri = 'bungalow:year:' + id;
			$('.sp-album').hide();
			var search = models.Search.search('year:' + id, 10, 0);
			$('#playlist').html("");
			var contextView = new views.TrackContext(search, {headers: true, 'fields': ['title', 'duration', 'popularity']});
			contextView.node.classList.add('sp-album');
			contextView.node.setAttribute('id', 'search_result_' + uri.replace(/\:/g, '__'));
			$('#playlist').append(contextView.node);
			views.hideThrobber();
		       var tabBar = new views.TabBar({
              views: [{id: 'overview', title: id}]
          });
            $('#tabbar').append(tabBar.node);
		}

	});
});