package ua.com.fielden.platform.web.menu.module.impl;

import ua.com.fielden.platform.menu.ModuleMenuItem;

public class WebSubMenuItem {

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

    public ModuleMenuItem getMenuItem() {
        final ModuleMenuItem menuItem = new ModuleMenuItem();
        menuItem.setKey(title);
        menuItem.setDesc(description);
        if (view != null) {
            menuItem.setView(view.getView());
        }
        return menuItem;
    }
}
