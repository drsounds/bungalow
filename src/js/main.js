const React = require('react');
const ReactDOM = require('react-dom');

const {StartView} = require('./views/StartView');
const {LoginView} = require('./views/LoginView');
const {PlaylistView} = require('./views/PlaylistView');
const {MusicStore} = require('./stores/MusicStore');


const {
  BrowserRouter,
  Route,
  browserHistory,
  Link
} = require('react-router-dom');


class MainView extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            menu: {
                objects: [{
                    id: 'start',
                    name: 'Start',
                    type: 'view',
                    href: '/',
                    uri: 'bungalow:internal:start'
                }]
            },
            playlists: []
        }
        this._onSearch = this._onSearch.bind(this);
        window.addEventListener('resize', () => {
            this.setState();
        })
    }
    _onSearch(event) {
        event.preventDefault();
    }
    render() {
        return (
            <BrowserRouter history={browserHistory}>
                <div style={{width: (window.innerWidth - 20) + 'px',height: (window.innerHeight - 160) + 'px'}}>
                    <header>
        				<div className="btn-group">
        					<button >&lt;</button>
        					<button>&gt;</button>
        				</div>
        				<form onSubmit={this._onSearch}>
        					<input type="search" id="search" placeholder="Search" />
        				</form> 
        			</header>
        			{this.state.message != null &&
        			<div className="alert alert-warning" id="alert">
        				<p>{this.state.message.description} <a style={{color: 'black', marginRight: '10pt', float: 'right'}}>x</a></p>
        			</div>}
        			<main>
        				<aside style={{width: '100pt'}}>
        					<div id="menu" style={{overflow: 'scroll'}}>
        						<ul cellspacing="0" width="100%" className="menu">
        						    {this.state.menu.objects.map((o) => {
        						        return (
        							        <li><Link to={o.href}><span className="fa fa-home"></span>{o.name}</Link></li>
        						 
        						        )
        						    })}
                                    
        						</ul>
        						<ul id="playlists" cellspacing="0" width="100%" className="menu">
        							{this.state.playlists.map((o) => {
        						        return (
        							        <li><Link to={o.href}><span className="fa fa-home"></span>{o.name}</Link></li>
        						 
        						        )
        						    })}
        							
        						</ul>
        					</div>
        
        					<div id="nowplaying" >
        						<div id="nowplaying_header">
        							<p id="song_title"></p>
        						</div>
        						<div id="nowplaying_image" style={{backgroundSize: 'cover', flex: 1}}></div>
        					</div>
        				</aside>
        				<div id="viewstack">
                            <Route component={StartView} path="/" />
                            <Route component={LoginView} path="/login" />
                            <Route component={PlaylistView} path="/user/:username/playlist/:identifier" />
        				</div>
        				<aside style={{display: 'none', width: '15%', position: 'relative'}}>
        					<div style={{position: 'absolute', left: '50%', top: '50%', 'webkitTransform': 'translate(-50%, -50%)'}}>
        						Friend feed
        					</div>
        				</aside>
        			</main>
        			
        			<footer>
        				<div className="conliols" style={{textAlign: 'center', width: '200px'}}>
        					<i id="btnSkipBack" onclick="shell.playPrevious()" className="fa fa-fast-backward btn player-btn"></i>
        					<i id="btnPlay" onclick="shell.playPause()" className="fa fa-play btn player-btn" style={{webkitTransform: 'scale(1.5)'}}></i>
        					<i id="btnSkipNext" onclick="shell.playNext()" className="fa fa-fast-forward btn player-btn"></i>
        				</div>
        				<input type="range" style={{width: '100%'}} min="0" max="1" value="0" id="liack_position" />
        				<div className="controls" style={{width: '150pt'}}>
        					<a onclick="shell.toggleMashcast()"><i className="fa fa-toggle-off"></i> Podcasts</a>&nbsp;
        					<i className="fa fa-repeat"></i>
        				</div>
        			</footer>
                </div>
            </BrowserRouter>
        )    
    }
}


ReactDOM.render(<MainView />, document.querySelector('#app'));