var React = require('react');
var ReactCanvas = require('react-canvas');

var Surface = ReactCanvas.Surface;
var Image = ReactCanvas.Image;
var Text = ReactCanvas.Text;
var Layer = ReactCanvas.Layer;

var BAR_WIDTH = 2;
var BAR_HEIGHT = 7;
var SPACE = 1;
var colorDark = 'rgba(255, 255, 255, 0.215)'; // 'rgba(89, 89, 89)';
var colorLight = 'rgba(255, 255, 255, .882)'; //'rgba(225, 255, 255)';
export default class PopularityBar extends React.Component {
    constructor(props) {
        super(props);
        console.log(props);
        this.renderBars = this.renderBars.bind(this);
    }

    renderBars() {
        var bars = [];
        var totalPigs = 0
        for (var i = 0; i < 120; i+= BAR_WIDTH + SPACE) {
            bars.push(
                <Layer top={0} left={i} backgroundColor={colorDark} width={BAR_WIDTH} height={BAR_HEIGHT}></Layer>
            );
            totalPigs++;
        }
        var fillStyle = colorLight;

        var lightPigs = this.props.popularity * totalPigs;
        var left = 0;
        for (var i = 0; i < lightPigs; i++) {
            bars.push(
                <Layer left={left} top={0} width={BAR_WIDTH} height={BAR_HEIGHT}></Layer>
            );
            left += BAR_WIDTH + SPACE;
        }
        return bars;
    }

    render() {
        return (
            <Surface width={120} height={8}>
                {this.renderBars()}
            </Surface>
        )
    }
}

PopularityBar.PropTypes = {
    popularity: React.PropTypes.number.isRequired
}