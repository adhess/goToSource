public class RouteModel {
    private String path;
    private String componentPath;
    private String redirectTo;
    private RouteModel[] children;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getComponentPath() {
        return componentPath;
    }

    public void setComponentPath(String componentPath) {
        this.componentPath = componentPath;
    }


    public String getRedirectTo() {
        return redirectTo;
    }

    public void setRedirectTo(String redirectTo) {
        this.redirectTo = redirectTo;
    }

    public RouteModel[] getChildren() {
        return children;
    }

    public void setChildren(RouteModel[] children) {
        this.children = children;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("{\n" +
                "\"path\":\"" + path + '\"' +
                ", \n\"componentPath\":\"" + componentPath + '\"' +
                ", \n\"redirectTo\":\"" + redirectTo + '\"' +
                ", \n\"children\": [\n");
        if (children != null)
        for (int i = 0; i < children.length; i++) {
            s.append(children[i].toString());
            if (i != children.length - 1) {
                s.append(",\n");
            }
        }
        return s +
                "]\n}";
    }
}
