require(['$api/models', '$api/views'], function (models, views) {

    window.addEventListener('message', function (event) {
        console.log("Event data", event.data);
        if (event.data.action === 'navigate') {
            console.log(event.data.arguments);
            var id = event.data.arguments[0];
            
            var header = new views.SimpleHeader({
                    type: 'hashtag',
                    name: '#' + id,
                    description: 'Latest tweets from hashtag',
                   
            });
            var tabBar = new views.TabBar({
                views: [{id:'overview', title: 'Hashtag'}]
            });
            $('#tabbar').html("");
            $('#tabbar').append(tabBar.node);
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