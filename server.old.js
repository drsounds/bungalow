var express = require('express');
var execPath = process.env.PWD;
var cookieSession = require('cookie-session')
var app = express();
app.set('trust proxy', 1) // trust first proxy

app.use(cookieSession({
    name: 'session',
    keys: ['spotifyAccessToken']
}))
var bodyParser = require('body-parser');
app.use(bodyParser.urlencoded({extended :false}));
app.use(bodyParser.json());
var www = require('./www.js');
var api = require('./api.js');
var appfinder = require('./appfinder.js');
app.use('/api', api.server);
app.use('/apps', appfinder.server);
app.use('/', www.server);
app.listen(process.env.PORT || 9261);

