var albums = {};

window.addEventListener('message', function (event) {
    console.log("Event data", event.data);
    if (event.data.action === 'navigate') {
        showThrobber();
        console.log(event.data.arguments);
        var id = event.data.arguments[0];
        var uri = 'bungalow:year:' + id;
        $('.sp-album').hide();
        Search.search('year:' + id, 10, 0, 'tracks', function (result) {
            $('#list').html("");
            console.log("Got album", album);
            var contextView = new TrackContextView(result, {'fields': ['title', 'duration', 'popularity']});
            contextView.node.classList.add('sp-album');
            contextView.node.setAttribute('id', 'search_result_' + uri.replace(/\:/g, '__'));
            $('#playlist').append(contextView.node);
            hideThrobber();
        });

    }

})