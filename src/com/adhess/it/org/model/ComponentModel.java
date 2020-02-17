package com.adhess.it.org.model;

import com.google.gson.Gson;
import kotlin.Pair;

import java.util.ArrayList;
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
        ArrayList<Object> ll = new ArrayList<>();
        ll.add(new Pair<>("componentPath", componentPath));
        ll.add(new Pair<>("selector", selector));
        if (relatedComponentSelectorName.size() > 0)
            ll.add(new Pair<>("relatedComponentSelectorName", relatedComponentSelectorName));
        return new Gson().toJson(ll);
    }
}
