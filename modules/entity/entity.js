var Entity = function (entity) {
    this.entity = entity;
}

Entity.request = function (method, type, id, data) {
    return new Promise(function (resolve, reject) {
        resolve({
            type: type,
            name: 'An object',
            id: id,
            uri: 'urn:' + type + ':' + id,
            authors: [],
            album: {
                id: '123',
                name: 'Album',
                uri: 'urn:album:123'
            }
        });
    });
};