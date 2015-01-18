var theme = 'spotify';
var subtheme = '2009';

var link = document.createElement('link');
link.setAttribute('rel', 'stylesheet');
link.setAttribute('type', 'text/css');
link.setAttribute('href', 'app://mercy/public/themes/' + theme + '/' + subtheme + '/css/style.css');
document.head.appendChild(link);