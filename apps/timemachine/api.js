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
function getBungalowApi(version) {
	return ({
		require: function(path) {
			var c = new XMLHttpRequest();
			try {
			c.open("GET", path+".js", false);
			c.send(null);
			} catch(e) {
				document.write(e.message);
			}
			var pc = "(new (function() { var exports = {};\n" + c.responseText +"\nthis._exports = exports;})())._exports";
			console.log(pc);
			return eval(pc);
			
		},
		core: {}
	});
}
function getSpotifyApi(version) {
	return getBungalowApi(version);
}