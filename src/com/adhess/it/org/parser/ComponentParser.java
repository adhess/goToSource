package com.adhess.it.org.parser;

import com.adhess.it.org.GoToSourceMain;
import com.adhess.it.org.model.ComponentModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class ComponentParser {
    public static void parseComponent(Project project) {
        Collection<ComponentModel> components = new ArrayList<>();
        for (String fileName : FilenameIndex.getAllFilenames(project)) {
            if (fileName.contains(".component.ts")) {
                Collection<VirtualFile> filesCollection = FilenameIndex.getVirtualFilesByName(project, fileName, GlobalSearchScope.allScope(project)
                );
                for (VirtualFile virtualFile : filesCollection) {
                    String path = virtualFile.getPresentableUrl();
                    if (path.contains(".component.ts")) {
                        PsiFile file = PsiManager.getInstance(project).findFile(virtualFile);
                        if (file != null && file.getText().contains("selector"))
                            for (PsiElement a = file.getFirstChild(); a != null; a = a.getNextSibling()) {
                                if (a.getClass().getSimpleName().equals("TypeScriptClassImpl")) {
                                    PsiElement h = a.getFirstChild();
                                    if (h != null) {
                                        PsiElement k = h.getFirstChild();
                                        if (k != null) {
                                            PsiElement j = k.getFirstChild();
                                            for (PsiElement b = j; b != null; b = b.getNextSibling()) {
                                                if (b.getClass().getSimpleName().equals("JSCallExpressionImpl")) {
                                                    for (PsiElement c = b.getFirstChild(); c != null; c = c.getNextSibling()) {
                                                        if (c.getClass().getSimpleName().equals("JSArgumentListImpl")) {
                                                            for (PsiElement d = c.getFirstChild(); d != null; d = d.getNextSibling()) {
                                                                if (d.getClass().getSimpleName().equals("JSObjectLiteralExpressionImpl")) {
                                                                    for (PsiElement e = d.getFirstChild(); e != null; e = e.getNextSibling()) {
                                                                        if (e.getClass().getSimpleName().equals("JSPropertyImpl") && e.getFirstChild().getText().equals("selector")) {
                                                                            for (PsiElement f = e.getFirstChild(); f != null; f = f.getNextSibling()) {
                                                                                if (f.getClass().getSimpleName().equals("JSLiteralExpressionImpl")) {

                                                                                    String selector = f.getText();
                                                                                    selector = selector.substring(1, selector.length() - 1);
                                                                                    ComponentModel component = new ComponentModel(path, selector);
                                                                                    String htmlComponentPath = path.substring(0, path.lastIndexOf('.')) + ".html";
                                                                                    VirtualFile htmlCPath = LocalFileSystem.getInstance().findFileByPath(htmlComponentPath);
                                                                                    if (htmlCPath != null) {
//                                                                                        System.out.println(selector + " > " + path + " > " + htmlCPath.getPresentableUrl());
                                                                                        String htmlComponent = PsiManager.getInstance(project).findFile(htmlCPath).getText();
                                                                                        HashSet<String> relatedComponentSelectorName = component.getRelatedComponentSelectorName();
                                                                                        for (int i = 0; i < htmlComponent.length(); i++) {
                                                                                            if (htmlComponent.charAt(i) == '<' && i + 1 < htmlComponent.length() && htmlComponent.charAt(i + 1) != '/') {
                                                                                                int indexStart = i + 1;
                                                                                                while (++i < htmlComponent.length()
                                                                                                        && htmlComponent.charAt(i) != ' '
                                                                                                        && htmlComponent.charAt(i) != '\r'
                                                                                                        && htmlComponent.charAt(i) != '>'
                                                                                                        && htmlComponent.charAt(i) != '\n')
                                                                                                    ;
                                                                                                String htmlSelector = htmlComponent.substring(indexStart, i);
                                                                                                relatedComponentSelectorName.add(htmlSelector);
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                    components.add(component);
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                }
                            }
                    }
                }
            }
        }

        for (ComponentModel component : components) {
            ArrayList<String> toDelete = new ArrayList<>();
            for (String s : component.getRelatedComponentSelectorName()) {
                if (!isComponentSelector(components, s)) {
                    toDelete.add(s);
                }
            }
            for (String s : toDelete) {
                component.getRelatedComponentSelectorName().remove(s);
            }
        }

        GoToSourceMain.componentsData = components;
    }

    private static boolean isComponentSelector(Collection<ComponentModel> components, String s) {
        for (ComponentModel component : components) {
            if (component.getSelector().equals(s)) return true;
        }
        return false;
    }

}
