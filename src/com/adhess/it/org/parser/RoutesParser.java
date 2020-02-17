package com.adhess.it.org.parser;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.Collection;

public class RoutesParser {
    /**
     * parse the project to get the router tree
     * A --> B
     * \-> C --> M
     * \-> N
     *
     * @return router tree
     */
    public static JSONArray parseProject(Project project) {
        Collection<VirtualFile> filesCollection = FilenameIndex
                .getVirtualFilesByName(project, "app.module.ts", GlobalSearchScope.allScope(project));
        for (VirtualFile file : filesCollection) {
            if (file.getPresentableUrl().contains("node_module")) continue;
            PsiFile ff = PsiManager.getInstance(project).findFile(file);
            for (PsiElement c = ff.getFirstChild(); c != null; c = c.getNextSibling()) {
                if (c.getClass().getSimpleName().equals("TypeScriptClassImpl")) {
                    try {
                        return extractRoutesFromTypeScriptClassImpl(c);
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    private static JSONArray parseJSArray(PsiElement c) throws JSONException {
        JSONArray ll = new JSONArray();
        for (PsiElement child = c.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getClass().getSimpleName().equals("JSObjectLiteralExpressionImpl")) {
                JSONObject rm = parseJSObject(child);
                if (rm.has("path"))
                    ll.put(rm);
            }
        }
        return ll;
    }

    private static JSONObject parseJSObject(PsiElement c) throws JSONException {
        JSONObject rm = new JSONObject();
        for (PsiElement child = c.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getClass().getSimpleName().equals("JSPropertyImpl")) {
                parseJSProperty(child, rm);
            }
        }
        return rm;
    }

    private static void parseJSProperty(PsiElement child, JSONObject rm) throws JSONException {
        switch (child.getFirstChild().getText()) {
            case "path":
                for (PsiElement c = child.getFirstChild(); c != null; c = c.getNextSibling()) {
                    if (c.getClass().getSimpleName().equals("JSLiteralExpressionImpl")) {
                        String text = c.getText();
                        rm.put("path", text.substring(1, text.length() - 1));
                    }
                }
                break;
            case "component":
                PsiElement component = null;
                for (PsiElement c = child.getFirstChild(); c != null; c = c.getNextSibling()) {
                    if (c.getClass().getSimpleName().equals("JSReferenceExpressionImpl")) {
                        component = c;
                    }
                }
                assert component != null;
                PsiReference reference = component.getReference();
                assert reference != null;
                PsiElement componentClass = reference.resolve();
                assert componentClass != null;
                rm.put("componentPath", componentClass.getContainingFile().getVirtualFile().getPresentableUrl());
                break;
            case "children":
                for (PsiElement c = child.getFirstChild(); c != null; c = c.getNextSibling()) {
                    if (c.getClass().getSimpleName().equals("JSArrayLiteralExpressionImpl")) {
                        JSONArray ll = parseJSArray(c);
                        addAllObjectToChildren(rm, ll);
                    }
                }
                break;
            case "loadChildren":
                for (PsiElement c = child.getFirstChild(); c != null; c = c.getNextSibling()) {
                    if (c.getClass().getSimpleName().equals("TypeScriptFunctionExpressionImpl")) {
                        for (PsiElement a = c.getFirstChild(); a != null; a = a.getNextSibling()) {
                            if (a.getClass().getSimpleName().equals("JSCallExpressionImpl")) {
                                for (PsiElement b = a.getFirstChild(); b != null; b = b.getNextSibling()) {
                                    if (b.getClass().getSimpleName().equals("JSArgumentListImpl")) {
                                        for (PsiElement d = b.getFirstChild(); d != null; d = d.getNextSibling()) {
                                            if (d.getClass().getSimpleName().equals("TypeScriptFunctionExpressionImpl")) {
                                                for (PsiElement e = d.getFirstChild(); e != null; e = e.getNextSibling()) {
                                                    if (e.getClass().getSimpleName().equals("JSReferenceExpressionImpl")) {
                                                        assert e.getReference() != null;
                                                        PsiElement resolve = e.getReference().resolve();
                                                        addAllObjectToChildren(rm, extractRoutesFromTypeScriptClassImpl(resolve));
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
                break;
            case "redirectTo":
                for (PsiElement c = child.getFirstChild(); c != null; c = c.getNextSibling()) {
                    if (c.getClass().getSimpleName().equals("JSLiteralExpressionImpl")) {
                        String text = c.getText();
                        rm.put("redirectTo", text.substring(1, text.length() - 1));
                    }
                }
                break;
        }

    }

    private static void addAllObjectToChildren(JSONObject rm, JSONArray jsonArray) throws JSONException {
        JSONArray children = null;
        try {
            children = (JSONArray) rm.get("children");
            putAllObject(children, jsonArray);
        } catch (JSONException e) {
            rm.put("children", jsonArray);
        }
    }

    private static JSONArray extractRoutesFromTypeScriptClassImpl(PsiElement resolve) throws JSONException {
        JSONArray lrm = new JSONArray();
        String str = resolve.getText();
        if (str.contains("@NgModule") && str.contains("imports")) {
            PsiElement jsProperty = getFirstJsPropertyOfNgModuleDecorator(resolve);
            if (jsProperty != null) {
                PsiElement importProperty = null;
                for (PsiElement f = jsProperty.getFirstChild(); f != null; f = f.getNextSibling()) {
                    if (f.getClass().getSimpleName().equals("JSPropertyImpl")) {
                        if (f.getFirstChild().getText().equals("imports")) {
                            importProperty = f;
                        }
                    }
                }
                PsiElement importArray = null;
                for (PsiElement f = importProperty.getFirstChild(); f != null; f = f.getNextSibling()) {
                    if (f.getClass().getSimpleName().equals("JSArrayLiteralExpressionImpl")) {
                        importArray = f;
                    }
                }
                extractImportJsArray(lrm, importArray);
            }
        }

        return lrm;
    }

    private static void extractImportJsArray(JSONArray lrm, PsiElement importArray) throws JSONException {
        for (PsiElement f = importArray.getFirstChild(); f != null; f = f.getNextSibling()) {
            if (f.getClass().getSimpleName().equals("JSCallExpressionImpl")) {
                putAllObject(lrm, extractRoutesFromJsCallExpression(f));
            } else if (f.getClass().getSimpleName().equals("JSReferenceExpressionImpl")) {
                putAllObject(lrm, extractRoutesFromTypeScriptClassImpl(f.getReference().resolve()));
            } else if (f.getClass().getSimpleName().equals("JSSpreadExpressionImpl")) {
                PsiElement subImportArray = f.getLastChild().getReference().resolve().getLastChild();
                extractImportJsArray(lrm, subImportArray);
            }
        }
    }

    private static void putAllObject(JSONArray lrm, JSONArray jsonArray) throws JSONException {
        for (int i = 0; i < jsonArray.length(); i++) {
            lrm.put(jsonArray.get(i));
        }
    }

    private static PsiElement getFirstJsPropertyOfNgModuleDecorator(PsiElement resolve) {
        try {
            PsiElement firstChild = resolve.getFirstChild();
            PsiElement firstChild1 = firstChild.getFirstChild();
            PsiElement nextSibling = firstChild1.getFirstChild().getNextSibling();
            PsiElement lastChild = nextSibling.getLastChild();
            return lastChild.getFirstChild().getNextSibling();
        } catch (Exception ex) {
            return null;
        }
    }

    private static JSONArray extractRoutesFromJsCallExpression(PsiElement jsCallExpression) throws JSONException {
        JSONArray ll = new JSONArray();
        PsiElement JsReferenceExpression = jsCallExpression.getFirstChild();
        String firstChild = JsReferenceExpression.getFirstChild().getText();
        String lastChild = JsReferenceExpression.getLastChild().getText();
        if (lastChild.equals("forChild") || lastChild.equals("forRoot")) {
            if (firstChild.equals("RouterModule")) {
                PsiElement JsArgumentList = jsCallExpression.getLastChild();
                for (PsiElement a = JsArgumentList.getFirstChild(); a != null; a = a.getNextSibling()) {
                    if (a.getClass().getSimpleName().equals("JSArrayLiteralExpressionImpl")) {
                        putAllObject(ll, parseJSArray(a));
                    } else if (a.getClass().getSimpleName().equals("JSReferenceExpressionImpl")) {
                        for (PsiElement c = a.getReference().resolve().getFirstChild(); c != null; c = c.getNextSibling()) {
                            if (c.getClass().getSimpleName().equals("JSArrayLiteralExpressionImpl")) {
                                putAllObject(ll, parseJSArray(c));
                            }
                        }
                    }
                }
            } else {
                JSONArray rm = extractRoutesFromTypeScriptClassImpl(JsReferenceExpression.getFirstChild().getReference().resolve());
                putAllObject(ll, rm);
            }
        }
        return ll;
    }

}
