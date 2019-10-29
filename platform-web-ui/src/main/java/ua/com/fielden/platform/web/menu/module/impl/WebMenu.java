package ua.com.fielden.platform.web.menu.module.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ua.com.fielden.platform.menu.ModuleMenuItem;

public class WebMenu {

    List<WebMenuItem> menuItems = new ArrayList<>();

    public WebMenuItem addMenuItem(final String title) {
        final WebMenuItem menuItem = new WebMenuItem(title);
        menuItems.add(menuItem);
        return menuItem;
    }

    public List<ModuleMenuItem> getMenu() {
        return menuItems.stream().map(menuItem -> menuItem.getModuleMenuItem()).collect(Collectors.toList());
    }
}
