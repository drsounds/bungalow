(new (
	function() { 
var exports = {};





exports.File = function() {



};

/***

	Web class, fetch data from web

	***/

exports.Web = function() {

};



exports.Event = function (type, callback) {

	this.type = type;

	this.callback = callback;

};

exports.EVENT = {

	ARGUMENTSCHANGED : 0

}; 

exports.Observable = function () {

	var events = [];

	this.observe = function(evt) {

		events.push(evt);

	};

	this.notify = function (type, data) {

		for(var i = 0; i < this.events.length; i++) {

			if(events[i].type == type) {

				events[i].callback(data);

			}

		}

	};

};

exports.Application.prototype = new Observable();

exports.Application.prototype.constructor = function () {

	

};



exports.application = new exports.Application();
this._exports = exports;}))()._exports