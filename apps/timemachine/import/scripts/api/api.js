var ___app = !___app ? false : ___app; 
var ___subScribeTitle = "";
function __appStateSubscribed(_args) {
	var bl = getBungalowApi(1);
	var models = bl.require("bl://import/scripts/api/models");
	var args = _args.split(":");
	var args = _args.split(":");
	var title = models.application.notify(models.EVENT.SUBSCRIBED, args.slice(3, 4));
	return (title);
}
function __argumentsChanged(_args) {
	
//	document.write("A");
	var bl = getBungalowApi(1);
	var models = bl.require("bl://import/scripts/api/models");
	var args = _args.split(":");
//	document.write(args);
	console.log(models.application);
	___app.arguments = args.slice(3, 4);
	models.application.notify(models.EVENT.ARGUMENTSCHANGED, args.slice(3, 4));
//	document.write("F");

	return null;
}
var __objects = {};
var __currentObj = {
	INSTANCE: {}
};
function pausecomp(millis)
 {
  var date = new Date();
  var curDate = null;
  do { curDate = new Date(); }
  while(curDate-date < millis);
}
function getBungalowApi(version) {
	return ({
		require: function(path) {
			var script = document.createElement("iframe");
			script.setAttribute("style", "display:none");
			script.setAttribute("src", path.replace("sp://", "") + ".js");
			document.getElementsByTagName("body")[0].appendChild(script);
			var serial = Math.random()*10000000;
			var data = null;
			var i = 0;
			pausecomp(1000);
			data = script.contentWindow.document;
			alert(data.body.children.length);
			
			var code = data.getElementsByTagName("pre")[0].innerHTML;
			var pc = "(new (function() { var exports = {};\n" + code  +"\nthis._exports = exports;})())._exports";

			__currentObj = pc;
			
			__objects[serial]=( data);
			
			
			
			return __objects[serial];

		
		},
		core: {}
	});
}
function getSpotifyApi(version) {
	return getBungalowApi(version);
}