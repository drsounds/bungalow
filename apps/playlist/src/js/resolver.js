var models = require('@bungalow/models');
exports2.requireUri = function (uri) {
    return new Promise(function (resolve, fail) {
        var parts = uri.split(/\:/g);
        var id = parts[4];
        var username = parts[2];
        models.Playlist.fromUserId(username, id).load().then(function (playlist) {
            console.log(playlist);
            playlist.icon = 'music';
            
            console.log("Playlist resolved", playlist);
            resolve(playlist);
        });
    });
    
};