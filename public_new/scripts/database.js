var db;
var request = indexedDB.open("Bungalow_Footcare", 1);
request.onerror = function(event) {
  alert("Why didn't you allow my web app to use IndexedDB?!");
};
request.onsuccess = function(event) {
  db = event.target.result;

    db.onerror = function(event) {
      // Generic error handler for all errors targeted at this database's
      // requests!
      alert("Database error: " + event.target.errorCode);
    };

    var transaction = db.transaction(['footcares']);
    var objectStore = transaction.objectStore("footcares");
    var index = objectStore.index('type');
    var request = index.openCursor(IDBKeyRange.only(['type']));
    var request = index.get(IDBKeyRange.only(['footcare']));
    request.onsuccess = function (event) {
        var cursor = event.target.result;
        if (cursor) {

        }
    };


};


// This event is only implemented in recent browsers
request.onupgradeneeded = function(event) { 
  var db = event.target.result;
  // Create an objectStore for this database
  var objectStore = db.createObjectStore("nodes", { autoIncrement: true });
  objectStore.createIndex('type', 'type', {unique:false});
  objectStore.createIndex('time', 'time', {unique:false});
  
};  

function issue() {
    var transaction = db.transaction(['nodes'], 'readwrite');
    var objectStore = transaction.objectStore('nodes');
    objectStore.add({
        '_id': Math.random(),
        'type': 'footcare',
        'time': new Date()
    });
}
