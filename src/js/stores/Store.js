const  EventEmitter = require('events');
const {AppDispatcher} = require('../dispatchers/AppDispatcher');
const moment = require('moment');

const CHECK_INTERVAL = 60000;

function diff(x, y) {
    return x > y ? x - y : y - x;
}
export class Store extends EventEmitter {
    constructor() {
        super();
    }
    saveState() {
        localStorage.setItem(this.constructor.name.substr(1) + 'Store', JSON.stringify(this.state));
    }
    addChangeListener(cb) {
        this.on('change', cb);
    }
    emitChange() {
        this.saveState();
        this.emit('change');
    }
    loadState() {
        if (localStorage.getItem(this.constructor.name.substr(1) + 'Store')) {
            this.state = JSON.parse(localStorage.getItem(this.constructor.name.substr(1) + 'Store'));
            if (this.state.enabled) {
                this.tick = setInterval(() => {
                    this.heartbeat();
                }, CHECK_INTERVAL);
            }
            this.emitChange();
            
        }
        
    }
}