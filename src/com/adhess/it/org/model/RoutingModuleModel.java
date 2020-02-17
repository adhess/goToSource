package com.adhess.it.org.model;

import java.util.ArrayList;

public class RoutingModuleModel {
    private ArrayList elementsParent = new ArrayList();
    private ArrayList elementsChildes = new ArrayList();
    private Object fileReference;

    public ArrayList<RoutingModuleModel> getElementsParent() {
        return elementsParent;
    }

    public ArrayList<RoutingModuleModel> getElementsChildes() {
        return elementsChildes;
    }

    public Object getFileReference() {
        return fileReference;
    }

    public void setFileReference(Object fileReference) {
        this.fileReference = fileReference;
    }
}
