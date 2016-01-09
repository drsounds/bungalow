require(['$api/models', '$api/views'], function (models, views) {
    window.addEventListener('message', function (event) {
        console.log("Event data", event.data);
        if (event.data.action === 'navigate') {
            views.showThrobber();
            console.log(event.data.arguments);
            var id = event.data.arguments[0];

            $('.sp-artist').hide();	
			$('#playlists').html("");
           
            models.User.fromId(id).load().then(function (user) {
                
                console.log(user);
                var header = new views.SimpleHeader({
                    uri: user.uri,
                    name: user.name,
                    imageSize: 128,
                    images: user.images,
                    type: 'user'
                });
                $('#header').html("");
                $('#header').append(header.node);
                var playlistCollection = new views.AlbumCollection(user, {extend: false}, 'playlist');

                $('#playlists').append(playlistCollection.node);

                views.hideThrobber();

            });

        }

    })
});