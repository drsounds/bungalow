window.onmessage = function (event) {
    if (event.data.action === 'navigate') {
        var arguments = event.data.arguments;
        $.getJSON('http://api.radioflow.se/v1/podcast/?format=json', function (objects) {

        });
    }
}