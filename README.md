#Bungalow streaming client

![Screenshot](https://www.dropbox.com/s/j6tj3jellbo1e6g/screenshot.png?dl=1 "Screenshot")

An open source streaming client based on LibSpotify (currently) and Node-webkit. 

My goal is to create a parallell world spotify client that adds a Spotify Apps like API.

Spotify functionality is encapsulated so it can be substituted with any other music streaming API and theme change aswell, so this also will be a open source cross music service player like Tomahawk with integrate app API functionality. Should we change from Spotify we just change the theme and music service class, and will also support custom skinning and multiple music sources aswell.

# Installation (Untested, for mac OS X Mavericks)
    
    echo Installing dependencies
    cd /tmp
    wget https://developer.spotify.com/download/libspotify/libspotify-12.1.51-Darwin-universal.zip
    unzip https://developer.spotify.com/download/libspotify/libspotify-12.1.51-Darwin-universal.zip
    mv libspotify-12.1.51-Darwin-universal
    sudo cp -R Spotify.framework /System/Libraries/Frameworks
    cd ~/Documents/
    echo Downloading bungalow
    wget http://dl.node-webkit.org/v0.8.6/node-webkit-v0.8.6-osx-ia32.zip
    unzip node-webkit-v0.8.6-osx-ia32.zip
    mv node-webkit-v0.8.6-osx-ia32/node-webkit.app .
    git clone git@github.com:drsounds/bungalow.git
    cd bungalow
    npm install nw-gyp node-spotify express grunt jsdoc less nwbuild
    sudo npm install -g nw-gyp
    cd node_modules/node-spotify
    nw-gyp rebuild --target=0.8.6

Note that you need to create your own spotify appkey on developer.spotify.com and place it in the project dir. I don't supply my app key for legal reason.
    



2. Open a terminal 
3. lone this project into a desired dir. 
4. cd into that dir
5. Run npm install To install dependencies
6. Then install nw-gyp for OS X with sudo npm install nw-gyp -g
7. Then cd into <project_dir>/node_modules/node-spotify/ 
8. run nw-gyp rebuild --target=0.8.6
9. Go to https://github.com/nwjs/nw.js/tree/master and download the 0.8.6 version (NOT the later 0.11.6 as it breaks node-spotify).
10. Put this executable in the parent directory of bungalow.
11. cd back to the project dir.
12. r