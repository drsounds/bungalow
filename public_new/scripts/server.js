var express = require('express');
var app = express();
var path = require('path');
var request = require("request");
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
app.use(express.static(execPath + '/public_new'));



app.get('/api/apps/:id', function (req, res) {
    var path = require('path');
    var fs = require('fs');
    var appId = req.params.id;
    var appDir = 'public_new' + path.sep + 'apps' + path.sep + appId + path.sep;
    var manifestFilePath = appDir + path.sep + 'manifest.json';
    // check if app is already existing
    // check if directory exists
    var appName = 'notfound';
    var self = this;
    var appURL = '';
    if (fs.existsSync(appDir) && fs.existsSync(manifestFilePath)) {
        var manifest = JSON.parse(fs.readFileSync(manifestFilePath));
        appName = appId;
        appURL = '/public_new/apps/' + appName + '/index.html';
        callback({
            'appUrl': appURL,
            'appName': appName
        });
        res.next();
    }  else {
        // Check if app is available on App Finder
        $.getJSON('http://appfinder.aleros.webfactional.com/api/index.php?id=' + appId, function (app) {
            appURL = app.app_url;
            appName = app.id;
            /*var appFrame = document.createElement('iframe');
            appFrame.setAttribute('src', appURL + '?t=' + new Date().getTime());
            console.log('/apps/' + appName + '/index.html');
            appFrame.setAttribute('id', 'app_' + appId + '');
            appFrame.classList.add('sp-app');
            appFrame.setAttribute('nwdisable', 'nwdisable');
            appFrame.setAttribute('frameborder', '0');
            appFrame.setAttribute('width', "100%");
            appFrame.style = 'width:100%; height: 100%';
            $('#viewstack').append(appFrame);
            $(appFrame).css({'height': $('#viewstack').height()});
            self.app[appId] = appFrame;*/

            callback({
                'appUrl': appURL,
                'appName': appName
            });
            res.next();
        });
        return;
    }
});

app.get('/api/play', function (req, res) {
    var uri = req.query.uri;
    if (action === 'play') {

    }
});

app.listen(process.env.PORT || 9261);