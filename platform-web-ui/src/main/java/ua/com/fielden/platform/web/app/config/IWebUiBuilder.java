package ua.com.fielden.platform.web.app.config;

import java.util.Optional;

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
     * Set the date format for the web application.
     *
     * @param dateFormat
     * @return
     */
    IWebUiBuilder setDateFormat(String dateFormat);

    /**
     * Set the time format for the web application.
     *
     * @param timeFormat
     * @return
     */
    IWebUiBuilder setTimeFormat(String timeFormat);
    
    /**
     * Set the time with millis format for the web application.
     *
     * @param timeWithMillisFormat
     * @return
     */
    IWebUiBuilder setTimeWithMillisFormat(String timeWithMillisFormat);

    /**
     * Adds the entity master to web application configuration object.
     *
     * @param entityType
     * @param master
     * @return
     */
    <T extends AbstractEntity<?>> IWebUiBuilder addMaster(final Class<T> entityType, final EntityMaster<T> master);
    
    /**
     * Returns an optional value with a master instance for the specified type. 
     * An empty optional value is returned if there is no master registered for the specified type. 
     * 
     * @param entityType
     * @return
     */
    <T extends AbstractEntity<?>> Optional<EntityMaster<T>> getMaster(final Class<T> entityType);

    /**
     * Adds the entity centre to web application configuration object.
     *
     * @param menuType
     * @param centre
     * @return
     */
    <M extends MiWithConfigurationSupport<?>> IWebUiBuilder addCentre(final Class<M> menuType, final EntityCentre<?> centre);

    /**
     * Returns an optional value with a centre instance for the specified menu item type. 
     * An empty optional value is returned if there is no centre registered for the specified type. 
     * 
     * @param menuType
     * @return
     */
    <M extends MiWithConfigurationSupport<?>> Optional<EntityCentre<?>> getCentre(final Class<M> menuType);

    /**
     * Adds the custom view to the application configuration object.
     *
     * @param customView
     * @return
     */
    IWebUiBuilder addCustomView(final AbstractCustomView customView);

    /**
     * Finish to configure the web application.
     *
     * @return
     */
    IWebUiConfig done();

}
