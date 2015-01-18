var theme = 'spotify';
var subtheme = 'concept';

var link = document.createElement('link');
link.setAttribute('rel', 'stylesheet');
link.setAttribute('type', 'text/css');
link.setAttribute('href', 'app://mercy/public/themes/' + theme + '/' + subtheme + '/css/style.css');
document.head.appendChild(link);