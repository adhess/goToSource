package com.adhess.it.org.action;

import com.adhess.it.org.GoToSourceMain;
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
                            ApplicationManager.getApplication().invokeLater(() -> {
                                Messages.showMessageDialog(routes.toString(), "Route Tree", Messages.getInformationIcon());
                            });
                        });
                    }
                }
        );
    }
}