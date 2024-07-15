package ua.com.fielden.platform.web.menu.module.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ua.com.fielden.platform.menu.ModuleMenuItem;

public class WebMenuItem {

    private final String title;
    private String description;
    private String icon;
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

    public void icon(final String icon) {
        this.icon = icon;
    }

    public WebSubMenuItem addMenuItem(final String title) {
        final WebSubMenuItem subMenuItem = new WebSubMenuItem(title);
        subItems.add(subMenuItem);
        return subMenuItem;
    }

    public ModuleMenuItem getModuleMenuItem() {
        final ModuleMenuItem menuItem = new ModuleMenuItem();
        menuItem.setKey(title);
        menuItem.setDesc(description);
        menuItem.setIcon(icon);
        if (view != null) {
            menuItem.setView(view.getView());
        } else if (subItems != null) {
            menuItem.setMenu(subItems.stream().map(subItem -> subItem.getMenuItem()).collect(Collectors.toList()));
        }
        return menuItem;
    }
}
