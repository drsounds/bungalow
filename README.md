#Bungalow

Open Source Spotify client (designed to be the Eclipse of streaming music clients, other service should be easily integrated).

It should be run in nw.js and is based on a back and front end. The interaction with streaming music client is done in the back-end while the user interface and features (a.k.a Apps) are in the front end.

## Web-API branch
Last development sprint I used node-spotify but as Spotify announce End-of-Life for LibSpotify, I decided to migrate to Web API.
 Spotify's Web API is still not supporting streaming at this point, so right now I'm stuck with no streaming possibility. However,
I have refactored the code so streaming music API implementation is in a node server back end while user interface are a front end
communicating with the back end through a REST API. On this way the code is easier to maintain.

## Apps
Spotify killed their app finder earlier this year claiming majority are using mobile phones. However many users liked the Spotify Apps and I believe the termination of it's apps program was a financial action more than a shift to mobile.
 In this project, I have written a framework that emulates parts of the Spotify Apps framework, to encourage people to write plugins for Bungalow. The views are all written in this format.

# 