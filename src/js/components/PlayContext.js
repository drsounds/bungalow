const React = require('react');
const {Redirect} = require('react-router');
const {MusicStore} = require('../stores/MusicStore');

const {Link} = require('react-router');
const {Loading} = require('react-loader');


export class PlayContext extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            object: null,
            selectedIndices: [],
            sorting: {
                name: (a,b) => { return a.name > b.name;},
                artist: (a,b) => { return a.artists[0].name > b.artists[0].name},
                release: (a,b) => { return a.album.name > b.album.name}
            },
            sort: null
        }
    }
    componentDidMount() {
        let uri = this.props.uri;
        MusicStore.getResourceByUri(this.props.uri);
        MusicStore.addChangeListener(() => {
            this.setState({
                object: MusicStore.state.object.resources[uri]
            });
        });
    }
    _onTouchTrack(i) {
        let track = this.state.objects[i];
        this.setState({
            selectedIndices: [i]
        });
    }
    _onDblClick(i) {
        let track = this.state.objects[i];
        MusicStore.playTrack(track, this.state);
    }
    render() {
        
        let tracks = this.state.tracks.objects;
        if (this.state.sort != null) {
            tracks = tracks.sort(this.state.sorting[this.state.sort]);
        }
        return (
            <Loading loaded={this.state.object != null}>
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
                        {this.state.object.tracks.map((o, i) => {
                            let isSelected = this.state.selectedIndices.indexOf(i) !== -1;
                            let className = MusicStore.state.player.item && MusicStore.state.player.item.uri === o.uri ? 'sp-current-track' : '';
                            return (
                            
                                <tr onDblClick={() => {this._onDblClick(i)}} onMouseDown={() => {this._onTouchTrack(i)}} className={className + ' ' + isSelected ? 'sp-track-selected' : ''}>
                                    <td>{o.name}</td>
                                    <td>{o.artists.map((artist) => {
                                       return <Link to={artist.name}>{artist}</Link>
                                    })}
                                    </td>
                                    <td><Link to={o.album.href}>{o.album}</Link></td>
                                </tr>
                            )
                        })}
                    </tbody>
                </table>
            </Loading>
        )
    }
}