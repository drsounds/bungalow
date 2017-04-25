require(['$api/models', '$api/views'], function (models, views) {

    window.addEventListener('message', function (event) {
        console.log("Event data", event.data);
        if (event.data.action === 'navigate') {
            console.log(event.data.arguments);
            var id = event.data.arguments[0];
            var hashtag = {
                name: '#' + id,
                id: id,
                icon: '#',
                images: [{
                    flat: true,
                    url: ''
                }]
            };
            var header = new views.Header(hashtag, {
                    type: 'hashtag',
                    tabs: {
                        'views':[
                            {id: 'overview', title: 'Overview'}
                        ],
                        slicky: true
                    }
                }
            , 128);
            $('#header').html("");
            $('#header').append(header.node);

            // Get hashtag
            var hashtag = models.Hashtag.fromTag(event.data.arguments[0]);

            var feed = new views.Feed(hashtag);

            $('#feed').html("");
            $('#feed').append(feed.node);
			$('#hashtag').html(id);

        }

    })
});