package com.adhess.it.org.action;

import com.adhess.it.org.GoToSourceMain;
import com.adhess.it.org.parser.ComponentParser;
import com.adhess.it.org.parser.PrefixParser;
import com.adhess.it.org.parser.RoutesParser;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.Messages;
import org.codehaus.jettison.json.JSONArray;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

public class Refresh extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        ProgressManager.getInstance().run(
                new Task.Modal(e.getProject(), "Parse Tree", true) {
                    @Override
                    public void run(@NotNull ProgressIndicator progressIndicator) {
                        progressIndicator.setIndeterminate(true);
                        AtomicReference<JSONArray> routes = new AtomicReference<>();
                        ReadAction.run(() -> {
                            PrefixParser.parseConfigurationSection(e.getProject());
                            GoToSourceMain.prefix.forEach(System.out::println);

                            routes.set(RoutesParser.parseProject(e.getProject()));
                            System.out.println(routes);
                            GoToSourceMain.routes = GoToSourceMain.jsonToRouteModels(routes.get());
                            progressIndicator.setText("Parse Prefix");
                            PrefixParser.parseConfigurationSection(e.getProject());
                            System.out.println(GoToSourceMain.prefix);
                            progressIndicator.setText("Parse Components");
                            ComponentParser.parseComponent(e.getProject());
                            System.out.println(GoToSourceMain.componentsData);

                            ApplicationManager.getApplication().invokeLater(() -> {
                                String message = "\n-----------------------\nRoutes\n-----------------------\n"
                                        + routes.toString()
                                        + "\n-----------------------\nComponents\n-----------------------\n"
                                        + GoToSourceMain.componentsData.toString()
                                        + "\n-----------------------\nPrefix\n-----------------------\n"
                                        + GoToSourceMain.prefix.toString();

                                Messages.showMessageDialog(message, "Route Tree", Messages.getInformationIcon());
                            });
                        });
                    }
                }
        );
    }
}
