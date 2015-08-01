require(['$api/models', '$api/views'], function (models, views) {

    window.addEventListener('message', function (event) {
        console.log("Event data", event.data);
        if (event.data.action === 'navigate') {
            views.showThrobber();
            console.log(event.data.arguments);
            var id = event.data.arguments[0];

            $('.sp-artist').hide();

            var tabBar = new views.TabBar({
                'views':[
                    {id: 'overview', title: 'Overview'}
                ]
            });


            $(tabBar.node).insertAfter('#header');

            models.User.fromId(id).then(function (user) {
                console.log(user);
                var header = new views.Header(user, "User");
                $('#header').html("");
                $('#header').append(header.node);

                var playlistCollection = new views.CardCollectionView(user.playlists, {}, 'playlist');

                $('#playlists').append(playlistCollection.node);

                views.hideThrobber();

            });

        }

    })
});