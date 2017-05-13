var express = require('express');
var execPath = process.env.PWD;
var fs = require('fs');
var cookieSession = require('cookie-session');
var api = require('./api.js');
var app = express();
var bodyParser = require('body-parser');
  app.use(bodyParser());


app.use(express.static(__dirname + '/public/'));
app.use('/api', api.server);
app.get('/callback.html', function (req, res) {
    var index = fs.readFileSync(__dirname + '/public/callback.html');
    res.write(index);
    res.end();
});
app.get('/*', function (req, res) {

    var index = fs.readFileSync(__dirname + '/public/index.html');
    res.write(index);
    res.end();
});
app.listen(process.env.PORT || 9261);

