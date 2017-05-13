const {AppDispatcher} = require('../dispatchers/AppDispatcher');
const {MusicConstants} = require('../constants/MusicConstants');
const {Store} = require('./Store');


class _MusicStore extends Store {
    constructor() {
        super();
        this.state = {
            resources: {},
            player: {
                is_playing: false,
                item: null,
                progress_ms: 0,
                context: null
            }
        }
        let i = setInterval(() => {
            let url = '/api/music/me/player';
            let result = fetch(url, {method: 'GET', credentials: 'include', mode: 'cors'}).then((result) => result.json()).then((result) => {
                this.state.player = result;
                this.emitChange();
            });
        }, 1000);
        
    }
    /**
     * Get libraries for book with ISBN
     * @param String isbn The ISBN number for the book to look up
     * @async
     * @return An book object
     **/
    async getResourceByUri(uri) {
        let result = null;
        if ((uri in this.state.resources)) {
            result = await new Promise((resolve, reject) => {
                resolve(this.state.resources[uri])
            });
        } else {
            let url = '/api/music/' + uri.substr(uri.split(':')[0].length + 1).split(':').join('/');
            result = await fetch(url, {method: 'GET', credentials: 'include', mode: 'cors'}).then((result) => result.json());
        }
        this.state.resources[uri] = result;
        this.emitChange();    
    }
    
    async play(context) {
        fetch('/api/music/me/player/play', {headers: {'Content-Type': 'application/json'}, method: 'PUT', body: JSON.stringify(context), mode: 'cors', credentials: 'include'}).then((result) => result.json()).then((result) => {
            
        }, (error) => {
        });
        
    }
    async resume() {
        
    }
    async pause() {
        await fetch('/api/music/me/player/pause', {method: 'PUT', mode: 'cors', credentials: 'include'});
    }
    
    
    async fetchObjectsFromCollection(uri) {
        if (!(uri in this.state.resources)) {
            this.state.resources[uri] = {
                objects: [],
                page: -1
            };
        }
        let state = this.state.resources[uri];
        state.page += 1;
        let url = '/api/music/' + uri.substr(uri.split(':')[0].length + 1).split(':').join('/') + '?p=' + state.page;
        let result = await fetch(url, {method: 'GET', credentials: 'include', mode: 'cors'}).then((result) => result.json());
        state.objects = result.objects;
        this.state.resources[uri] = state;
        this.emitChange();
    }
}


export let MusicStore = new _MusicStore();


AppDispatcher.register((payload) => {
    if (payload.actionType === MusicConstants.GET_RESOURCE) {
        MusicStore.getLibrariesForBook(payload.uri);
    }
});
