package com.adhess.it.org.model;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.HashSet;

public class ComponentModel extends RoutingModuleModel {
    private String componentPath;
    private String selector;
    private HashSet<String> relatedComponentSelectorName = new HashSet<>();

    public ComponentModel() {
    }

    public ComponentModel(String componentPath, String selector) {
        this.componentPath = componentPath;
        this.selector = selector;
    }

    public String getComponentPath() {
        return componentPath;
    }

    public void setComponentPath(String componentPath) {
        this.componentPath = componentPath;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public HashSet<String> getRelatedComponentSelectorName() {
        return relatedComponentSelectorName;
    }

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("componentPath", componentPath);
            jsonObject.put("selector", selector);
            if (relatedComponentSelectorName.size() > 0)
                jsonObject.put("relatedComponents", relatedComponentSelectorName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
}
