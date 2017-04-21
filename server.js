var express = require('express');
var evh = require('express-vhost');
var execPath = process.env.PWD;


var app = express();
var www = require('./www.js');
var api = require('./api.js');
var appfinder = require('./appfinder.js');
app.use('/api', api.server);
app.use('/apps', appfinder.server);
app.use('/', www.server);
app.listen(process.env.PORT || 9261);

