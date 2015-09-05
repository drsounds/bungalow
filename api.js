var path = require('path');
var fs = require('fs');
var async = require('async');
var MusicService = require('./services/music/service/spotify/spotify.js');
var music = new MusicService();
var less = require('less');
var request = require('request');
var url = require('url');

var utils = require('./utils.js');
var app = utils.createApp();

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

app.get('/services/:id/authenticate', function (req, res) {
    console.log("Got authenticate request");
    music.authenticate(req.query.code).then(function (success) {
        console.log("success");
        res.statusCode = 200;
        res.send('success')
        res.end();
    }, function (error) {
        console.log(error);
        res.statusCode = 500;
        res.end(error);
    });

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
        fs.writeFileSync(process.env.PWD + '/themes/' + settings.theme + '/css/main.scss', output.css);
    });
    fs.writeFileSync(process.env.PWD + '/themes/' + settings.theme + '/css/main.scss', '@import url("' + (settings.light ? 'light' : 'dark') + '.css")');


    var mainCSS = '@import url("' + settings.theme + '/css/style.css")';
    fs.writeFileSync(process.env.PWD + '/themes/main.scss', mainCSS);

    fs.writeFileSync(process.env.PWD + '/themes/main.scss', '@import url("http://127.0.0.1:9261/themes/' + settings.theme + '/css/main.scss")');
    fs.writeFileSync(process.env.PWD + '/themes/light.css', '@import url("http://127.0.0.1:9261/themes/' + settings.theme + '/css/light.css")');
    fs.writeFileSync(process.env.PWD + '/themes/main.scss', '@import url("http://127.0.0.1:9261/themes/' + settings.theme + '/css/main.scss")');

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
*/

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
app.get('/music/*', function (req, res) {
    console.log("A");
    console.log(music);
    music.request("GET", req.params[0], req.query).then(function (result) {

        res.json(result);
    }, function (reject) {
        res.json(reject);
    });
});

app.get('/music/*', function (req, res) {
    console.log("A");
    console.log(music);
    music.request("GET", req.params[0], req.query).then(function (result) {

        res.json(result);
    }, function (reject) {
        res.json(reject);
    });
});

app.get('/player/play', function (req, res) {
    var id = req.params.id;
    music.getAlbumTracks(id).then(function (artist) {
        var data = JSON.stringify(artist);
        res.json(artist);
    });
});


module.exports = app;