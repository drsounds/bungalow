require([], function () {
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

    exports = Echonest;
})