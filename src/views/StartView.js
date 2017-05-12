const React = require('react');
const {Redirect} = require('react-router');
const {MusicStore} = require('../stores/MusicStore');


export class StartView extends React.Component {
    constructor(props) {
        super(props);
        
    }
    componentDidMount() {
        
    }
    render() {
        return (
            <div>
            {this.state.loggedIn &&
                <Redirect to="/" />
            }
            </div>
        )
    }
}