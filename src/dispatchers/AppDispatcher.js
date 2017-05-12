const Dispatcher = require('flux').Dispatcher;


export let AppDispatcher = Object.assign(new Dispatcher(), {
  handleViewAction: function(action) {
    this.dispatch({
      source: 'VIEW_ACTION',
      action: action
    });
  }
});