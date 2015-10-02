var models = require('@bungalow/models');
exports2.resolveUri = function (uri) {
    return new Promise(function (resolve, fail) {
       
        var id = uri.parameters[0];
    
        resolve({
            id: id,
            icon: 'hammer',
            name: id,
            uri: 'bungalow:doctrine:' + id
        });
    
    });
};