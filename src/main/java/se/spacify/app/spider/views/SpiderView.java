package se.spacify.app.spider.views;

import se.spacify.app.spider.Request;
import se.spacify.app.spider.Spider;
import se.spacify.controls.XUL;

public class SpiderView extends XUL {
    private Spider spider = new Spider();
    public Spider getSpider() {
        return spider;
    }
    public SpiderView() {
        super();
    }
    
    /**
     * Called back to the spider to post back to the spider controller that is attached to this view
     * @param request
     */
    public void request(Request request) {
        // TODO Implement postback, that can be triggered by a button click and attach input data in a Map<String, Object>
    }
}
