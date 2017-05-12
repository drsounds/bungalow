const React = require('react');
const {Redirect} = require('react-router');


export class LoginView extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            loggedIn: false
        }
    }
    componentDidMount() {
        var loginWindow = window.open('https://accounts.spotify.com/authorize?client_id=9cae232f0ddd4ba3b55b7e54ca6e76f0&scope=user-read-private user-read-currently-playing user-read-playback-state user-modify-playback-state&response_type=code&redirect_uri=' + encodeURI('https://' + window.location.hostname + '/callback.html'));
		
    	var t = setInterval(() => {
			if (!loginWindow) {
				clearInterval(t);
                this.setState({
                    loggedIn:true
                });
			}
		});
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