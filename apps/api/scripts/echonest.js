(function (root, factory) {
    if (typeof define === 'function' && define.amd) {
        // AMD. Register as an anonymous module.
        define(['b'], factory);
    } else if (typeof module === 'object' && module.exports) {
        // Node. Does not work with strict CommonJS, but
        // only CommonJS-like environments that support module.exports,
        // like Node.
        module.exports = factory(require('b'));
    } else {
        // Browser globals (root is window)
        root.returnExports = factory(root.b);
    }
}(this, function (b) {
    //use b in some fashion.
    var Echonest = function () {

    }

    Echonest.analyzeTrack = function (id) {
        return new Promise(function (resolve, fail) {


            var xmlHttp = new XMLHttpRequest();
            xmlHttp.onchange = function () {
                if (xmlHttp.status == 200) {
                    var data = xmlHttp.responseText;
                    var audio_summary = data.response.audio_summary;
                    resolve(audio_summary);
                }
            }
            xmlHttp.responseType = 'json';
            xmlHttp.open('GET', 'http://developer.echonest.com/api/v4/track/profile?api_key=DBRXWR0QJSWGJLNST&format=json&id=spotify:track:' + id + '&bucket=audio_summary');
            xmlHttp.send(null);
        });
    }

    return Echonest;
})