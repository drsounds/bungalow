require(['$api/models', '$api/views'], function (models, views) {

    window.addEventListener('message', function (event) {
        console.log("Event data", event.data);
        if (event.data.action === 'navigate') {
            console.log(event.data.arguments);
            var id = event.data.arguments[0];
            $('#title').html('#' + id);

        }

    })
});