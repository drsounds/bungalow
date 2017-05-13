const React = require('react');
const {Redirect} = require('react-router-dom');

const {MusicStore} = require('../stores/MusicStore');
const {PlayContext} = require('../components/PlayContext');
const {Header} = require('../components/Header');
const Loader = require('react-loader');


Object.prototype.toPath = function () {
    let t = '';
    for (let k of Object.keys(this)) {
        t += '/' + k + '/' + k.value;
    }
    return t;
}

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
        let uri = 'bungalow:user:' + this.props.match.params.username +
            ':playlist:' + this.props.match.params.identifier; 
        MusicStore.getResourceByUri(uri);
        MusicStore.addChangeListener(() => {
            this.setState({
                loaded: true,
                object: MusicStore.state.resources[uri],
                track: MusicStore.state.track
            });
        })
    }
    render() {
        return (
            <Loader loaded={this.state.loaded}>
                {this.state.object &&
                <div className="sp-container">
                    <Header object={this.state.object} />
                    <PlayContext showHeaders={true} uri={this.state.object.uri + ':track'} />
                </div>}
            </Loader>
        )
    }
}