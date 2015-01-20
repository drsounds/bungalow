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

Legend

* Socket - defines what socket to override. Currently no sockets are implemented now.
* BundleName - Like app name in Spotify Apps
* BundleIdentifier - please be same as the folder name.

# The index.html file

The index.html file is the base point for a bungalow app. It must have the following structure

    <!DOCTYPE html>
    <html>
        <head>
            <title>App name</title>
            <base href="Base uri">
            <link rel="stylesheet" href="http://127.0.0.1:9261/public/bower_components/font-awesome/css/font-awesome.css">
            <script src="http://127.0.0.1:9261/bower_components/jquery/dist/jquery.js"></script>

            <!-- The JavaScript API -->
            <script src="http://127.0.0.1:9261/apps/api/api.js"></script>
            <link rel="stylesheet" href="http://127.0.0.1:9261/themes/main.css">
        </head>
        <body>
            <div class="container">
                <!-- User interface goes here -->  

            </div>
            <!-- Main script file -->
            <script src="scripts/main.js"></script>
        </body>
    </html>

# JavaScript API

    The Bungalow framework has it API incorporated in a single file.s