package se.spacify.app.create.controller;

import java.util.HashMap;
import java.util.Map;

import se.spacify.app.spider.Request;
import se.spacify.app.spider.controller.Controller;
import se.spacify.net.Uri;

public class CreateController extends Controller {

    @Override
    public boolean acceptsUri(Uri uri) {
        // TODO Auto-generated method stub
        return uri != null && uri.toString().startsWith("spacify:create");
    }

    @Override
    protected String template(Request request) {
        // TODO Auto-generated method stub
        return loadTemplateFromResource("/se/spacify/app/create/views/create.xml");
    }

    @Override
    protected Map<String, Object> data(Request request) {
        // TODO Auto-generated method stub
        String prompt = (String)request.getPost("prompt", null);
        if (request.getMethod().equals("POST")) {
             // TODO Implement the logic to handle the POST request and create a new app based on the prompt.
        
        }
        return new HashMap<String,Object>() {
            {
                put("prompt", prompt);
            }
        };
    }
    
}
