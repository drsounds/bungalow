package se.spacify.app.spider;

import java.util.ArrayList;
import java.util.List;

import se.spacify.app.spider.controller.Controller;

/***
 * 
 * A template application based on LUA preprocessor that takes a preprocessing like
 * {@code
 * <view>
 *      <page id="overview" title="Overview">
 *          <text>${os.date("%Y-%m-D")}</text>
 *          % for i,10 do 
 *          <text>${i}</text>
 *          % end
 *          <button onclick="refresh">Refresh</button>
 *      </page>
 *  </view>} should in processs() function p
 * 
 * where refresh is
 * 
*/
public class Spider {
    private List<Controller> controllers = new ArrayList<>();
    public List<Controller> getControllers() {
        return controllers;
    }

    public class Response {
        private org.w3c.dom.Element xml;
        private String text;

        public String getText() {
            return text;
        }

        public org.w3c.dom.Element getXml() {
            return xml;
        }
        private Request request;

        public Request getRequest() {
            return request;
        }

    }

    /**
     * Takes a request and format it into a response, accoring to preprocessing a LUA template
     * eg. {@code
     *  <view>
     *      <page id="overview" title="Overview">
     *          <text>${os.date("%Y-%m-D")}</text>
     *          % for i,10 do 
     *          <text>${i}</text>
     *          % end
     *          <button onclick="refresh">Refresh</button>
     *      </page>
     *  </view>} from registered Spider handlers, iterate through the registered controllers, check if it accepts request uri spacify:* and if so process that request
     * @param request
     * @return
     */
    public org.w3c.dom.Element process(Request request) {
        return null;
    }
}
