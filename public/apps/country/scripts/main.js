require(['$api/models', '$api/views'], function (models, views) {

    window.addEventListener('message', function (event) {
        console.log("Event data", event.data);
        if (event.data.action === 'navigate') {
            views.showThrobber();
            console.log(event.data.arguments);
            var id = event.data.arguments[0];


            models.Country.fromCode(id).then(function (country) {

                var header = new views.Header(country, "User");
                $('#header').html("");
                $('#header').append(header.node);

                var playlistCollection = new views.CardCollectionView(user.playlists, {}, 'playlist');

                $('#playlists').append(playlistCollection.node);

                views.hideThrobber();

            });

        }

    })
});