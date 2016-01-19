function issue() {
    dpd.cares.post({}).then(function (care) {
        console.log(care);
    })
}
window.onargumentschanged = function (event) {
    var arguments = event.data;
    dpd.cares.get(arguments[0]).then(function (care) {
        $('bungalow-header name').html(care.id);
    });
}