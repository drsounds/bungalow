package se.spacify.app.spider.controller;

import org.w3c.dom.Element;

import se.spacify.app.spider.Request;
import se.spacify.app.spider.template.TemplateEngine;
import se.spacify.net.Uri;

/**
 * Handles the Spider requests it {@link #acceptsUri accepts}, turning each into a
 * control tree. A controller supplies a Lua-preprocessor template (from a file,
 * resource or string) via {@link #template(Request)}; {@link #process(Request)}
 * renders it with the {@link TemplateEngine} — interpolating {@code ${...}}
 * expressions and running {@code %}-directives — and returns the resulting
 * {@link Element} tree for the {@link se.spacify.app.spider.views.SpiderView} to mount.
 *
 * <p>Postbacks re-enter the same controller: a button {@code onclick="refresh"}
 * sends a {@code POST} whose {@code action} header is {@code "refresh"} (readable
 * in the template as the {@code action} global), so a single template can branch on
 * what was clicked and re-render.
 *
 * <pre>{@code
 *   class OverviewController extends Controller {
 *       public boolean acceptsUri(Uri uri) { return "spacify".equals(uri.getScheme()); }
 *       protected String template(Request request) {
 *           return """
 *               <view>
 *                 <page id="overview" title="Overview">
 *                   <text>${os.date("%Y-%m-%d")}</text>
 *                   % for i,10 do
 *                   <text>${i}</text>
 *                   % end
 *                   <button onclick="refresh">Refresh</button>
 *                 </page>
 *               </view>""";
 *       }
 *   }
 * }</pre>
 */
public abstract class Controller {

    public abstract boolean acceptsUri(Uri uri);

    /**
     * The Lua-preprocessor template for this request — loaded from a file, classpath
     * resource or built inline. See the class javadoc for the directive syntax.
     */
    protected abstract String template(Request request);

    /** Render this controller's {@link #template(Request) template} against the request. */
    public Element process(Request request) {
        return TemplateEngine.render(template(request), request);
    }
}
