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
            , "Artist");
            $('#header').html("");
            $('#header').append(header.node);


        }

    })
});