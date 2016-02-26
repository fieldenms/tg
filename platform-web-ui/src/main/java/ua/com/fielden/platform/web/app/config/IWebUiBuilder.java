package ua.com.fielden.platform.web.app.config;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.custom_view.AbstractCustomView;
import ua.com.fielden.platform.web.view.master.EntityMaster;

/**
 * A contract for building a Web UI configuration.
 *
 * @author TG Team
 *
 */
public interface IWebUiBuilder {

    /**
     * Set the minimal desktop width.
     *
     * @param width
     * @return
     */
    IWebUiBuilder setMinDesktopWidth(int width);

    /**
     * Set the minimal tablet width
     *
     * @param width
     * @return
     */
    IWebUiBuilder setMinTabletWidth(int width);

    /**
     * Set the locale for the web application.
     *
     * @param locale
     * @return
     */
    IWebUiBuilder setLocale(String locale);

    /**
     * Set the time format for the web application.
     *
     * @param timeFormat
     * @return
     */
    IWebUiBuilder setTimeFormat(String timeFormat);

    /**
     * Set the date format for the web application.
     *
     * @param dateFormat
     * @return
     */
    IWebUiBuilder setDateFormat(String dateFormat);

    /**
     * Adds the entity master to web application configuration object.
     *
     * @param entityType
     * @param master
     * @return
     */
    <T extends AbstractEntity<?>> IWebUiBuilder addMaster(Class<T> entityType, EntityMaster<T> master);

    /**
     * Adds the entity centre to web application configuration object.
     *
     * @param menuType
     * @param centre
     * @return
     */
    <M extends MiWithConfigurationSupport<?>> IWebUiBuilder addCentre(Class<M> menuType, EntityCentre<?> centre);

    /**
     * Adds the custom view to the application configuration object.
     *
     * @param customView
     * @return
     */
    IWebUiBuilder addCustomView(AbstractCustomView customView);

    /**
     * Finish to configure the web application.
     *
     * @return
     */
    IWebUiConfig done();

}
