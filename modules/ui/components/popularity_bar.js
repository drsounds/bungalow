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
var PopularityBar = React.createClass({

    renderBars: function() {
        var bars = [];
        var totalPigs = 0
        for (var i = 0; i < 120; i+= BAR_WIDTH + SPACE) {
            bars.push(
                React.createElement(Layer,
                    {
                        top: 0,
                        left: i,
                        backgroundColor: colorDark,
                        width: BAR_WIDTH,
                        height: BAR_HEIGHT
                    }
                )
            );
            totalPigs++;
        }
        var fillStyle = colorLight;

        var lightPigs = this.props.popularity * totalPigs;
        var left = 0;
        for (var i = 0; i < lightPigs; i++) {
            bars.push(
                React.createElement(Layer, 
                    {
                        left: left,
                        top: 0,
                        width: BAR_WIDTH,
                        height: BAR_HEIGHT
                    }
                )
            );
            left += BAR_WIDTH + SPACE;
        }
        return bars;
    },

    render: function() {
        return (
            React.createElement(Surface, 
                {
                    width: 120,
                    height: 10
                },
                this.renderBars()
            )
        )
    }
});

PopularityBar.PropTypes = {
    popularity: React.PropTypes.number.isRequired
}

module.exports = PopularityBar;