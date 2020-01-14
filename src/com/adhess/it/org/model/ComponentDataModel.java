package com.adhess.it.org.model;

import com.intellij.openapi.vfs.VirtualFile;

import java.util.ArrayList;
import java.util.HashSet;

public class ComponentDataModel {
    private String path;
    private String selector;
    private HashSet<String> relatedComponentSelectorName = new HashSet<>();

    public ComponentDataModel() {
    }

    public ComponentDataModel(String path, String selector) {
        this.path = path;
        this.selector = selector;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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
        StringBuilder str = new StringBuilder(selector);
        if (relatedComponentSelectorName.size() > 0) {
            str.append(" > [");
            for (String s : relatedComponentSelectorName) {
                str.append(s).append(',');
            }
            str.append("]");
        }
        return str.toString();
    }
}
