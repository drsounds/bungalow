var models = require('@bungalow/models');
var exports = {};
/**
 * Abstract class for Mashcast user storage
 **/
var MashcastBackend = function () {

};
var Observable = models.Observable;

/**
 * Logs user in
 * @param {String} username The username
 * @param {String} password The password
 * @return {Promise} a promise 
 **/
MashcastBackend.prototype.login = function (username, password) {

}

/**
 * Adds podcast
 **/
MashcastBackend.prototype.addPodcast = function (podcast) {

}

/**
 * Returns if it has podcast
 * @param {String} podcast
 **/
MashcastBackend.prototype.hasPodcast = function (podcast) {

}

exports.onmediaended = function () {
    mashcast.stopEpisode();
    
}

var AudioApp = function (name, id, options) {
    this.name = name;
    this.id = id;
    this.options = options;
};

/***
 * Abstract class for getting and setting the user account 
 */
var Account = function () {
    
};
Account.prototype = new Observable();
Account.prototype.constructor = Observable;

Account.prototype.login = function () {
    
};

var LocalSettings = function () {

}
LocalSettings.prototype.setItem = function (key, value) {
  var config = bungalow_load_settings();
  if (!('localStorage' in config)) {
   config.localStorage = {};
  }
  config.localStorage[key] = JSON.parse(value);
  bungalow_save_settings(config);
}


LocalSettings.prototype.getItem = function (key) {
  var config = bungalow_load_settings();
  if (!('localStorage' in config)) {
   config.localStorage = {};
  }
  return JSON.stringify(config.localStorage[key]);
}
localSettings = new LocalSettings;
/**
 * Mashcast base class 
 * @constructor
 * @param {String} audioApp The name of the audio application
 * @this {Mashcast}
 * @implements {Observable}
 * @class
 */
var READY = 0;
var PLAYING = 1;
var Mashcast = function (audioApp) {
    this.podcasts = localSettings.getItem('podcasts') ? JSON.parse(localSettings.getItem('podcasts')) : [];
    this.broadcasts = []; 
    console.log("f", this.podcasts);
    this.date = null;
    this.audioApp = audioApp;
    this.channels = [];
    this.pendingEpisodes = [];
    this.episodes = [];
    this.episode = null;
    this.channel = null;
    this.playing = false;
    this.status = READY;

    var self = this;

    this.addEventListener('mediaended', function () {
      console.log("EVENT");
      mashcast.stopEpisode();
      self.episodes = []; // TODO Empty the qeue for now
      self.playing = false;
      
    });
    this.volume = 50;
    
    this.tickert = setInterval(function () {
        if (self.playing) {
            return;
        }
        var episode = self.episodes.shift();
        if (episode != null) {
            self.play(episode);

            if (!isNaN(episode.duration)) {
              setTimeout(function () {
                
                mashcast.stopEpisode();
                self.episodes = [];

              }, episode.duration);
            }
        }
    }, 1000);
    this.addEventListener('newepisode', function (e, evt, data) {
        var episode = data.episode;
        // // console.log("Episode", episode);
        if (self.episodes.length == 0 && !self.playing) // TODO Queue doesn't work empty it
        self.episodes.push(episode);
    });



};

Mashcast.prototype = new Observable();
Mashcast.prototype.constructor = Observable;


Mashcast.prototype.hasPodcast = function (podcast) {
  // console.log("podcasts", this.podcasts);
  this.podcasts = JSON.parse(localSettings.getItem('podcasts'));
    var podcasts = this.podcasts.filter(function (object) {
      // console.log(podcast, "Podcast");
       // console.log(podcast, object);
        return object && object.url === podcast.url || object.url == podcast;
    });
    // console.log("Has podcasts", podcast, podcasts);
  //  console.log("podcasts", podcasts);
    return podcasts.length > 0;
}

Mashcast.prototype.addPodcast = function (podcast) {
    this.podcasts.push(podcast);
    console.log(this.podcasts);
    localSettings.setItem('podcasts', JSON.stringify(this.podcasts));
}

Mashcast.prototype.removePodcast = function (podcast) {
    podcast = this.podcasts.filter(function (a) {
      return podcast.id == a.id;
    })[0]
    this.podcasts.splice(this.podcasts.indexOf(podcast), 1);
    localSettings.setItem('podcasts', JSON.stringify(this.podcasts));
}

/**
 * Returns the id of the last peisode for a given podcast with the url
 * @this {Podcast}
 * @function
 * @return {String|null} A string if found, otherwise null.
 */
Mashcast.prototype.setLatestEpisode = function (episode) {
    return localSettings.setItem('mashcast:podcast:' + this.url + ':episode', episode);
}


Mashcast.prototype.start = function () {
    // // console.log(this);
    // // console.log("Starting channel");
    var self = this;
    this.ticker = setInterval(function () {
        // // console.log("Checking channel");
        self.checkForNewEpisodes();
    }, 60000);

        self.checkForNewEpisodes();
}



Mashcast.prototype.stop = function () {
    clearInterval(this.ticker);
}


Mashcast.prototype.checkForNewEpisodes = function () {
    // // console.log("Checking for new episodes");
    var self = this;
   // We don't need CORS header since this will be run inside
    // a CEF-based app.
    console.log("Checking for new episodes");
    this.podcasts.forEach(function (podcast) {
      var url = podcast.url;

      try {
        if (podcast.schedule_url !== '' && podcast.schedule_url !== null) {
          console.log("Checking", podcast.schedule_url);
          // Check schedule
          $.getJSON('/json.php?url=' + encodeURI(podcast.schedule_url), function (data) {
            console.log("Got schedule URL");
            $.each(data.schedule, function (i, episode) {
              try {
                console.log("episode.start", episode.start);
                var start = new Date(Date.parse(episode.start));
                console.log(episode.duration);
                console.log(start);
                var end = new Date(start.getTime() + episode.duration * 1000);

                var now = new Date();
                var offset = now.getTime() - start.getTime();
                console.log("Checking programme ", episode);
                var d = now.getTime() > start && now.getTime() < end.getTime();
                console.log(now.getTime() - start);
                console.log("in program", d);
                console.log("start", start);
                console.log("end", end);
                console.log("now", now);
                if (d) {
                  console.log("Dispatch live episode", episode);
                  self.dispatchEvent('newepisode', {
                    episode: new Episode({
                        url:episode.schedule_url,
                        duration: (episode.duration - offset)
                    })
                  });
                }
              } catch (e) {
                  console.log(e, e.stack);
              }
            });
          }).fail(function (e) {
            console.log(e)
          });
          return;
        }
          var xmlHttp = new XMLHttpRequest();
          // console.log(url);
          // this.unsetLatestEpisode(url);
          xmlHttp.onreadystatechange = function () {
              // console.log(xmlHttp.readyState);
              if (xmlHttp.readyState == 4) {
                  // console.log(xmlHttp.status);
                  // // console.log("Connection start")
                  if (xmlHttp.status == 200) {                          // // console.log("Got data");
                      // // console.log(xmlHttp);
                      var xmlDoc = null;
                      // console.log(xmlHttp.responseText);
                      if (xmlHttp.responseXML != null) {
                          xmlDoc = xmlHttp.responseXML;
                      } else {
                          var parser = new DOMParser();
                          xmlDoc = parser.parseFromString(xmlHttp.responseText, "text/xml");
                          
                      }
                      
                      if (xmlDoc != null) {
                          // // console.log(url);
                         // console.log("TG"); 
                          var latestEpisode = xmlDoc.getElementsByTagName('item')[0];
                          if (!latestEpisode) 
                          {
                              // console.log("Error");
                              
                              return;
                          }
                          var items = xmlDoc.getElementsByTagName('item');
                          for (var i = items.length - 1; i >= 0; i--) {
                              var episode = items[i];
                              var pubDate = episode.getElementsByTagName('pubDate')[0].textContent;
                              pubDate = new Date(pubDate);
                              var now = mashcast.date != null ? mashcast.date : new Date();
                              // // console.log(now);
                              var delta = diff(now.getTime(), pubDate.getTime() );
                          
                              // // console.log("Difference", delta, pubDate);
                             
                              
                              var url = episode.getElementsByTagName("enclosure")[0].getAttribute('url');
                              //console.log(url);
                              // // console.log("Has new episodes", self.getLatestEpisode() != url);
                              if (delta > -1 && delta < 1000 * 60 * 60 * 2 && played_episodes.indexOf(url) < 0) {
                                  //if (self.getLatestEpisode() == url) {
                                   //   continue;
                                  //}
                                  // console.log("GT");
                                  mashcast.date = null;
                                  // // console.log("New episode found");
                                  // // console.log(self.mashcast);
                                  self.dispatchEvent('newepisode', {
                                          episode: new Episode({
                                              url:url,
                                              duration: null
                                          })
                                      
                                  });
                                  played_episodes += url + ';';
                                  
                                  self.setLatestEpisode(url);
                                  break;
                              }
                              // We use local storage to check for enw 
                              
                          }
                        // resolve(url);
                      } else {
                        console.log("No XML recognized");
                      }
                  } else {
                        console.log("No XML recognized");
                      //fail();
                  }
              }
          };
          // console.log("FL");
          // // console.log("Sending request");
          var url = '/rss.php?url=' + encodeURI(url);
          xmlHttp.open('GET', url, true);
          xmlHttp.send(null);
          // // console.log("Request sent");
      } catch (e) {
          console.log(e);
      }
  }); 
};

Mashcast.prototype.getChannelById = function (id) {
    for(var i = 0 ; i < this.channels.length; i++) {
        var channel = this.channels[i];
        if (channel.id == id)
            return channel;
    }
    return null;
}

Mashcast.prototype.request = function (method, address, query) {
     var promise = new Promise(function (resolve, fail) {
         var xmlHttp = new XMLHttpRequest();
         xmlHttp.onreadystatechange = function () {
             if (xmlHttp.readyState == 4) {
                 if (xmlHttp.status == 200) {
                     var json = JSON.parse(xmlHttp.responseText);
                     resolve(json);
                 }
             }
         };
         xmlHttp.open(method, address + '?' + query);
         xmlHttp.send(query);
     });
     return promise;
     
};

Mashcast.prototype.loadChannel = function (channel) {
     var self = this;
     var promise = new Promise(function (resolve, fail) {
  
         self.request('GET', 'http://api.radioflow.se/v1/channel/' + channel + '/', 'format=json').then(function (data) {
            var channel = new Channel(data, self, this.mashcast);
            self.channels.push(channel);
            resolve(channel);
            self.channel = channel;
         });
     });
     return promise;
};

/**
 * Stop (Mute) the music 
 */
Mashcast.prototype.stopMusic = function () {
    // // console.log("Stopping music");
  var event = new CustomEvent('episodestopped');
  event.data = episode;
  this.dispatchEvent(event);

  return MC.stopMusic(); //this.fadeOutMusic(this.audioApp);
}
/**
 * Stop (Mute) the music 
 */
Mashcast.prototype.startMusic = function () {
    // // console.log("Starting music");
    
    // In this build, we broadcast an event
  var event = new CustomEvent('episodestarted');
  event.data = episode;
  this.dispatchEvent(event);

    //return  MC.playMusic();  //this.fadeInMusic(this.audioApp).then(function (){});
};

Mashcast.prototype.fadeOutMusic = function (appId) {
    var promise = new Promise(function (resolve, fail) {
        var self = this;
       
        var appId = this.audioApp;
        // // console.log("A");
        // // console.log("Starting fading in music from" + self.volume);
                if (true) {
                    MC.stopMusic();
                    setTimeout(function () {
                        resolve();
                    }, 100);
        self.status = PLAYING;
        mashcast.playing = true;
                } else {
                    var ic = setInterval(function () {
            if (self.volume > -1) {
                self.volume -= 1;
               } else {
                   MC.stopMusic();
                     resolve(self);
                    clearInterval(ic);
                    __mashcast.showPopup(0, 'Radioflow', 'Music block started');
                    self.status = PLAYING;
                    mashcast.playing = true;
            
               }
               // // console.log(self.volume);
            var result = __mashcast.setApplicationVolume(appId, self.volume);     
            if (!result) {
                clearInterval(ic);
                fail();
                __mashcast.showMessage(0, "Could not sync volume, the program will not be played");
            } else {
                
            }
            // // console.log("setting volume to " + self.volume);
            
                    }, 100);
                }
    });
    return promise;
}
Mashcast.prototype.fadeInMusic = function () {
    var self = this;
    var promise = new Promise(function (resolve, fail) {
        var appId = this.audioApp;
        console.log(MC.play());
      // // console.log("Starting fading in music");
                MC.play();

      self.status = READY;
      mashcast.playing = false;
        if (false) {
                    var ic = setInterval(function () {
            
            if (self.volume < 50) {
                self.volume += 1;
                
            } else { 
                self.status = READY;
                mashcast.playing = false;
                fail(self);
                clearInterval(ic);
                
            }
            // // console.log("setting volume to " + self.volume);
            __mashcast.setApplicationVolume(appId, self.volume);
            // // console.log("T");
            
                    }, 100);
                }
                setTimeout(function () {
                    resolve(self);
                }, 100);
    });
    return promise;
}

Mashcast.prototype.playEpisode = function (episode) {
    var self = this;
   
    
    this.episodes.push(episode);
}

Mashcast.prototype.play = function (episode) {
    if (this.playing) {
        return;
    }
        if (episode == null) {
            return;
        }
    // // console.log(episode.url);
    var self = this;
    this.stopMusic();

}

 Mashcast.prototype.stopEpisode = function (episode) {
    
    this.episode = null;
            this.playing = false;
  /*  if (this.episodes.length > 0) {
        this.play();
    } else {
        this.startMusic(); // Unmute the music
    }*/
   //// alert(this.episodes.length);
   console.log(this.episodes);
    console.log(this.episodes.length);
  // if (this.episodes.length < 1) {
   //   // alert("T");
        this.startMusic();
   //}
};

/**
 *Queue the episode 
 */

Mashcast.prototype.enqueueEpisode = function (episode) {
    this.episodes.push(episode);  
};

/** 
 * Channel 
 **/
Channel = function (channel, mashcast) {
    // // console.log(arguments);
    this.podcasts = [];
    this.name = channel.name;
    this.id = channel.id;
    this.mashcast = mashcast;
    this.data = channel;
    this.status = READY;
     for (var i = 0; i < channel.podcasts.length; i++) {
        var podcast = new Podcast(channel.podcasts[i], this, this.mashcast);
        // // console.log("Podacst", podcast);
        this.podcasts.push(podcast);
        
     }

};


Channel.prototype = new Observable();
Channel.prototype.constructor = Observable;

/***
 * Check for new episodes
 **/
Channel.prototype.checkForNewEpisodes = function () {
    // console.log(this.podcasts);
    for (var i = 0; i < this.podcasts.length; i++) {
        // console.log("T");
        this.podcasts[i].checkForNewEpisodes();
    }
};

/**
 *Activates the channel
 * @function
 * @this {Channel} 
 */
Channel.prototype.start = function () {
    // // console.log("Starting channel");
    // // console.log("CHANNEL", this);
    // // console.log(this.podcasts);
    for (var i = 0; i < this.podcasts.length; i++) {
        var podcast = this.podcasts[i];
        // // console.log(podcast);
        podcast.start();
    }
    this.status = PLAYING;
    __mashcast.addActiveChannel(this.id);
}

/**
 * Turns of the channel 
 */
Channel.prototype.stop = function () {
    for (var i = 0; i < this.podcasts.length; i++) {
        var podcast = this.podcasts[i];
        podcast.stop();
    }
    this.status = READY;
    __mashcast.removeActiveChannel(this.id);
}
/*
 * Register a podcast channel into the system
 * @param {Podcast} podcast The podcast to register
 */
Channel.prototype.registerPodcast = function (podcast) {
    this.podcasts.push(podcast);
    var self = this;
    podcast.addEventListener('newepisode', function (event) {
        self.mashcast.enqueueEpisode(event.data.episode);
    });
};

/**
 * Unregister a podcast from the system
 * @method
 * @this {Mashcast}
 * @param {Podcast} podcast The podcast to unregister 
 */
Channel.prototype.unregisterPodcast = function (podcast) {
    this.podcasts.splice(this.podcasts.indexOf(podcast), 1);
};



/**
 * Podcast
 * @class
 * @this {Podcast}
 * @constructor
 * @param {String} url The url to the podcast 
 */
var Podcast = function (podcast, channel, mashcast) {
    this.url = podcast.url;
    this.name = podcast.name;
    this.channel = channel;
    
    this.ticker = null;
    this.mashcast = mashcast;
    // // console.log(mashcast);
}
Podcast.prototype = new Observable();
Podcast.prototype.constructor = Observable;

/***
 * Episode
 * @function
 * @constructor
 * @class
 * @param {String} url The url
 * @param {Mashcast} mashcast The instance of the mashcast object 
 */
var Episode = function (episode, mashcast) {
    this.url = episode.url;
    this.title = episode.title;
    this.mashcast = mashcast;
};
Episode.prototype = new Observable();
Episode.prototype.constructor = Observable;

/**
 * Starts playing the episode 
 */
Episode.prototype.play = function () {
    // // console.log(this.url);
    // Play the episode
    this.mashcast.playEpisode(this); // Request mashcast to play the programme
    this.dispatchEvent('episodestarted', {
        data: {
            episode: this
        }
    });
};

 /**
 * Starts playing the episode 
 */
Episode.prototype.stop = function () {
    // Play the episode
    this.mashcast.stopEpisode(this); // Request mashcast to play the programme
    this.dispatchEvent('episodeended', {
        data: {
            episode: this
        }
    });
};

 /**
 * Returns the id of the last peisode for a given podcast with the url
 * @this {Podcast}
 * @function
 * @return {String|null} A string if found, otherwise null.
 */
Podcast.prototype.getLatestEpisode = function () {
    return localSettings.getItem('mashcast:podcast:' + this.url + ':episode', null);
}

/**
 * Returns the id of the last peisode for a given podcast with the url
 * @this {Podcast}
 * @function
 * @return {String|null} A string if found, otherwise null.
 */
Podcast.prototype.setLatestEpisode = function (episode) {
    return localSettings.setItem('mashcast:podcast:' + this.url + ':episode', episode);
}

/**
 *Unset latest episode 
 */
Podcast.prototype.unsetLatestEpisode = function (episode) {
    return localSettings.setItem('mashcast:podcast:' + this.url + ':episode', null);
}

function diff(x, y) {
    return x - y ? x - y : y - x;
}


var played_episodes = '';


/**
 * Check for updates
 * @this {Podcast}
 * @param {Object} url
 * 
 */
Podcast.prototype.checkForNewEpisodes = function () {
    // // console.log("Checking for new episodes");
    var self = this;
   // We don't need CORS header since this will be run inside
    // a CEF-based app.
    try {
        var xmlHttp = new XMLHttpRequest();
        var url = this.url;
        // console.log(url);
        var self = this;
      if (podcast.stream_url !== '') {
        // Check schedule
        $.getJSON(podcast.schedule_url, function (data) {
          $.each(data.schedule, function (episode) {
            var start = new Date(episode.start);
            var end = new Date(start.getTime() + episode.duration * 1000);
            var now = new Date();
            var offset = now.getTime() - start;
            if (now.getTime() > start && now.getTime() < end) {
              self.dispatchEvent('newepisode', {
                episode: new Episode({
                    url:episode.stream_url,
                    duration: episode.duration - offset
                })
              });
            }
          });
        });
        return;
      }
        // this.unsetLatestEpisode(url);
        xmlHttp.onreadystatechange = function () {
            // console.log(xmlHttp.readyState);
            if (xmlHttp.readyState == 4) {
                // console.log(xmlHttp.status);
                // // console.log("Connection start")
                if (xmlHttp.status == 200) {
                    // // console.log("Got data");
                    // // console.log(xmlHttp);
                    var xmlDoc = null;
                    // console.log(xmlHttp.responseText);
                    if (xmlHttp.responseXML != null) {
                        xmlDoc = xmlHttp.responseXML;
                    } else {
                        var parser = new DOMParser();
                        xmlDoc = parser.parseFromString(xmlHttp.responseText, "text/xml");
                    }
                    // console.log(xmlDoc);
                    if (xmlDoc != null) {
                        // // console.log(url);
                       console.log(xmlDoc.responseXML);
                       // console.log("TG"); 
                        var latestEpisode = xmlDoc.getElementsByTagName('item')[0];
                        if (!latestEpisode) 
                        {
                            // console.log("Error");
                            
                            return;
                        }
                        var items = xmlDoc.getElementsByTagName('item');
                        for (var i = items.length - 1; i >= 0; i--) {
                            var episode = items[i];
                            var pubDate = episode.getElementsByTagName('pubDate')[0].textContent;
                            pubDate = new Date(pubDate);
                            var now = mashcast.date != null ? mashcast.date : new Date();
                            // // console.log(now);
                            var delta = diff(now.getTime(), pubDate.getTime() );
                        
                            // // console.log("Difference", delta, pubDate);
                           
                            
                            var url = episode.getElementsByTagName("enclosure")[0].getAttribute('url');
                            // // console.log("Has new episodes", self.getLatestEpisode() != url);
                            if (delta > -1 && delta < 1000 * 60 * 60 * 2 && played_episodes.indexOf(url) < 0) {
                                //if (self.getLatestEpisode() == url) {
                                 //   continue;
                                //}
                                // console.log("GT");
                                mashcast.date = null;
                                // // console.log("New episode found");
                                // // console.log(self.mashcast);
                                self.mashcast.dispatchEvent('newepisode', {
                                        episode: new Episode({
                                            url:url,
                                          duration: null
                                        })
                                    
                                });
                                played_episodes += url + ';';
                                
                                self.setLatestEpisode(url);
                                break;
                            }
                            // We use local storage to check for enw 
                            
                        }
                      // resolve(url);
                    } else {
                     //  fail();
                    }
                } else {
                    // // console.log("Error occured");
                    //fail();
                }
            }
        };
        // console.log("FL");
        // // console.log("Sending request");
        var url = '/rss.php?url=' + encodeURI(url);
        console.log(url);
        xmlHttp.open('GET', url, true);
        xmlHttp.send(null);
        // // console.log("Request sent");
    } catch (e) {
        // console.log(e.stack);
    }
        
};

Podcast.prototype.start = function () {
    // // console.log(this);
    // // console.log("Starting channel");
    var self = this;
    this.ticker = setInterval(function () {
        // // console.log("Checking channel");
        self.checkForNewEpisodes();
    }, 60000);
    setTimeout(function () {
        // // console.log("Checking channel");
        self.checkForNewEpisodes();
    }, 1000);
};
Podcast.prototype.stop = function () {
    clearInterval(this.ticker);
};
exports.Mashcast = Mashcast;
exports.Podcast = Podcast;
exports.AudioApp = AudioApp;
exports.Account = Account;
exports.Channel = Channel;
exports.Episode = Episode;
module.exports = exports;
