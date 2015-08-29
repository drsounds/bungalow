require([], function () {
    var Cosmos = {

    };


    Cosmos.request = function (method, url, params, data) {
        return new Promise(function (resolve, fail) {
            var xhr = new XMLHttpRequest();
            console.log("Sending request");
            xhr.onreadystatechange = function () {
                if (xhr.readyState == 4 && xhr.status == 200) {
                    var data = xhr.response;
                    console.log("Resolved url " + url);
                    resolve(data);
                }
            };
            xhr.responseType = 'json';
            console.log(method, url);
            xhr.open(method, 'http://api.bungalow.qi' + url, true);
            xhr.send(data);
            console.log(data);
        });
    };
    exports = Cosmos;
});
