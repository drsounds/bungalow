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
        this.loadState();
        
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
            this.state.player.item = result.item;
        
            this.emitChange();
        }, (error) => {
        });
        
    }
    async resume() {
        
    }
    async pause() {
        await fetch('/api/music/me/player/pause', {method: 'PUT', mode: 'cors', credentials: 'include'});
    }
    
    
    async fetchObjectsFromCollection(uri) {
       
        let state = this.state.resources[uri];
        if (state != null) {
            
            setTimeout(() => {this.emitChange()}, 1);
            return;
        
        } else {
            state = {
                objects: [],
                page: -1
            };
        }
    
        state.page++;
        let url = '/api/music/' + uri.substr(uri.split(':')[0].length + 1).split(':').join('/') + '?p=' + state.page;
        let result = await fetch(url, {method: 'GET', credentials: 'include', mode: 'cors'}).then((result) => result.json());
        for (let obj of result.objects) {
            state.objects.push(obj);   
        }
        this.state.resources[uri] = state;
    
        this.emitChange();
    }
}


export let MusicStore = new _MusicStore();


AppDispatcher.register((payload) => {
    if (payload.actionType === MusicConstants.GET_RESOURCE_BY_URI) {
        MusicStore.getResourceByUri(payload.uri);
    }
    if (payload.actionType == MusicConstants.FETCH_RESOURCE_FROM_URI) {
        MusicStore.fetchObjectsFromCollection(payload.uri, payload.p);
    }
});
