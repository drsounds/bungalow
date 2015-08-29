window.onargumentschanged = function (event) {
    var arguments = event.data;
    console.log(dpd.threats);
    dpd.threats.get(arguments[0]).then (function (threat) {
        console.log(threat);
        $('bungalow-header name').html(threat.name);
    });

    
}