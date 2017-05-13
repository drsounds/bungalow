const {AppDispatcher} = require('../dispatchers/AppDispatcher');
const {PlayerConstants} = require('../constants/PlayerConstants');
const {Store} = require('./Store');


class _PlayerStore extends Store {
    constructor() {
        super();
        this.state = {
            resources: {}
        }    
    }
}


export let PlayerStore = new _PlayerStore();


AppDispatcher.register((payload) => {
    if (payload.actionType === PlayerConstants.GET_RESOURCE) {
        PlayerStore.getLibrariesForBook(payload.uri);
    }
});
