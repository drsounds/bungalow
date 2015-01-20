
var BUNGALOW_THEME = 'bungalow';


parent.postMessage({'action': 'getConfig'}, '*');

window.onmessage = function (event) {
	if (event.data.action === 'gotConfig') {
		var settings = event.data.config;
		BUNGALOW_THEME = settings.theme;

		var link = document.createElement('link');
		link.setAttribute('rel', 'stylesheet');
		link.setAttribute('type', 'text/css');
		link.setAttribute('href', 'http://127.0.0.1:9261/themes/' + BUNGALOW_THEME + '/css/style.css');
		
		document.head.appendChild(link);
	}

};