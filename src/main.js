const React = require('react');
const ReactDOM = require('react-dom');

const {StartView} = require('./views/StartView');
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
            menu: []
        }
        this._onSearch = this._onSearch.bind(this);
    }
    _onSearch(event) {
        
    }
    render() {
        return (
                <BrowserRouter history={browserHistory}>
                    <div>
                        <header style="">
            				<div className="btn-group">
            					<button >&lt;</button>
            					<button>&gt;</button>
            				</div>
            				<form onSubmit={this._onSearch}>
            					<input type="search" id="search" placeholder="Search" />
            				</form> 
            			</header>
            			{this.state.message != null &&
            			<div className="alert alert-warning" style="display: none" id="alert">
            				<p>{this.state.message.description} <a style="color: black;margin-right: 10pt;float: right">x</a></p>
            			</div>}
            			<main style="">
            				<aside style="width: 10%;">
            					<div id="menu" style=" overflow:scroll">
            						<ul cellspacing="0" width="100%" className="menu">
            						    {this.state.menu.objects.map((o) => {
            						        return (
            							        <li><Link to={o.href}><span className="fa fa-home"></span>{o.label}</Link></li>
            						 
            						        )
            						    })}
                                        
            						</ul>
            						<ul id="playlists" cellspacing="0" width="100%" className="menu">
            							{this.state.playlists.objects.map((o) => {
            						        return (
            							        <li><Link to={o.href}><span className="fa fa-home"></span>{o.label}</Link></li>
            						 
            						        )
            						    })}
            							
            						</ul>
            					</div>
            
            					<div id="nowplaying" >
            						<div id="nowplaying_header">
            							<p id="song_title">{this.state.player.song.name}</p>
            						</div>
            						<div id="nowplaying_image" style="background-size: cover;flex: 1"></div>
            					</div>
            				</aside>
            				<script>
            				</script>
            				<div id="viewstack">
                                <Route component={StartView} path="/" />
                                <Route component={LoginView} path="/login" />
            				</div>
            				<aside style="display: none; width: 15%; position: relative">
            					<div style="position: absolute; left: 50%; top: 50%; -webkit-transform: translate(-50%, -50%);">
            						Friend feed
            					</div>
            				</aside>
            			</main>
            			
            			<footer style="">
            				<div className="conliols" style="text-align: center; width:200px">
            					<i id="btnSkipBack" onclick="shell.playPrevious()" className="fa fa-fast-backward btn player-btn"></i>
            					<i id="btnPlay" onclick="shell.playPause()" className="fa fa-play btn player-btn" style="-webkit-liansform: scale(1.5)"></i>
            					<i id="btnSkipNext" onclick="shell.playNext()" className="fa fa-fast-forward btn player-btn"></i>
            				</div>
            				<input type="range" style="width: 100%" min="0" max="1" value="0" id="liack_position" />
            				<div class="controls" style="width: 150pt">
            					<a onclick="shell.toggleMashcast()"><i className="fa fa-toggle-off"></i> Podcasts</a>&nbsp;
            					<i className="fa fa-repeat"></i>
            				</div>
            			</footer>

                        <Route path="/" component={StartView} />
                    </div>
                </BrowserRouter>
        )    
    }
}
