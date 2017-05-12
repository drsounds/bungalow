const React = require('react');
const {Redirect} = require('react-router');
const {MusicStore} = require('../stores/MusicStore');


export class PlaylistView extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            object: null,
            loaded: false,
            track: MusicStore.state.track
        }
    }
    componentDidMount() {
        let uri = 'bungalow:' + this.props.matches.params.join(':');
        MusicStore.addChangeListener(() => {
            this.setState({
                object: MusicStore.state.resources[uri],
                track: MusicStore.state.track
            });
        })
    }
    render() {
        return (
            <Loader loaded={this.state.loaded}>
                <div className="sp-container">
                    <Header obj={this.state.playlist} />
                    <PlayContext uri={this.state.object.uri + ':track'} />
                </div>
            </Loader>
        )
    }
}