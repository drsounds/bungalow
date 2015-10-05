var request = require('request');
var utils = require('./utils.js');
var path = require('path');
var fs = require('fs');
var external_apps = {};

var express = require('express');
var evh = require('express-vhost');

var appInventory = {};

var hostile = require('hostile');

function AppFinder () {
    this.apps = {};
    this.installedApps = [];

    
    


    var evh = require('express-vhost');
    var server = utils.createApp();
    this.server = server;
    this.api = utils.createApp();
    this.evh = evh;


    
}

AppFinder.prototype.registerApp = function (appDir) {
    

    var manifestFilePath = appDir + path.sep + 'manifest.json';
    // check if app is already existing
    // check if directory exists
    var appURL = '';
    console.log(appDir);

    if (fs.existsSync(appDir) && fs.existsSync(manifestFilePath)) {
        console.log(manifestFilePath);
        var manifest = JSON.parse(fs.readFileSync(manifestFilePath));
        appName = manifest.BundleIdentifier;
        appURL = __dirname + path.sep + 'apps/' + appName + '/';
        console.log(manifest);
        this.installedApps.push(manifest);
        /*hostile.remove('127.0.0.1', appName + '.bungalowapp.com', function (err) {
          if (err) {
            console.error(err)
          } else {
            console.log('set /etc/hosts successfully!')
          }
        })
        console.log("Registering app");
        hostile.set('127.0.0.1', appName + '.bungalowapp.com', function (err) {
          if (err) {
            
            
          } else {
            evh.register(appName + '.bungalowapp.com', www);
            console.log('set /etc/hosts successfully!')
          }
        })*/
    } 
}

AppFinder.prototype.registerApps = function () {

    var appDir = __dirname + path.sep + 'apps';
    this.registerDirectory(appDir);
    var ownApps = '~' + path.sep + 'Bungalow';
    if (fs.existsSync(ownApps)) {
        this.registerDirectory(ownApps);
    }
}

AppFinder.prototype.registerDirectory = function (appDir) {
    var directories = fs.readdirSync(appDir).filter(function(file) {
        return fs.statSync(path.join(appDir, file)).isDirectory();
    });
    for(var i = 0; i < directories.length; i++) {
        var directory = directories[i];
        this.registerApp(appDir + path.sep + directory);
    }
}

AppFinder.prototype.listen = function () {
    var self = this;
    this.server.get('/api/v1/apps', function (req, res) {
        console.log(self.installedApps);
        res.json(self.installedApps);
        res.end();
    });

    this.server.get('/*', function (req, res) {
        console.log("A");
        var host = req.host;
         var app_path = req.params[0].split('/');
        var appId = app_path[0];
        console.log("A");
        if (!(appId in external_apps)) {
            var address = app_path.slice(1).join('/');
            var appDir = __dirname + path.sep + path.sep + 'apps' + path.sep + appId + path.sep;
            if (!fs.existsSync(appDir)) {
                appDir = '~/Bungalow' + path.sep + appId + path.sep;
            }

            var manifestFilePath = appDir + path.sep + 'manifest.json';
            // check if app is already existing
            // check if directory exists
            
            var appURL = '';
                
            if (fs.existsSync(appDir) && fs.existsSync(manifestFilePath)) {
                var manifest = JSON.parse(fs.readFileSync(manifestFilePath));
                appName = appId;
                appURL = __dirname + path.sep + 'apps/' + appName + '/';
                var file = appDir + app_path.slice(1).join('/');

                self.apps[appName] = manifest;

                var data = fs.readFileSync(file);

                res.write(data);
                res.end();
            } else {
                // Check if app is available on App Finder
                if (false)
                request('http://appfinder.aleros.webfactional.com/api/index.php?id=' + appId, function (error, response, body) {
                    var app = JSON.parse(body);
                    external_apps[appId] = app.app_url;
                    var url = external_apps[appId] + app_path.slice(1).join('/');
                    request(url, function (error, response, body) {
                        if (!body) body = "";
                        res.write(body);
                        res.end();
                    })

                });
                var notfound = fs.readFileSync(__dirname + path.sep + 'apps/notfound/index.html');
                res.write(notfound);
                res.end();
              
            }
        } else {
            request(external_apps[appId] + '/' + app_path.slice(1).join('/'), function (error, response, body) {
                if (error) {
                    var notfound = fs.readFileSync(__dirname + path.sep + 'apps/notfound/index.html');
                    res.write(notfound);
                    res.end();
                    return;
                }
                res.write(body);
                res.end();
            })
        }
    });
}

var appFinder = new AppFinder();

appFinder.registerApps();
appFinder.listen();

module.exports = appFinder;
