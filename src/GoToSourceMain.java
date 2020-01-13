import com.google.gson.Gson;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.progress.util.DispatchThreadProgressWindow;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.openapi.wm.ToolWindowManager;
import org.codehaus.jettison.json.JSONArray;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GoToSourceMain implements ProjectComponent {
    static List<String> prefix = new ArrayList<>();
    static RouteModel[] routes;
    static Project project;

    @NotNull
    @Override
    public String getComponentName() {
        return "GoToSourceMain";
    }

    @Override
    public void initComponent() {
        ProjectManager.getInstance().addProjectManagerListener(new ProjectManagerListener() {
            @Override
            public void projectOpened(@NotNull Project project) {
                GoToSourceMain.project = project;
                PrefixParser.parseConfigurationSection(project);
                GoToSourceMain.prefix.forEach(System.out::println);

                JSONArray routes = RoutesParser.parseProject(project);
                System.out.println(routes);
                GoToSourceMain.routes = jsonToRouteModels(routes);
            }
        });

        addListenerToChromeMessages(43872);
    }

    private void addListenerToChromeMessages(int port) {
        System.out.println("port: " + port);
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        try {
                            if (serverSocket.isClosed()) {
                                return;
                            }
                            Socket socket = serverSocket.accept();
                            try {
                                BufferedReader reader = new BufferedReader(
                                        new InputStreamReader(socket.getInputStream(), "UTF-8"));
                                StringBuilder builder = new StringBuilder();
                                int c;
                                while ((c = reader.read()) >= 0 && reader.ready()) {
                                    builder.append((char) c);
                                }
                                String postRequest = builder.toString();
                                String url = postRequest.substring(postRequest.lastIndexOf('\n'));
                                int slashNb = 0;
                                for (int i = 0; i < url.length(); i++) {
                                    if (url.charAt(i) == '/' && ++slashNb >= 3) {
                                        url = url.substring(i);
                                        break;
                                    }
                                }
                                searchComponentByURL(cleanURL(url));
                            } finally {
                                socket.close();
                            }
                        } catch (Throwable ignored) {
                            System.out.println("++++++++++++++");
                            ignored.printStackTrace();
                        }
                    }
                }
            }).start();
        } catch (BindException ex) {
            addListenerToChromeMessages(port + 1);
        } catch (Exception e) {
            System.out.println("error: " + e);
        }
    }


    private String[] cleanURL(String url) {
        int index = url.indexOf('?');
        if (index > 0) {
            url = url.substring(0, index);
        }
        ArrayList<String> urlParts = slashSplit(url);
        int cutSlash = 0;
        for (String p : prefix) {
            ArrayList<String> ll = slashSplit(p);
            if (ll.size() > urlParts.size()) break;
            boolean isTheRightPrefix = false;

            for (int i = 0; i < ll.size(); i++) {
                if (urlParts.get(i).equals(ll.get(i))) {
                    isTheRightPrefix = true;
                    cutSlash++;
                } else {
                    isTheRightPrefix = false;
                    cutSlash = 0;
                }
            }
            if (isTheRightPrefix) break;
        }
        for (int i = 0; i < cutSlash; i++) {
            urlParts.remove(i);
        }
        return urlParts.toArray(new String[prefix.size()]);
    }

    @NotNull
    private ArrayList<String> slashSplit(String url) {
        ArrayList<String> urlParts = new ArrayList<>();
        StringBuilder part = new StringBuilder();
        for (int i = 0; i < url.length(); i++) {
            if (url.charAt(i) == '/') {
                if (part.length() > 0) {
                    urlParts.add(part.toString());
                    part = new StringBuilder();
                }
            } else {
                part.append(url.charAt(i));
            }
        }
        if (part.length() > 0) urlParts.add(part.toString());
        return urlParts;
    }

    private void searchComponentByURL(String[] paths) {
        try {
            for (Object s : paths) {
                System.out.print(s + " || ");
            }
            System.out.println();

            String path = getComponentPathRecursively(routes, paths, 0);
            if (path != null)
                openComponent(path);
            System.out.println(path);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static RouteModel[] jsonToRouteModels(JSONArray routes) {
        Gson gson = new Gson();
        return gson.fromJson(routes.toString(), RouteModel[].class);
    }

    private String searchEmptyPath(RouteModel[] routes) {
        for (int i = 0; i < routes.length; i++) {
            if (routes[i].getComponentPath() != null)
                return routes[i].getComponentPath();
            else if (routes[i].getPath().isEmpty()) return searchEmptyPath(routes[i].getChildren());
        }
        return null;
    }

    private String getComponentPathRecursively(RouteModel[] routes, String[] paths, int index) {
        if (routes == null) return null;
        if (paths.length - 1 < index) {
            String result = searchEmptyPath(routes);
            if (result != null) return result;
        } else
            for (RouteModel route : routes) {
                if (route.getPath().isEmpty() && route.getChildren() != null) {
                    String componentPath = getComponentPathRecursively(route.getChildren(), paths, index);
                    if (componentPath != null) return componentPath;
                } else if (route.getPath().equals(paths[index])) {
                    if (paths.length - 1 == index && route.getComponentPath() != null) {
                        String result = route.getComponentPath();
                        if (result != null) return result;
                    }
                    String componentPath = getComponentPathRecursively(route.getChildren(), paths, index + 1);
                    if (componentPath != null) return componentPath;
                } else if (route.getPath().contains("/") || route.getPath().contains(":")) {
                    String[] subPaths = route.getPath().split("/");
                    boolean isMatching = false;
                    for (int i = 0; i < subPaths.length; i++) {
                        if (subPaths[i].startsWith(":") || subPaths[i].equals(paths[index])) {
                            index++;
                            isMatching = true;
                        }
                    }
                    if (isMatching) {
                        if (paths.length - 1 < index && route.getComponentPath() != null) {
                            String result = route.getComponentPath();
                            if (result != null) return result;
                        }
                        String result = getComponentPathRecursively(route.getChildren(), paths, index);
                        if (result != null) return result;
                    }
                }
            }
        return null;
    }

    private void openComponent(String path) {
        VirtualFile target = LocalFileSystem.getInstance().findFileByPath(path);
        ApplicationManager.getApplication().invokeLater(() -> {
            final ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(ToolWindowId.PROJECT_VIEW);
            toolWindow.activate(() -> {
                System.out.println("activate");
                final ProjectView projectView = ProjectView.getInstance(project);
                System.out.println(projectView.toString());
                projectView.selectCB(null, target, false);
            });
        });
    }
}
