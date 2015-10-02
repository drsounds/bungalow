jest.dontMock('../../components/link.js');
jest.dontMock('react');
describe('Link', function() {
    it('presents a link', function () {
        var React = require('react/addons');
        var Link = require('../../components/link.js');

        var TestUtils = React.addons.TestUtils;

        // Render a checkbox with label in the document
        var link = TestUtils.renderIntoDocument(
            <Link uri="bungalow:test">test</Link>
        );
        // Verify that it's Off by default
        var label = TestUtils.findRenderedDOMComponentWithTag(
          link, 'a');
        expect(React.findDOMNode(label).textContent).toEqual('test');
    });
});