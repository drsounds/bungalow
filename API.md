# Writing apps and bungalows for Bungalow music Client

Bungalow music client has a Spotify Apps like infrastructure. It has two main types of applications, Apps and Bungalows. Apps are what they are in Spotify apps, apps that can be installed and accessed from the sidebar.

Bungalows howevers are mini apps that can placed on different views in the Spotify client. They consists of an iframe embedded in some views, that can be configured to show a certain 'bungalow' at the user choice.

# Writing an app or bungalow

The base is the same. Currently all bungalows and apps are stored in the <APP directory>/apps/ directory. The base is same for both formats.

It has two main files

* Manifest.json - The manifest file

For a bungalow or app

    {
        "BundleType": "Bungalow",
        "BundleName": {
        	"en": "Sample application"
    	},
    	"socket": "album-view",
    	"BundleIdentifier": "testbungalow"
	}

* Socket - defines what socket to override. Currently no sockets are implemented now.
* BundleName - Like app name in Spotify Apps
* BundleIdentifier - please be same as the folder name.

Support for converting and running apps based the discountinued Spotify Apps API will come in the future. 

# Overview of 'Rovi' sample app

The Rovi sample application is demonstrating the intended capability of this API.


# The index.html file

The index.html file is the base point for a bungalow app. It must have the following structure. The Bungalow framework has it API incorporated in two files, models.js (for models) and views.js (for views).

    <!DOCTYPE html>
    <html>
        <head>
            <title>Rovio app</title>
            <base href="http://rovio.aleros.webfactional.com/">
            <link rel="stylesheet" href="http://127.0.0.1:9261/public/bower_components/font-awesome/css/font-awesome.css">
            <script src="http://127.0.0.1:9261/bower_components/jquery/dist/jquery.js"></script>
            <script src="http://127.0.0.1:9261/apps/api/models.js"></script>
            <script src="http://127.0.0.1:9261/apps/api/views.js"></script>
            <link rel="stylesheet" href="http://127.0.0.1:9261/themes/main.css">
        </head>
        <body>
            <div class="sp-tabbar"> <!-- Currently only decoration now -->
                <div class="sp-tabbar-tab sp-tabbar-tab-active">Rovio</div>
            </div>
            <div style="height: 23px"></div>
            <div class="sp-container">
                <h1>Rovio sample app</h1>
                <p>This is a sample app for Bungalow that demonstrates the capabilities of the Bungalow app framework</p>
                <h2>Current first argument is <span id="argument"></span></h2>
                <h3>Playlist generation demonstration</h3>
                <p>Rovio will generate a playlist according to the app argument. Type spotify:rovio:&lt;ambient&gt; and rovio will create an ambient playlist</p>

            </div>  
                <div id="playlist"></div>

            <!-- Main script file -->
            <script src="scripts/main.js"></script>
        </body>
    </html>

# The JavaScript file

The app logic is done in this JavaScript file. Note the extensive use of window.onmessage. Apps cannot natively access the Spotify function, instead it is doing the calls through
postMessage communication between the host app and the bungalow.

    /***
     * Tutorial script for Rovio sample app.
     * The central mechanism of the Bungalow app framework is the communication between the host bungalow (the music player) and the client app (this).
     **/

    window.onmessage = function (event) {
        // Like how it was in Spotify Apps API, there is a navigation handler.
        // Listen to navigation by doing this
        if (event.data.action === 'navigate') {
            // Here we will perform argumentschanged operations.

            var arguments = event.data.arguments;
                // Like how it was in Spotify, we get the URL arguments here.

            // For demonstration purpose, we will show the current argument here
            $('#argument').html(event.data.arguments[0]);
            
            // In this example, we will threat the first argument as a genre search query in Spotify, to demonstrate the playlist facility.

            var query = 'genre:' + event.data.arguments[0];

            
            Search.search(query, 10, 0, 'track', function (tracks) {

                // Create a context view for the search result
                var contextView = new ContextView({
                    'uri': 'spotify:search:' + query,
                    'tracks': tracks
                }, {headers:true, fields: ['title', 'artist', 'duration', 'popularity', 'album']});
                $('#playlist').html("");
                $('#playlist').append(contextView.node);
            });
        }   
    }

# Sample app #2: Time Machine

![Screenshot](https://www.dropbox.com/s/aggxg4r4q5xwpox/timemachine.png?dl=1 "Screenshot")

Time Machine was an app I wanted to release when Spotify had their App Finder, but I never managed to get it there, but hope to share it with my own platform instead.


# The index.html file

The index.html file is the base point for a bungalow app. It must have the following structure. The Bungalow framework has it API incorporated in two files, models.js (for models) and views.js (for views).

    <!DOCTYPE html>
    <html>
        <head>
            <title>Time Machine</title>
            <base href="http://rovio.aleros.webfactional.com/timemachine/">
            <link rel="stylesheet" href="http://127.0.0.1:9261/public/bower_components/font-awesome/css/font-awesome.css">
            <script src="http://127.0.0.1:9261/bower_components/jquery/dist/jquery.js"></script>
            <script src="http://127.0.0.1:9261/apps/api/models.js"></script>
            <script src="http://127.0.0.1:9261/apps/api/views.js"></script>
            <link rel="stylesheet" href="http://127.0.0.1:9261/themes/main.css">
        </head>
        <body>
            <style>
            li {
                float: left;
                min-width: 10pt;
                margin-left: 15pt;
                margin-right: 15pt;
                text-align: center;
            }
            a.active {
                color: #b6e1fd;
            }
            </style>
            <div class="sp-tabbar">
                <div class="sp-tabbar-tab sp-tabbar-tab-active">Time Machine</div>
            </div>
            <div style="height: 23px"></div>
            <div class="sp-container">
                <table>
                    <tr>
                        <td valign="top" style="padding: 5pt">
                            <h1>Time Machine</h1>
                            <p>Select a decade</p>
                            <ul style="list-style-type: none">
                                <li><a data-year="1950-1959" data-uri="spotify:timemachine:year:1950-1959">50s</a></li>
                                <li><a data-year="1960-1969" data-uri="spotify:timemachine:year:1960-1969">60s</a></li>
                                <li><a data-year="1970-1979" data-uri="spotify:timemachine:year:1970-1979">70s</a></li>
                                <li><a data-year="1980-1989" data-uri="spotify:timemachine:year:1980-1989">80s</a></li>
                                <li><a data-year="1990-1999" data-uri="spotify:timemachine:year:1990-1999">90s</a></li>
                                <li><a data-year="2000-2009" data-uri="spotify:timemachine:year:2000-2009">00s</a></li>
                                <li><a data-year="2010-2019" data-uri="spotify:timemachine:year:2010-2019">10s</a></li>
                            </ul>
                        </td>
                    </tr>
                </table>
            </div>
            <div id="playlist"></div>

            <!-- Main script file -->
            <script src="scripts/main.js"></script>
        </body>
    </html>

# The JavaScript file

The app logic is done in this JavaScript file. Note the extensive use of window.onmessage. Apps cannot natively access the Spotify function, instead it is doing the calls through
postMessage communication between the host app and the bungalow.

    /***
     * Time Machine sample app
     * The central mechanism of the Bungalow app framework is the communication between the host bungalow (the music player) and the client app (this).
     **/

    window.onmessage = function (event) {
        // Like how it was in Spotify Apps API, there is a navigation handler.
        // Listen to navigation by doing this
        if (event.data.action === 'navigate') {
            // Here we will perform argumentschanged operations.

            var arguments = event.data.arguments;
                // Like how it was in Spotify, we get the URL arguments here.

            // For demonstration purpose, we will show the current argument here
            $('#argument').html(event.data.arguments[1]);
            
            // In this example, we will threat the first argument as a genre search query in Spotify, to demonstrate the playlist facility.

            var query = 'year:' + event.data.arguments[1];

            // Highlight current year
            $('a').removeClass('active');
            $('a[data-year="' + event.data.arguments[1] + '"]').addClass('active');
            Search.search(query, 50, 0, 'track', function (tracks) {

                // Create a context view for the search result
                var contextView = new ContextView({
                    'uri': 'spotify:search:year:' + query,
                    'tracks': tracks
                }, {headers:true, fields: ['title', 'artist', 'duration', 'popularity', 'album']});
                $('#playlist').html("");
                $('#playlist').append(contextView.node);
            });
        }   
    }

# Converting Spotify HTML5 Apps to Bungalow Apps (Example app is apps-tutorial by Spotify iself)

My intention is to make it possible to run application written for the discountinued Spotify App finder to run in Bungalow. However, due to the complexity between the APIs, some manual tasks must be done.

1. Replace BundleName with AppName in the manifest.json

```json
{
  "AppName": {
    "en": "Spotify Apps API Tutorial"
  },
  "BundleIdentifier": "api-tutorial",
  "AppDescription": {
    "en": "A tutorial app for Spotify Apps API"
  },
  "AppIcon": {
    "18x18": "img/icons/icon-18x18.png",
    "32x32": "img/icons/icon-32x32.png",
    "64x64": "img/icons/icon-64x64.png",
    "128x128": "img/icons/icon-128x128.png",
    "300x300": "img/icons/icon-300x300.png"
  },
  "AcceptedLinkTypes": [
    "playlist"
  ],
  "BundleType": "Application",
  "BundleVersion": "0.2",
  "DefaultTabs": [
    {
      "arguments": "index",
      "title": {
        "en": "Home"
      }
    },
    {
      "arguments": "tabs",
      "title": {
        "en": "How to use tabs"
      }
    }
  ],
  "Dependencies": {
    "api": "1.38.0",
    "views": "1.18.1"
  },
  "SupportedLanguages": [
    "en"
  ],
  "VendorIdentifier": "com.spotify"
}
```

to

```json
{
  "BundleName": {
    "en": "Spotify Apps API Tutorial"
  },
  "BundleIdentifier": "api-tutorial",
  "AppDescription": {
    "en": "A tutorial app for Spotify Apps API"
  },
  "AppIcon": {
    "18x18": "img/icons/icon-18x18.png",
    "32x32": "img/icons/icon-32x32.png",
    "64x64": "img/icons/icon-64x64.png",
    "128x128": "img/icons/icon-128x128.png",
    "300x300": "img/icons/icon-300x300.png"
  },
  "AcceptedLinkTypes": [
    "playlist"
  ],
  "BundleType": "Application",
  "BundleVersion": "0.2",
  "DefaultTabs": [
    {
      "arguments": "index",
      "title": {
        "en": "Home"
      }
    },
    {
      "arguments": "tabs",
      "title": {
        "en": "How to use tabs"
      }
    }
  ],
  "Dependencies": {
    "api": "1.38.0",
    "views": "1.18.1"
  },
  "SupportedLanguages": [
    "en"
  ],
  "VendorIdentifier": "com.spotify"
}
```

2. Convert the require directives to requirejs compliant (use require.js)


from
```javascript
require([
    '$api/models',
    ], function(models) {

    function htmlEscape(str) {
        return String(str)
                .replace(/&/g, '&amp;')
                .replace(/"/g, '&quot;')
                .replace(/'/g, '&#39;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;');
    }

    function tabs() {
        var args = models.application.arguments;
        if (args) {
            var lastArg = args[args.length - 1];
            if (lastArg !== 'index' && lastArg !== 'tabs') {
                return;
           }
       }

        // compose file
        var file = args.length == 1 ? (args[0] + '.html') : '/tutorials/' + args.slice(0, args.length-1).join('/') + '.html';
        var xhr = new XMLHttpRequest();
        xhr.open('GET', file);
        xhr.onreadystatechange = function () {
            if (xhr.readyState != 4 || xhr.status != 200) return;

            var wrapper = document.getElementById('wrapper');
            wrapper.innerHTML = args[0] === 'index' ? '' : '<ul class="breadcrumb"><li><a href="spotify:app:api-tutorial:index">&laquo; Back to main page</a></li></ul>';
            if (args[0] === 'index') {
                var aux = document.createElement('div');
                aux.innerHTML = xhr.responseText;
                wrapper.innerHTML = aux.querySelector('#wrapper').innerHTML;
            } else {
                wrapper.innerHTML += xhr.responseText;
            }

            window.scrollTo(0, 0);
            var htmlSnippets = wrapper.querySelectorAll(".html-snippet");
            for (i = 0; i < htmlSnippets.length; i++) {
                container = htmlSnippets[i].getAttribute("data-container");
                if (container) {
                    document.getElementById(container).innerHTML = '<pre><code data-language="html">' + htmlEscape(htmlSnippets[i].innerHTML) + '</code></pre>';
                }
            }

            // search js snippets
            var scripts = wrapper.querySelectorAll("script");
            for (var i = 0; i < scripts.length; i++) {
                if (scripts[i].getAttribute('type') == 'script/snippet') {
                    var dataExecute = scripts[i].getAttribute('data-execute');
                    if (!dataExecute || dataExecute != 'no') {
                        eval(scripts[i].innerHTML);
                    }
                    var container = scripts[i].getAttribute("data-container");
                    if (container) {
                        document.getElementById(container).innerHTML = '<pre><code data-language="javascript">' + htmlEscape(scripts[i].innerHTML) + '</code></pre>';
                    }
                }
            }

            // search html snippets
            Rainbow.color();
        };
        xhr.send(null);
    }

    // When application has loaded, run pages function
    models.application.load('arguments').done(tabs);

    // When arguments change, run pages function
    models.application.addEventListener('arguments', tabs);
}); // require
```

to

```javascript

    function htmlEscape(str) {
        return String(str)
                .replace(/&/g, '&amp;')
                .replace(/"/g, '&quot;')
                .replace(/'/g, '&#39;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;');
    }

    function tabs() {
        
        // Insert window.onmessage listener for navigate action
        window.onmessage = function (event) {
            if (event.data.action === 'navigate') {
                var args = event.data.arguments;
                if (args) {
                    var lastArg = args[args.length - 1];
                    if (lastArg !== 'index' && lastArg !== 'tabs') {
                        return;
                   }
                }
            }
        }

        // compose file
        var file = args.length == 1 ? (args[0] + '.html') : '/tutorials/' + args.slice(0, args.length-1).join('/') + '.html';
        var xhr = new XMLHttpRequest();
        xhr.open('GET', file);
        xhr.onreadystatechange = function () {
            if (xhr.readyState != 4 || xhr.status != 200) return;

            var wrapper = document.getElementById('wrapper');
            wrapper.innerHTML = args[0] === 'index' ? '' : '<ul class="breadcrumb"><li><a href="spotify:app:api-tutorial:index">&laquo; Back to main page</a></li></ul>';
            if (args[0] === 'index') {
                var aux = document.createElement('div');
                aux.innerHTML = xhr.responseText;
                wrapper.innerHTML = aux.querySelector('#wrapper').innerHTML;
            } else {
                wrapper.innerHTML += xhr.responseText;
            }

            window.scrollTo(0, 0);
            var htmlSnippets = wrapper.querySelectorAll(".html-snippet");
            for (i = 0; i < htmlSnippets.length; i++) {
                container = htmlSnippets[i].getAttribute("data-container");
                if (container) {
                    document.getElementById(container).innerHTML = '<pre><code data-language="html">' + htmlEscape(htmlSnippets[i].innerHTML) + '</code></pre>';
                }
            }

            // search js snippets
            var scripts = wrapper.querySelectorAll("script");
            for (var i = 0; i < scripts.length; i++) {
                if (scripts[i].getAttribute('type') == 'script/snippet') {
                    var dataExecute = scripts[i].getAttribute('data-execute');
                    if (!dataExecute || dataExecute != 'no') {
                        eval(scripts[i].innerHTML);
                    }
                    var container = scripts[i].getAttribute("data-container");
                    if (container) {
                        document.getElementById(container).innerHTML = '<pre><code data-language="javascript">' + htmlEscape(scripts[i].innerHTML) + '</code></pre>';
                    }
                }
            }

            // search html snippets
            Rainbow.color();
        };
        xhr.send(null);
    }

    // When application has loaded, run pages function
    models.application.load('arguments').done(tabs);

    // When arguments change, run pages function
    models.application.addEventListener('arguments', tabs);
```

3. Convert 