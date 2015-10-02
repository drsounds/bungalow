var models = require('@bungalow/models');
exports2.resolveUri = function (uri) {
    return new Promise(function (resolve, fail) {
        var parts = uri.split(/\:/g);
        var id = parts[2];
    
        resolve({
            id: id,
            icon: 'calendar-o',
            name: id,
            uri: 'bungalow:year:' + id
        });
    
    });
}
