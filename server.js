var express = require('express');
var app = express();
var path = require('path');
var fs = require('fs');
var async = require('async');
var MusicService = require('./services/music/service/mockify/mockify.js');
var music = new MusicService();
var less = require('less');
var request = require('request');
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
app.use(express.static(__dirname + '/'));


app.get('/player/play', function (req, res) {
    var id = req.params.id;
    music.getAlbumTracks(id).then(function (artist) {
        var data = JSON.stringify(artist);
        res.json(artist);
    });
});

app.get('/', function (req, res) {

    var index = fs.readFileSync('./index.html');
    res.write(index);
    res.end();
});

app.get('/index.html', function (req, res) {

    var index = fs.readFileSync('./index.html');
    res.write(index);
    res.end();
});

app.get('/settings.json', function (req, res) {

    if (fs.existsSync(path)) {
        var settings = JSON.parse(fs.readFileSync('./settings.json'));
        res.json(settings);
    } else {
        var settings = {
            'bungalows': {},
            'apps': [],
            'theme': 'spotify09',
            'light': true,
            'primaryColor': '#FB8521'
        };
        fs.writeFileSync('./settings.json', settings);
        return settings;
    }
});

app.put('/settings.json', function (req, res) {
    var settings = JSON.stringify(req.json);
    fs.writeFileSync('settings.json', settings);

    var lightTheme = fs.readFileSync(process.env.PWD + '/themes/' + settings.theme + '/css/light.less', {'encoding': 'utf-8'});
    console.log(process.env.PWD + '/themes/' + settings.theme + '/css/style.less');
    lightTheme = lightTheme.replace(/\@primary-color/, settings.primaryColor);
    lightTheme = lightTheme.replace(/\@secondary-color/, settings.secondaryColor);
    lightTheme = lightTheme.replace("@islight", true ? '@light' : '@dark');
    lightTheme = lightTheme.replace("@isdark", !true ? '@light' : '@dark');

    var darkTheme = fs.readFileSync(process.env.PWD + '/themes/' + settings.theme + '/css/dark.less', {'encoding': 'utf-8'});
    console.log(process.env.PWD + '/themes/' + settings.theme + '/css/style.less');
    darkTheme = darkTheme.replace(/\@primary-color/, settings.primaryColor);
    darkTheme = darkTheme.replace(/\@secondary-color/, settings.secondaryColor);
    darkTheme = darkTheme.replace("@islight", true ? '@light' : '@dark');
    darkTheme = darkTheme.replace("@isdark", !true ? '@light' : '@dark');

    //alert(theme);
    less.render(lightTheme, {}, function (error, output) {
        console.log(error, output);
        //alert(output);
        fs.writeFileSync(process.env.PWD + '/themes/' + settings.theme + '/css/light.css', output.css);
    });

    less.render(darkTheme, {}, function (error, output) {
        console.log(error, output);
        //alert(output);
        fs.writeFileSync(process.env.PWD + '/themes/' + settings.theme + '/css/dark.css', output.css);
    });
    fs.writeFileSync(process.env.PWD + '/themes/' + settings.theme + '/css/main.css', '@import url("' + (settings.light ? 'light' : 'dark') + '.css")');


    var mainCSS = '@import url("' + settings.theme + '/css/style.css")';
    fs.writeFileSync(process.env.PWD + '/themes/main.css', mainCSS);

    fs.writeFileSync(process.env.PWD + '/themes/main.css', '@import url("http://127.0.0.1:9261/themes/' + settings.theme + '/css/main.css")');
    fs.writeFileSync(process.env.PWD + '/themes/light.css', '@import url("http://127.0.0.1:9261/themes/' + settings.theme + '/css/light.css")');
    fs.writeFileSync(process.env.PWD + '/themes/dark.css', '@import url("http://127.0.0.1:9261/themes/' + settings.theme + '/css/dark.css")');

})
/*
app.post('/api/login', function (req, res) {

});

app.get('/api/albums/:id', function (req, res) {
    var id = req.params.id;
    music.getAlbumById(id).then(function (artist) {
        var data = JSON.stringify(artist);
        res.json(artist);
    });
});


app.get('/api/albums/:id/tracks', function (req, res) {
    var id = req.params.id;
    music.getAlbumTracks(id).then(function (artist) {
        var data = JSON.stringify(artist);
        res.json(artist);
    });
});

app.get('/api/playlists/:id/tracks', function (req, res) {
    var id = req.params.id;
    music.getPlaylistTracks(id).then(function (artist) {
        var data = JSON.stringify(artist);
        res.json(artist);
    });
});

app.get('/api/search', function (req, res) {
    var q = req.query.q;
    var type = req.query.type;
    var offset = req.query.offset;
    var limit = req.query.limit;

    music.search(q, type, limit, offset).then(function (data) {

        res.json(data);
    });
});

app.get('/api/artists/:id', function (req, res) {
    var id = req.params.id;
    music.getArtistById(id).then(function (artist) {
       var data = JSON.stringify(artist);
        res.json(artist);
    });
});

app.get('/api/albums/:id', function (req, res) {
    var id = req.params.id;
    music.getAlbumById(id).then(function (artist) {
        var data = JSON.stringify(artist);
        res.json(data);
    });
});

app.get('/api/users/:username/playlists/:id', function (req, res) {
    var username = req.params.username;
    var id = req.params.id;
    music.getPlaylistById(username, id).then(function (playlist) {

        res.json(playlist);
    });
});

app.get('/api/users/:username/playlists', function (req, res) {
    var username = req.params.username;
    var id = req.params.id;
    music.getPlaylistsForUser(username).then(function (data) {
        res.json(data);
    });
});

var external_apps = {};
/*
app.get('/app/*', function (req, res) {

    var app_path = req.params[0].split('/');
    var appId = app_path[0];
    if (!(appId in external_apps)) {
        var address = app_path.slice(1).join('/');
        var appDir = './app' + path.sep + appId + path.sep;
        if (!fs.existsSync(appDir)) {
            appDir = '~/Spotify' + path.sep + appId + path.sep;
        }

        var manifestFilePath = appDir + path.sep + 'manifest.json';
        // check if app is already existing
        // check if directory exists
        var self = this;
        var appURL = '';

        if (fs.existsSync(appDir) && fs.existsSync(manifestFilePath)) {
            var manifest = JSON.parse(fs.readFileSync(manifestFilePath));
            appName = appId;
            appURL = '/app/' + appName + '/';
            var file = appDir + app_path.slice(1).join('/');
            var data = fs.readFileSync(file);

            res.write(data);
            res.end();
        } else {
            // Check if app is available on App Finder
            request('http://appfinder.aleros.webfactional.com/api/index.php?id=' + appId, function (error, response, body) {
                var app = JSON.parse(body);
                external_apps[appId] = app.app_url;
                var url = external_apps[appId] + app_path.slice(1).join('/');
                request(url, function (error, response, body) {
                    res.write(body);
                    res.end();
                })

            });
        }
    } else {
        request(external_apps[appId] + '/' + app_path.slice(1).join('/'), function (error, response, body) {
            if (error) {
                var notfound = fs.readFileSync('./notfound/index.html');
                res.write(notfound);
                res.end();
                return;
            }
            res.write(body);
            res.end();
        })
    }
});*/
/*
app.get('/api/users/:username', function (req, res) {
    var username = req.params.username;
    var id = req.params.id;
    music.getUserById(username).then(function (data) {
        res.json(data);
    });
});


app.get('/api/users/:username/playlists/:id/tracks', function (req, res) {
    var username = req.params.username;
    var id = req.params.id;
    music.getPlaylistTracks(username, id).then(function (playlist) {
        var data = JSON.stringify(playlist);
        res.json(data);
    });
});

app.get('/chrome/*', function (req, res) {
    var app_path = req.params[0].split('/');
    var file = fs.readFileSync('./' + app_path.join('/'));
    res.write(file);
    res.end();
});*/
app.get('/api/music/*', function (req, res) {
    music.request("GET", req.params[0]).then(function (result) {

        res.json(result);
    });
});

app.listen(process.env.PORT || 9261);
