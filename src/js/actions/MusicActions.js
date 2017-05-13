const {MusicConstants} = require('../constants/MusicConstants');
const {AppDispatcher} = require('../dispatchers/AppDispatcher');


export let MusicActions = {
    getResourceByUri(uri) {
         AppDispatcher.dispatch({
            actionType: MusicConstants.GET_RESOURCE_BY_URI,    
            uri: uri
        });            
    },
    playContext(context) {
        AppDispatcher.dispatch({
            actionType: MusicConstants.PLAY_CONTEXT,
            context: context
        });  
    }
}