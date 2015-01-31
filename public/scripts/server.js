var express = require('express');
var app = express();
var path = require('path');
var bungalow = require('./bungalow.js');
var fungalow = new bungalow.Fungalow();
var execPath = process.env.PWD;

    execPath = process.env.PWD;

console.log(execPath);
var router = new express.Router();
var allowCrossDomain = function(req, res, next) {
    res.header('Access-Control-Allow-Origin', '*');
    res.header('Access-Control-Allow-Methods', 'GET,PUT,POST,DELETE');
    res.header('Access-Control-Allow-Headers', 'Content-Type');

    next();
}
app.use(allowCrossDomain);
app.use(express.static(execPath + '/public'));

app.get('/api/player/play', function (req, res, next) {
    fungalow.engine.playPause();
    next();
});
app.get('/api/player/stop', function (req, res, next) {
    fungalow.engine.stop();
    next();
});
app.get('/api/tracks/:id', function (req, res, next) {
    next();
});
app.get('/api/users/:username/playlists/:id', function (req, res, next) {
    next();
});



app.listen(process.env.PORT || 9261);