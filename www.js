var utils = require('./utils.js');
var app = utils.createApp();
var express = require('express');
var fs = require('fs');
app.use(express.static(__dirname + '/public'));
app.get('/', function (req, res) {

    var index = fs.readFileSync(__dirname + '/public/index.html');
    res.write(index);
    res.end();
});

app.get('/*', function (req, res) {

    var index = fs.readFileSync(__dirname + '/public/index.html');
    res.write(index);
    res.end();
});



app.listen(process.env.PORT || 9261);

module.exports = app;