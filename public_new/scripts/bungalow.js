var Bungalow = function () {
    this.music = new SpotifyResolver();
    if (!window.indexedDB) {
        window.alert("Your browser doesn't support a stable version of IndexedDB. Such and such feature will not be available.");
    }
};

var REALM = 'bungalow';

Bungalow.prototype.navigate = function (uri, section, noHistory) {
    if (uri.indexOf('#') == 0) {
        uri = 'bungalow:hashtag:' + uri.substr(1);
    }
    if (uri.indexOf('bungalow:') != 0) {
        uri = 'bungalow:search:' + uri;
    }

    var fragments = uri.split(/#/g);
    var parts = fragments[0].split(/\:/g);
    var section = fragments[1];
    if (!section) {
        section = "overview";
    }
    console.log(arguments, uri, section ,noHistory);
    var view = parts[1];
    var arguments = parts.slice(2);

    if (uri.match(/^bungalow:start/)) {
        this.loadView('start', arguments, section);
    } else if (uri.match(/^bungalow:foot/)) {
        this.loadView('foot', arguments, section);
    } else if (uri.match(/^bungalow:user:(.*)/)) {
        this.loadView('user', arguments, section);
    } else if (uri.match(/^bungalow:job:(.*)/)) {
        this.loadView('job', arguments, section);
    } else if (uri.match(/^bungalow:artist:(.*)/)) {
        this.loadView('artist', arguments, section);
    } else {
        
        this.loadView(view, arguments, section);

    }
    $('.menu li').removeClass('active');
    //alert(uri);
    $('.menu li[data-uri="' + uri + '"]').addClass('active');
    var q = '.menu li[data-uri="' + uri + "']";
    console.log($(q)[0]);
    var viewUrl = uri.substr((REALM + ':').length - 1).replace(':', '/');
    if (!noHistory) {
        window.history.pushState({'action': 'navigate', 'uri': uri}, 'View', viewUrl.replace(/\:/g, '/'));
    }
}

window.addEventListener('message', function (event) {
    console.log(event.data);
    if ('uri' in event.data) {
        if (event.data.action === 'navigate') {
            if (event.data.uri.match(/#(.*)/g)) {
                window.location.hash = event.data.uri;
            } else {
                bungalow.navigate(event.data.uri, '', false);
            }
        } 

    }
    if (event.data.action === 'cosmos') {
        var method = event.data.method;
        var uri = event.data.uri;
        var payload = event.data.payload;
        var requestId = event.data.requestId;
        console.log("URI", uri);
        console.log("Request id", requestId);
        if (uri.match(/^cosmos\:\/\/v1\/search\//)) {
            payload.page = payload.page ? payload.page : 0;
            payload.limit = payload.limit ? payload.limit : 25;
            console.log(payload);
            bungalow.music.search(payload.q, payload.type, payload.page + 1, payload.limit).then(function (data) {
                alert(data);
                console.log("DATA", data);
                event.source.postMessage({
                    'action': 'cosmosReply',
                    'requestId': requestId,
                    'object': data
                }, '*');
            });
        } else if (uri.match(/^cosmos\:\/\/v1\/album\/(.*)\/tracks/)) {
            alert(uri);
            var parts = uri.split(/^cosmos\:\/\/v1\/album\/(.*)\/tracks/);
    
            bungalow.music.getAlbumTracks(parts[1]).then(function (data) {
                console.log("DATA", data);
                event.source.postMessage({
                    'action': 'cosmosReply',
                    'requestId': requestId,
                    'object': data
                }, '*');
            });

        } else if (uri.match(/^cosmos\:\/\/v1\/album\/(.*)/)) {
            var parts = uri.split(/^cosmos\:\/\/v1\/album\/(.*)/);
            console.log(parts);
            bungalow.music.getAlbum(parts[1]).then(function (data) {
                console.log("DATA", data);
                event.source.postMessage({
                    'action': 'cosmosReply',
                    'requestId': requestId,
                    'object': data
                }, '*');
            });
        } else if (uri.match(/^cosmos\:\/\/v1\/artist\/(.*)\/albums/)) {
            var parts = uri.split(/^cosmos\:\/\/v1\/artist\/(.*)\/albums/);

            bungalow.music.getAlbumsByArtist(parts[0]).then(function (data) {
                event.source.postMessage({
                    'action': 'cosmosReply',
                    'requestId': requestId,
                    'object': data
                }, '*');
            });

        } else if (uri.match(/^cosmos\:\/\/v1\/artist\/(.*)/)) {
            var parts = uri.split(/^cosmos\:\/\/v1\/artist\/(.*)/);

            bungalow.music.getArtist(parts[0]).then(function (data) {
                event.source.postMessage({
                    'action': 'cosmosReply',
                    'requestId': requestId,
                    'object': data
                }, '*');
            });

        } 
    }
});

window.addEventListener("popstate", function(e) {
    var pathname = location.pathname;
    var uri = REALM + pathname.replace(/\//g, ':');
    console.log(uri);
    bungalow.navigate(uri, '', true);
});

window.onload = function () {
    var path = window.location.pathname;
    console.log(path);
    if (path == '/') {
        path = "/start";
    }
    var uri = REALM + path.replace(/\//g, ':');

    bungalow.navigate(uri);
}

Bungalow.prototype.search = function (event) {
    event.preventDefault();
    var $search = $('#search');
    console.log($search[0]);
    var q = $search.val();
    console.log(q);
    this.navigate(q, '', false);
    return false;
}

Bungalow.prototype.loadView = function (viewId, parameters, section) {
    var view = document.querySelector('.sp-app#view_' + viewId);
    if (view) {
        $('.sp-app').hide();
        $(view).show();
        view.contentWindow.postMessage({'action': 'navigate', 'arguments': parameters}, '*');
            //view.contentWindow.location.hash = section;
            //console.log(view.contentWindow.location.hash);
    } else {
        $('.sp-app').hide();
        var view = document.createElement('iframe');
        $(view).addClass('sp-app');
        view.setAttribute('id', 'view_' + viewId);

        view.setAttribute("frameborder", "0");
        if (viewId === 'cosmos') {
            view.setAttribute('src', 'http://localhost:9261/');
        } else {
            view.setAttribute('src', 'app/' + viewId + '/index.html');

        }
        view.onload = function () {
            view.contentWindow.postMessage({'action': 'navigate', 'arguments': parameters}, '*');
            view.contentWindow.postMessage({'action': 'navigate', 'uri': '#' + section}, '*');
        }
        $('#viewstack').append(view);
        this.resizeApps();

    }
}

window.alert = function (msg) {
    $('info-bar').show();
    console.log(msg);
    $('info-bar').html('<description>' + msg + '</description>');
}

Bungalow.prototype.resizeApps = function () {
     $('iframe').each(function (i, element) {
        //alert($(this).parent().width());
        $(this).attr('width', $(this).parent().width());
        $(this).attr('height', $(this).parent().height());
    });
}
var bungalow = new Bungalow();


$(window).resize(function () {
    bungalow.resizeApps();
})
$(document).on('click', '*[data-uri]', function (event) {
    var target = event.target;
    bungalow.navigate(target.dataset['uri']);
});
