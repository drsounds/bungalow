require([], function () {
    var Cosmos = {

    };


    Cosmos.request = function (method, url, params, data) {
        return new Promise(function (resolve, fail) {
            var xhr = new XMLHttpRequest();

            xhr.onreadystatechange = function () {
                if (xhr.readyState == 4 && xhr.status == 200) {
                    var data = xhr.response;
                    console.log("Resolved url " + url);
                    resolve(data);
                }
            };
            xhr.responseType = 'json';
            console.log(method, url);
            xhr.open(method, 'http://localhost:9261/api' + url, true);
            xhr.send(data);
        });
    };
    exports = Cosmos;
});
