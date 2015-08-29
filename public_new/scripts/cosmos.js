var Cosmos = function () {
    this.requests = {'b': 'c'};
}
if (!('cosmosRequests' in window))
window.cosmosRequests = {};

Cosmos.prototype.request = function (method, uri, payload) {
    var self = this;
    var requestId = '' + Math.random() * 1000000;
    var promise = new Promise(function (resolve, fail) {

        window.parent.postMessage({
            'action': 'cosmos',
            'uri': uri,
            'payload': payload,
            'requestId': requestId
        }, '*');
        window.cosmosRequests[requestId] = {'resolve': resolve, 'fail': fail};
        
    });
    return promise;
};

var cosmos = new Cosmos();
window.addEventListener('message', function (event) {
    if (event.data.action == 'cosmosReply') {
        window.cosmosRequests[event.data.requestId].resolve(event.data.object);
    }
});