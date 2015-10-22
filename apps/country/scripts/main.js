var models = require('@bungalow/models');
var views = require('@api/views');

window.addEventListener('message', function (event) {
    console.log("Event data", event.data);
    if (event.data.action === 'navigate') {
        views.showThrobber();
        console.log(event.data.arguments);
        var id = event.data.arguments[0];


        models.Country.fromCode(id).then(function (country) {
			console.log("Country", country);
            var header = new views.Header(country, "Country");
            $('#header').html("");
            $('#header').append(header.node);

          

        });

    }

});