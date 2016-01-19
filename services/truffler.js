var fs = require('fs');
var request = require('request');
var echonest = require('./echonest.js');
var Promise = require('bluebird');

function Truffler () {
    this.echonest = new echonest.Echonest();
}

Truffler.prototype.compareTracks = function (id1, id2) {
    Promise.join(

            this.echonest.getTrackAnalysis(id1),
            this.echonest.getTrackAnalysis(id2), function (track1, track2) {
            var analyze = new Analyze(track1, track2);
            console.log(analyze);

        }
    )
};


var Analyze = function () {
    console.log(arguments);
    this.myTrack = arguments[0];
    this.tracks = Array.prototype.slice.call(arguments, 1);
    this.count = this.tracks.length;
    this.tracks = this.tracks.slice(1);
    // General stuff





    this.key = this.balanceProp('key', this.myTrack, this.tracks);
    this.loudness = this.balanceProp('loudness', this.myTrack, this.tracks);
    this.tempo = this.balanceProp('tempo', this.myTrack, this.tracks);
}

Analyze.prototype.avgProp = function (prop, collection) {
    var val = 0;
    collection.map(function (profile) {
        val += profile[prop];
    });
    val /= collection.length;
    return val;
};

Analyze.prototype.balanceProp = function (prop, object, collection) {
    var avgProp = this.avgProp(prop, collection);
    return object[prop] / avgProp;

}


exports = Truffler;

var truffler = new Truffler();
truffler.compareTracks('spotify:track:2IUOujqpbIi67mGsX9rKTg', 'spotify:track:0o5pMvYl8Gq8TpnefQ1ONr')