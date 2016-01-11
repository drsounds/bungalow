require(['$api/views', '$api/models'], function (views, models) {
	var playlists = {};

	window.addEventListener('message', function (event) {
		console.log("Event data", event.data);
		if (event.data.action === 'tracksadded') {
		}
		if (event.data.action === 'navigate') {
			$('#categories').html("");
      
            if (event.data.arguments.length == 1) {
                  $.getJSON('/api/music/browse/categories/' + event.data.arguments[0] + '/playlists', function (result) {
                  	console.log(result);
                  	$.each(result.objects, function (i, playlist) {
                  		var div = document.createElement('div');
                  		div.classList.add('col-md-2');

                  		console.log(playlist);
                  		var catView = new views.Card(playlist);
                  		
                  		$(div).append(catView.node);
                  		$('#playlists').append(div);
                  	});
                  });
      		
      		}
            }

	});
});