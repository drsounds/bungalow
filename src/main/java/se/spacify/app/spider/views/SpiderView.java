package se.spacify.app.spider.views;

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
}
