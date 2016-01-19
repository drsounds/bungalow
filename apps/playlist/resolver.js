require(['$api/models'], function (models) {
    exports = function (uri) {
        return new Promise(function (resolve, fail) {
            var parts = uri.split(/\:/g);
            var id = parts[4];
            var username = parts[2];
            models.Playlist.fromUserId(username, id).load().then(function (playlist) {
                console.log(playlist);
                playlist.icon = 'music';
				playlist.allowDrop = true;
				console.log("Playlist resolved", playlist);
                resolve(playlist);
            });
        });
    }
})