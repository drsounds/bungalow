var express = require('express');
var evh = require('express-vhost');
var execPath = process.env.PWD;

console.log(execPath);


var www = require('./www.js');
var api = require('./api.js');
var appfinder = require('./appfinder.js');
var server = express();
server.use(evh.vhost(server.enable('trust proxy')));
server.listen(80);
evh.register('play.bungalow.qi', www);
evh.register('api.bungalow.qi', api);
evh.register('appfinder.bungalow.qi', appfinder.server);

