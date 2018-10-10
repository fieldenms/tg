package ua.com.fielden.platform.web.app.config;

import java.util.Optional;
import java.util.function.Supplier;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
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
     * @param master
     *
     * @return
     */
    <T extends AbstractEntity<?>> IWebUiBuilder addMaster(final EntityMaster<T> master);

    /**
     * Registers entity master in web application and returns it.
     *
     * @param master
     * @return
     */
    <ENTITY_TYPE extends AbstractEntity<?>> EntityMaster<ENTITY_TYPE> register(final EntityMaster<ENTITY_TYPE> master);

    /**
     * Returns an optional value with a master instance for the specified type.
     * An empty optional value is returned if there is no master registered for the specified type.
     *
     * @param entityType
     * @return
     */
    <T extends AbstractEntity<?>> Optional<EntityMaster<T>> getMaster(final Class<T> entityType);

    /**
     * Registers entity centre in web application and returns it.
     *
     * @param centre
     * @return
     */
    <ENTITY_TYPE extends AbstractEntity<?>> EntityCentre<ENTITY_TYPE> register(final EntityCentre<ENTITY_TYPE> centre);

    /**
     * Adds the entity centre to web application configuration object.
     * @param centre
     *
     * @return
     */
    <M extends MiWithConfigurationSupport<?>> IWebUiBuilder addCentre(final EntityCentre<?> centre);

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


    /**
     * Registers (associates and caches) an entity action configuration with an entity type. Entity action configuration must represent an action to open an Entity Master corresponding to the specified entity type.
     * Repeated attempts to register an entity action configuration with the same entity type result in a runtime exception.
     *
     * @param entityType
     * @param openMasterActionConfig
     * @return
     */
    <T extends AbstractEntity<?>> IWebUiBuilder registerOpenMasterAction(final Class<T> entityType, final EntityActionConfig openMasterActionConfig);

    /**
     * Returns a supplier to lazily obtain an open entity master action configuration for the specified entity type.
     * The returned supplier is never <code>null</code>, but its result is optional and could be empty.
     *
     * @param entityType
     * @return
     */
    <T extends AbstractEntity<?>> Supplier<Optional<EntityActionConfig>> getOpenMasterAction(final Class<T> entityType);
}
