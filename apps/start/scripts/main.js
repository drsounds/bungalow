require(['$api/views', '$api/models'], function (views, models) {
	var playlists = {};

	window.addEventListener('message', function (event) {
		console.log("Event data", event.data);
		if (event.data.action === 'tracksadded') {
		}
		if (event.data.action === 'navigate') {
			$('#categories').html("");
			if (event.data.arguments.length == 0) {
	            $.getJSON('/api/music/browse/categories', function (result) {
	            	console.log(result);
	            	$.each(result.objects, function (i, category) {
	            		var div = document.createElement('div');
	            		div.classList.add('col-md-2');

	            		category.type = 'category';
	            		category.uri = 'bungalow:genre:' + category.id;
	            		category.images = category.icons;
	            		console.log(category);
	            		var catView = new views.Card(category);
	            		
	            		$(div).append(catView.node);
	            		$('#categories').append(div);
	            	});
	            });
	        }
		
		}

	});
});