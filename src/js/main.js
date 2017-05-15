const React = require('react');
const ReactDOM = require('react-dom');

const {
    Redirect,
    BrowserRouter,
    Route,
    browserHistory,
    Link
} = require('react-router-dom');

const {App} = require('./app');


class MainView extends React.Component {
    constructor(props) {
        super(props);
    }
    render() {
        return (
            <BrowserRouter>
                <App router={this} />
            </BrowserRouter>
        )    
    }
}


ReactDOM.render(<MainView />, document.querySelector('#app'));