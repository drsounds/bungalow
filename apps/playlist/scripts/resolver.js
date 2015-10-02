var models = require('@bungalow/models');
exports2.resolveUri = function (uri) {
    return new Promise(function (resolve, fail) {
        
        var id = uri.parameters[3];
        var username = uri.parameters[1];
        models.Playlist.fromUserId(username, id).load().then(function (playlist) {
            console.log(playlist);
            playlist.icon = 'music';
			
			console.log("Playlist resolved", playlist);
            resolve(playlist);
        });
    });

};