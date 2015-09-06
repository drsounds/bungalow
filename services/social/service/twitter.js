var request = require('request');
var fs = require('fs');
var API_KEYS = JSON.parse(fs.readFileSync('./key.json'));
var Twitter = function () {

}

Twitter.prototype.request = function (method, url) {

}