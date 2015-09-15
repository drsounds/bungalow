 require(['$api/models'], function (models) {
    exports = function (uri) {
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
})