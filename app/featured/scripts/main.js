var playlists = {};

window.addEventListener('message', function (event) {
    console.log("Event data", event.data);
    if (event.data.action === 'tracksadded') {
        var playlistURI = event.data.uri;
        var position = event.data.position;
        var tracks = event.data.tracks;
    }
    if (event.data.action === 'navigate') {
        showThrobber();
        console.log(event.data.arguments);
        var uri = 'bungalow:user:drsounds:playlist:763eLyGqbJrXpuwdI5tlPV';
        var user = 'drsounds';
        var playlist = '763eLyGqbJrXpuwdI5tlPV';
        $('.sp-playlist').hide();
        if (uri in playlists) {
            $('#context_' + uri.replace(/\:/g, '__')).show();
            hideThrobber();
        } else {
            console.log("A");
            Playlist.fromUserId(user, playlist).then(function (playlist) {
                $('.sp-playlist').hide();
                playlists[uri] = playlist;
                var contextView = new TrackContextView(playlist, {headers:true, fields: ['title', 'artist', 'album']});
                contextView.node.classList.add('sp-playlist');
                $('#list').append(contextView.node);
                //$('#title').html(playlist.name);
                // $('#description').html(playlist.description);
                hideThrobber();
            });
        }
    }

})