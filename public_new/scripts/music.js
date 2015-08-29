var SpotifyResolver = function () {

};

SpotifyResolver.prototype.getAlbum = function (id) {
    var promise = new Promise(function (resolve, fail) {

        $.getJSON('https://api.spotify.com/v1/albums/' + id, function (data) {
            resolve(data);
        });
    });
    return promise;

}

SpotifyResolver.prototype.login = function () {
    var promise = new Promise(function (resolve, fail) {

    });
    return promise;
};

SpotifyResolver.prototype.search = function (query, type, page, limit) {
    var promise = new Promise(function (resolve, fail) {
        $.getJSON('https://api.spotify.com/v1/search?q=' + query + '&type=' + type + '&offset=' + page + '&limit=' + limit, function (data) {
            console.log(data.tracks);
            resolve(data.tracks.items);
        });
    });
    return promise;
}

SpotifyResolver.prototype.getArtist = function (id) {
    var promise = new Promise(function (resolve, fail) {
         $.getJSON('https://api.spotify.com/v1/artists/' + id, function (data) {
            resolve(data);
        });
    });
    return promise;
};

SpotifyResolver.prototype.getAlbumsByArtist = function (id) {
    var promise = new Promise(function (resolve, fail) {
         $.getJSON('https://api.spotify.com/v1/artists/' + id + '/albums', function (data) {
            resolve(data.items);
        });
    });
    return promise;
};

SpotifyResolver.prototype.getAlbumTracks = function (id) {
    var promise = new Promise(function (resolve, fail) {
        $.getJSON('https://api.spotify.com/v1/albums/' + id + '/tracks', function (data) {
            resolve(data.items);
        });
    });
    return promise;
};

SpotifyResolver.prototype.getPlaylist = function (user, id) {
    var promise = new Promise(function (resolve, fail) {

    });
    return promise;
};

SpotifyResolver.prototype.getUser = function (user) {
    var promise = new Promise(function (resolve, fail) {

    });
    return promise;
};

