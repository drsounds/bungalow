const React = require('react');
const {Redirect} = require('react-router-dom');
const {MusicStore} = require('../stores/MusicStore');
const {uriToPath} = require('../utils');
const {Link} = require('react-router-dom');
const Loading = require('react-loader');


export class PlayContext extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            uri: this.props.uri,
            objects: [],
            showHeaders: this.props.showHeaders,
            selectedIndices: [],
            sorting: {
                name: (a,b) => { return a.name > b.name;},
                artist: (a,b) => { return a.artists[0].name > b.artists[0].name},
                release: (a,b) => { return a.album.name > b.album.name}
            },
            loaded: false,
            sort: null
        }
    }
    componentDidMount() {
        let uri = this.props.uri;
        MusicStore.fetchObjectsFromCollection(this.props.uri);
        MusicStore.addChangeListener(() => {
            let uri = this.props.uri;
            let state = MusicStore.state.resources[uri];
            if (state)
            this.setState({
                loaded: true,
                objects: state.objects
            });
        });
    }
    _onTouchTrack(i) {
        let track = this.state.objects[i];
        this.setState({
            selectedIndices: [i]
        });
    }
    _onDoubleClick(i) {
        let track = this.state.objects[i];
        MusicStore.play({
            context_uri: this.state.uri.substr(0, this.state.uri.length - ':track'.length),
            offset: {
                position: i
            }
        });
    }
    render() {
        
        let tracks = this.state.objects;
        if (this.state.sort != null) {
            tracks = tracks.sort(this.state.sorting[this.state.sort]);
        }
        return (
            <Loading loaded={this.state.loaded}>
                <table className="sp-table" style={{width: '100%'}}>
                    {this.state.showHeaders && 
                    <thead>
                        <tr>
                            <th>Name</th>
                            <th>Artist</th>
                            <th>Album</th>
                        </tr>
                    </thead>}
                    <tbody>
                        {tracks instanceof Array && tracks.map((o, i) => {
                            let isSelected = this.state.selectedIndices.includes(i);
                            let className = MusicStore.state.player.item && MusicStore.state.player.item.uri === o.uri ? 'sp-now-playing' : '';
                            return (
                            
                                <tr key={i} onDoubleClick={() => {this._onDoubleClick(i)}} onMouseDown={() => {this._onTouchTrack(i)}} className={className + ' ' + (isSelected ? 'sp-track-selected' : '')}>
                                    <td>{o.name}</td>
                                    <td>{o.artists instanceof Array && o.artists.map((artist) => {
                                       return <Link to={uriToPath(artist.uri)}>{artist.name}</Link>
                                    })}
                                    </td>
                                    {o.album &&
                                    <td><Link to={uriToPath(o.album.uri)}>{o.album.name}</Link></td>}
                                </tr>
                            )
                        })}
                    </tbody>
                </table>
            </Loading>
        )
    }
}