const React = require('react');
const {Redirect} = require('react-router-dom');

const {MusicStore} = require('../stores/MusicStore');
const {PlayContext} = require('../components/PlayContext');
const {Header} = require('../components/Header');
const Loader = require('react-loader');
const {Separator} = require('../components/Separator');
const {AlbumContext} = require('../components/AlbumContext');


export class ArtistView extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            object: null
        }
    }
    componentDidMount() {
        let uri = 'bungalow:artist:' + this.props.match.params.identifier; 
        MusicStore.getResourceByUri(uri);
        MusicStore.addChangeListener(() => {
            this.setState({
                loaded: true,
                object: MusicStore.state.resources[uri]
            });
        })
    }
    render() {
        return (
            <Loader loaded={this.state.object != null}>
                {this.state.object &&
                    <div>
                        <div className="sp-container">
                            <Header object={this.state.object} />
                        </div>
                        <Separator>Albums</Separator>
                        <AlbumContext uri={this.state.object.uri + ':release'} />
                    </div>
                }
            </Loader>
        )
    }
}