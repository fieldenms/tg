package ua.com.fielden.platform.web.menu.module.impl;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.menu.Module;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;

public class WebMenuModule {

    public final String title;

    private String description;
    private String bgColor;
    private String captionBgColor;
    private String icon;
    private String detailIcon;
    private WebMenu menu;
    private WebView view;
    private List<EntityActionConfig> actions = new ArrayList<>();

    public WebMenuModule(final String title) {
        this.title = title;
    }

    public WebMenuModule description(final String description) {
        this.description = description;
        return this;
    }

    public WebMenuModule bgColor(final String bgColor) {
        this.bgColor = bgColor;
        return this;
    }

    public WebMenuModule captionBgColor(final String captionBgColor) {
        this.captionBgColor = captionBgColor;
        return this;
    }

    public WebMenuModule icon(final String icon) {
        this.icon = icon;
        return this;
    }

    public WebMenuModule detailIcon(final String detailIcon) {
        this.detailIcon = detailIcon;
        return this;
    }

    public WebMenu menu() {
        this.menu = new WebMenu();
        return this.menu;
    }

    public WebMenuModule view(final WebView view) {
        this.view = view;
        return this;
    }

    public WebMenuModule addAction(final EntityActionConfig action) {
        this.actions.add(action);
        return this;
    }

    public List<EntityActionConfig> getActions() {
        return actions;
    }

    public Module getModule() {
        final Module module = new Module().
                setBgColor(bgColor).
                setCaptionBgColor(captionBgColor).
                setIcon(icon).
                setDetailIcon(detailIcon).
                setKey(title).
                setDesc(description);
        //TODO module menu can not be null. Right now platform supports modules with view. This case should be covered with separate issue.
        if (this.menu != null) {
            module.setMenu(menu.getMenu());
        } else if (view != null) {
            module.setView(view.getView());
        }
        return module;
    }
}
