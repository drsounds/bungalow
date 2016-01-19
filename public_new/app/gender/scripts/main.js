window.onargumentschanged = function (event) {
    var arguments = event.data;
    console.log(dpd.genders);
    dpd.genders.get({slug: arguments[0]}).then (function (genders) {
        console.log(genders[0]);
        $('bungalow-header name').html(genders[0].name);
    });
}