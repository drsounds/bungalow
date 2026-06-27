package se.spacify.app.spider.controller;

import se.spacify.app.spider.Request;
import se.spacify.net.Uri;

/**
 * Takes a request and format it into a response, accoring to preprocessing a LUA template
 * eg.  from registered Spider handlers
 * @param request
 * @return
 */
public abstract class Controller {
    public abstract boolean acceptsUri(Uri uri);
    /**
     * Processes the request body with the xml lua preprocessor template, let LUA preprocess it and return it as org.w3c.dom.Element
     * @param request
     * @return 
     * It will take a template from file or whatever, abstract to implement
     * The returned String is a LUA preprocessor template which could look like this
     * {@code
     *  <view>
     *      <page id="overview" title="Overview">
     *          <text>${os.date("%Y-%m-D")}</text>
     *          % for i,10 do 
     *          <text>${i}</text>
     *          % end
     *          <button onclick="refresh">Refresh</button>
     *      </page>
     *  </view>}
     * @return Element tree
     */
    
    public org.w3c.dom.Element process(Request request) {
        // TODO Should process the request body with the xml lua preprocessor template, let LUA preprocess it and return it as org.w3c.dom.Element
        return null;
    }
}
