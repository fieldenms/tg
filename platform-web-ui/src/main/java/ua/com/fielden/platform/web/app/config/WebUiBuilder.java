package ua.com.fielden.platform.web.app.config;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.custom_view.AbstractCustomView;
import ua.com.fielden.platform.web.view.master.EntityMaster;

/**
 * Implementation of the {@link IWebUiBuilder}.
 *
 * @author TG Team
 *
 */
public class WebUiBuilder implements IWebUiBuilder {

    /**
     * The {@link IWebUiConfig} instance for which this configuration object was created.
     */
    private final IWebUiConfig webUiConfig;

    private int minDesktopWidth = 980, minTabletWidth = 768;
    private String locale = "en-AU";
    private String dateFormat = "DD/MM/YYYY";
    private String timeFormat = "h:mm A";

    /**
     * Holds the map between master's entity type and its master component.
     */
    private final Map<Class<? extends AbstractEntity<?>>, EntityMaster<? extends AbstractEntity<?>>> mastersMap = new LinkedHashMap<>();

    /**
     * Holds the map between entity centre's menu item type and entity centre.
     */
    private final Map<Class<? extends MiWithConfigurationSupport<?>>, EntityCentre<?>> centreMap = new LinkedHashMap<>();

    /**
     * Holds the map between custom view name and custom view instance.
     */
    private final Map<String, AbstractCustomView> viewMap = new LinkedHashMap<>();

    /**
     * Creates new instance of {@link WebUiBuilder} for the specified {@link IWebUiConfig} instance.
     *
     * @param webUiConfig
     */
    public WebUiBuilder(final IWebUiConfig webUiConfig) {
        this.webUiConfig = webUiConfig;
    }

    @Override
    public IWebUiBuilder setMinDesktopWidth(final int width) {
        this.minDesktopWidth = width;
        return this;
    }

    @Override
    public IWebUiBuilder setMinTabletWidth(final int width) {
        this.minTabletWidth = width;
        return this;
    }

    @Override
    public IWebUiBuilder setLocale(final String locale) {
        this.locale = locale;
        return this;
    }

    @Override
    public IWebUiBuilder setTimeFormat(final String timeFormat) {
        this.timeFormat = timeFormat;
        return this;
    }

    @Override
    public IWebUiBuilder setDateFormat(final String dateFormat) {
        this.dateFormat = dateFormat;
        return this;
    }

    @Override
    public IWebUiConfig done() {
        return webUiConfig;
    }

    @Override
    public <T extends AbstractEntity<?>> IWebUiBuilder addMaster(final Class<T> entityType, final EntityMaster<T> master) {
        mastersMap.put(entityType, master);
        return this;
    }

    @Override
    public <M extends MiWithConfigurationSupport<?>> IWebUiBuilder addCentre(final Class<M> menuType, final EntityCentre<?> centre) {
        centreMap.put(menuType, centre);
        return this;
    }

    public Map<Class<? extends AbstractEntity<?>>, EntityMaster<? extends AbstractEntity<?>>> getMasters() {
        return mastersMap;
    }

    public Map<Class<? extends MiWithConfigurationSupport<?>>, EntityCentre<?>> getCentres() {
        return centreMap;
    }

    public Map<String, AbstractCustomView> getCustomViews() {
        return viewMap;
    }

    /**
     * Generates a HTML representation of the web application UI preferences.
     *
     * @return
     */
    public String genWebUiPrefComponent() {
        if (this.minDesktopWidth <= this.minTabletWidth) {
            throw new IllegalStateException("The desktop width can not be less then or equal tablet width.");
        }
        return ResourceLoader.getText("ua/com/fielden/platform/web/app/config/tg-app-config.html").
                replaceAll("@minDesktopWidth", Integer.toString(this.minDesktopWidth)).
                replaceAll("@minTabletWidth", Integer.toString(this.minTabletWidth)).
                replaceAll("@locale", "\"" + this.locale + "\"").
                replaceAll("@dateFormat", "\"" + this.dateFormat + "\"").
                replaceAll("@timeFormat", "\"" + this.timeFormat + "\"");
    }

    @Override
    public IWebUiBuilder addCustomView(final AbstractCustomView customView) {
        viewMap.put(customView.getViewName(), customView);
        return this;
    }
}
