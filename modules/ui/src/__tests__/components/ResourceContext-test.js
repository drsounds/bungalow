jest.dontMock('../../components/resource_context.js');
jest.dontMock('react');
describe('Link', function() {
    it('presents a link', function () {
        var React = require('react/addons');
        var ResourceContext = require('../../components/resource_context.js');

        var resources = [
            {
                uri: 'bungalow:resource:2FORU',
                authors:[
                    {
                        uri: 'bungalow:author:test',
                        name: 'Test',
                    }
                ],
                duration: 60 * 3,
                album: {
                    name: 'Test Album',
                    uri: 'bungalow:album:1251215'
                },
                added_by: {
                    id: 'drsounds',
                    uri: 'bungalow:user:drsounds',
                    name: 'Dr. Sounds'
                }
            }
        ];

        var fields = ['name', 'authors', 'album', 'duration']

        var TestUtils = React.addons.TestUtils;

        // Render a checkbox with label in the document
        var resource_context = TestUtils.renderIntoDocument(
            <ResourceContext resources={resources} columnHeaders={fields} />
        );
        // Verify that it's Off by default
        var table = TestUtils.findRenderedDOMComponentWithTag(
          resource_context, 'table');
        expect(React.findDOMNode(table).innerHTML).toContain('Dr. Sounds');
    });
});