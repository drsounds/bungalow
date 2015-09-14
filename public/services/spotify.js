require([], function () {
	var Service = function () {
		this.observers = [];
	}
	
	Service.prototype.addEventListener = function (evt, callback) {
		this.observers[evt] = callback;
	}
	
	Servie.prototype.dispatchEvent = function (event) {
		this.observers[event.name].call(this, event);
	}
	
	Service.prototype.play = function (uri) {
		var xmlHttp = new XMLHttpRequest();
	}
	
	exports.Service = Service;
});