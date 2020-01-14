package com.adhess.it.org.parser;

import com.adhess.it.org.GoToSourceMain;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;

import java.util.*;

public class PrefixParser {
    public static void parseConfigurationSection(Project project) {
        Collection<VirtualFile> filesCollection = FilenameIndex
                .getVirtualFilesByName(project, "angular.json", GlobalSearchScope.allScope(project));
        for (VirtualFile file : filesCollection) {
            PsiFile ff = PsiManager.getInstance(project).findFile(file);
            for (PsiElement c = ff.getFirstChild(); c != null; c = c.getNextSibling()) {
                if (c.getClass().getSimpleName().equals("JsonObjectImpl")) {
                    try {
                        String defaultProjectName = null;
                        for (PsiElement defaultProject = c.getFirstChild(); defaultProject != null; defaultProject = defaultProject.getNextSibling()) {
                            if (defaultProject.getClass().getSimpleName().equals("JsonPropertyImpl") && defaultProject.getFirstChild().getText().equals("\"defaultProject\"")) {
                                defaultProjectName = defaultProject.getLastChild().getText();
                            }
                        }
                        for (PsiElement projectSection = c.getFirstChild(); projectSection != null; projectSection = projectSection.getNextSibling()) {
                            if (projectSection.getClass().getSimpleName().equals("JsonPropertyImpl") && projectSection.getFirstChild().getText().equals("\"projects\"")) {
                                for (PsiElement obj = projectSection.getFirstChild(); obj != null; obj = obj.getNextSibling()) {
                                    if (obj.getClass().getSimpleName().equals("JsonObjectImpl")) {
                                        for (PsiElement property = obj.getFirstChild(); property != null; property = property.getNextSibling()) {
                                            if (property.getClass().getSimpleName().equals("JsonPropertyImpl") && property.getFirstChild().getText().equals(defaultProjectName)) {
                                                // inside defaultNameProject Object, searching for the architect section
                                                for (PsiElement m = property.getLastChild().getFirstChild(); m != null; m = m.getNextSibling()) {
                                                    if (m.getClass().getSimpleName().equals("JsonPropertyImpl") && m.getFirstChild().getText().equals("\"architect\"")) {
                                                        for (PsiElement n = m.getLastChild().getFirstChild(); n != null; n = n.getNextSibling()) {
                                                            if (n.getClass().getSimpleName().equals("JsonPropertyImpl") && n.getFirstChild().getText().equals("\"build\"")) {
                                                                // searching for the build section
                                                                for (PsiElement o = n.getLastChild().getFirstChild(); o != null; o = o.getNextSibling()) {
                                                                    if (o.getClass().getSimpleName().equals("JsonPropertyImpl") && o.getFirstChild().getText().equals("\"configurations\"")) {
                                                                        for (PsiElement p = o.getLastChild().getFirstChild(); p != null; p = p.getNextSibling()) {
                                                                            if (p.getClass().getSimpleName().equals("JsonPropertyImpl")) {
                                                                                // searching for the baseHref property
                                                                                for (PsiElement q = p.getLastChild().getFirstChild(); q != null; q = q.getNextSibling()) {
                                                                                    if (q.getClass().getSimpleName().equals("JsonPropertyImpl")  && q.getFirstChild().getText().equals("\"baseHref\"")) {
                                                                                        String baseHref = q.getLastChild().getText();
                                                                                        String basehref = baseHref.substring(1, baseHref.length() - 1);
                                                                                        if (!GoToSourceMain.prefix.contains(basehref)) {
                                                                                            GoToSourceMain.prefix.add(basehref);
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

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        GoToSourceMain.prefix.sort((a, b) -> b.length() - a.length());
    }
}
