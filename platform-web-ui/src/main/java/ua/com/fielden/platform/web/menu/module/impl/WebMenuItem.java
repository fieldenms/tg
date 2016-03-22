package ua.com.fielden.platform.web.menu.module.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.web.interfaces.IExecutable;
import ua.com.fielden.platform.web.minijs.JsCode;

public class WebMenuItem implements IExecutable {

    private final String title;
    private String description;
    private WebView view;

    private final List<WebSubMenuItem> subItems = new ArrayList<>();

    public WebMenuItem(final String title) {
        this.title = title;
    }

    public void description(final String description) {
        this.description = description;
    }

    public void view(final WebView view) {
        this.view = view;
    }

    public WebSubMenuItem addSubMenuItem(final String title) {
        final WebSubMenuItem subMenuItem = new WebSubMenuItem(title);
        subItems.add(subMenuItem);
        return subMenuItem;
    }

    @Override
    public JsCode code() {
        final String code = "{ title: \"" + this.title + "\", " +
                "description: \"" + this.description + "\"" +
                (this.subItems.size() > 0 ? ", submenu: [" + StringUtils.join(subItems, ",") + "]" : "") +
                (this.view != null ? ", view: " + view.code() : "") +
                "}";
        return new JsCode(code);
    }

    @Override
    public String toString() {
        return code().toString();
    }
}
