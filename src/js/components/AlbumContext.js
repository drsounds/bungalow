const React = require('react');
const {Redirect} = require('react-router-dom');
const {MusicStore} = require('../stores/MusicStore');
const {uriToPath} = require('../utils');
const {Link} = require('react-router-dom');
const Loading = require('react-loader');
const {PlayContext} = require('./PlayContext');

export class AlbumContext extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            uri: this.props.uri,
            objects: [],
            loaded: false
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
                objects: MusicStore.state.resources[uri].objects
            });
        });
    }
    _onTouchTrack(i) {
        let object = this.state.objects[i];
        this.setState({
            selectedIndices: [i]
        });
    }
    render() {
        return (
            <Loading loaded={this.state.loaded}>
                <table style={{width: '100%', 'cell-padding': '3pt'}}>
                    <tbody>
                        {this.state.objects.map((o, i) => {
                            return (
                            
                                <tr key={i}>
                                    <td style={{paddingLeft: '3pt',paddingBottom: '15pt', paddingTop: '15pt', width: '96pt', verticalAlign: 'top'}}>
                                        <Link to={uriToPath(o.uri)}><img width="96pt" className="sp-cover-image" src={o.images[0].url} /></Link>
                                    </td>
                                    <td style={{paddingLeft: '3pt', paddingBottom: '15pt', paddingTop: '15pt', verticalAlign: 'top'}}>
                                        <Link to={uriToPath(o.uri)}><h2>{o.name}</h2></Link>
                                        <PlayContext uri={o.uri + ':track'} />
                                    </td>
                                </tr>
                            )
                        })}
                    </tbody>
                </table>
            </Loading>
        )
    }
}