# Writing apps and bungalows for Bungalow music Client

Bungalow music client has a Spotify Apps like infrastructure. It has two main types of applications, Apps and Bungalows. Apps are what they are in Spotify apps, apps that can be installed and accessed from the sidebar.

Bungalows howevers are mini apps that can placed on different views in the Spotify client. They consists of an iframe embedded in some views, that can be configured to show a certain 'bungalow' at the user choice.

# Writing an app or bungalow

The base is the same. Currently all bungalows and apps are stored in the <APP directory>/apps/ directory. The base is same for both formats.

It has two main files

* Manifest.json - The manifest file

For a bungalow

    {
        "BundleType": "Bungalow",
        "BundleName": {
        	"en": "Sample application"
    	},
    	"socket": "album-view",
    	"BundleIdentifier": "testbungalow"
	}

Legend

	* Socket - defines what socket to override.