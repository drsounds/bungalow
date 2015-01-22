var express = require('express');
var app = express();
var path = require('path');
var execPath = process.env.PWD;

    execPath = process.env.PWD;

console.log(execPath);
var allowCrossDomain = function(req, res, next) {
    res.header('Access-Control-Allow-Origin', '*');
    res.header('Access-Control-Allow-Methods', 'GET,PUT,POST,DELETE');
    res.header('Access-Control-Allow-Headers', 'Content-Type');

    next();
}
app.use(allowCrossDomain);
app.use(express.static(execPath + '/public'));

app.listen(process.env.PORT || 9261);