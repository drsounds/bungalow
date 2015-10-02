var models = require('@bungalow/models');
var views = require('@bungalow/views');
var $ = require('jquery');

window.addEventListener('message', function (event) {
	console.log("Event data", event.data);
	if (event.data.action === 'navigate') {
		views.showThrobber();
		console.log(event.data.arguments);
		var id = event.data.arguments[0];
		$('.sp-album').hide();
		models.Album.fromId(id).load().then(function (album) {
			$('#album').html("");
			var albumView = new views.Album(album);
			$('#album').append(albumView.node);
			views.hideThrobber();
		});

	}
});

if (app) {
	app.resolveUri = function (uri) {
	    return new Promise(function (resolve, fail) {
	        request.get('')
	    });
	};