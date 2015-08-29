var Promise = require('bluebird');
var request = require('request');
function Echonest () {

}



Echonest.prototype.getTrackAnalysis = function (trackId) {
    return new Promise(function (resolve, fail) {
        request('http://developer.echonest.com/api/v4/track/profile?api_key=DBRXWR0QJSWGJLNST&format=json&id=' + trackId + '&bucket=audio_summary', function (error, response, body) {
            if (error) {
                fail(error);
            }
            var data = JSON.parse(body);
            data = data.response.track.audio_summary;
            var audioSummary = data.audio_summary;
            var fullAnalysis = data.analysis_url;
            /*request(fullAnalysis, function (error, response, body) {
                console.log(error);
                if (!error) {
                    resolve(JSON.parse(body));
                }
            });*/
            resolve(data);


        });
    });
}

exports.Echonest = Echonest;