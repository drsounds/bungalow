define(function () {
    var exports = {};
    var Cosmos = function () {

    }


    Cosmos.request = function (method, url, params, data) {
        return new Promise(function (resolve, fail) {
            var xhr = new XMLHttpRequest();

            xhr.onreadystatechange = function () {
                if (xhr.readyState == 4 && xhr.status == 200) {
                    var data = xhr.response;
                    resolve(data);
                }
            };
            xhr.responseType = 'json';
            xhr.open(method, 'http://localhost:9261/api' + url, true);
            xhr.send(data);
        });
    };
    exports = Cosmos;
    return exports;
});
