var models = require('@bungalow/models');
var views = require('@bungalow/views');
var $ = require('jquery');
window.addEventListener('message', function (event) {
    console.log("Event data", event.data);
    if (event.data.action === 'navigate') {
        views.showThrobber();
        console.log(event.data.arguments);
        var id = event.data.arguments[0];


        models.Country.fromCode(id).load('').then(function (country) {
            console.log("Country", country);
            var header = new views.SimpleHeader(country, "Country");
            $('#header').html("");
            $('#header').append(header.node);

          

        });

    }

});