package ua.com.fielden.platform.web.menu.module.impl;

import ua.com.fielden.platform.web.interfaces.IExecutable;
import ua.com.fielden.platform.web.minijs.JsCode;

public class WebSubMenuItem implements IExecutable {

    private final String title;
    private String description;
    private WebView view;

    public WebSubMenuItem(final String title) {
        this.title = title;
    }

    public void description(final String description) {
        this.description = description;
    }

    public void view(final WebView view) {
        this.view = view;
    }

    @Override
    public JsCode code() {
        final String code = "{ title: \"" + this.title + "\", " +
                "description: \"" + this.description + "\"" +
                (this.view != null ? ", view: " + view.code() : "") +
                "}";
        return new JsCode(code);
    }

    @Override
    public String toString() {
        return code().toString();
    }
}
