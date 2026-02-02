package ua.com.fielden.platform.web.app.config;

import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.app.exceptions.WebUiBuilderException;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.custom_view.AbstractCustomView;
import ua.com.fielden.platform.web.view.master.EntityMaster;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static java.lang.String.format;
import static org.apache.logging.log4j.LogManager.getLogger;

/// Implementation of the [IWebUiBuilder].
///
public class WebUiBuilder implements IWebUiBuilder {
    private final Logger logger = getLogger(getClass());
    /// The [IWebUiConfig] instance for which this configuration object was created.
    ///
    private final IWebUiConfig webUiConfig;

    private Optional<String> panelColor = Optional.empty();
    private Optional<String> watermark = Optional.empty();
    private Optional<String> watermarkStyle = Optional.empty();

    /// Holds the map between master's entity type and its master component.
    ///
    private final Map<Class<? extends AbstractEntity<?>>, EntityMaster<? extends AbstractEntity<?>>> mastersMap = new ConcurrentHashMap<>();

    /// Holds the map between entity centre's menu item type and entity centre.
    ///
    private final Map<Class<? extends MiWithConfigurationSupport<?>>, EntityCentre<?>> centreMap = new ConcurrentHashMap<>();

    private final Map<Class<? extends AbstractEntity<?>>, EntityActionConfig> openMasterActions = new ConcurrentHashMap<>();

    /// Extra action configurations not attached to any centre, master or main menu item.
    ///
    /// Key: action identifier.
    ///
    private final Map<String, EntityActionConfig> extraActionsMap = new ConcurrentHashMap<>();

    /// Holds the map between custom view name and custom view instance.
    ///
    private final Map<String, AbstractCustomView> viewMap = new LinkedHashMap<>();

    /// Creates new instance of [WebUiBuilder] for the specified [IWebUiConfig] instance.
    ///
    public WebUiBuilder(final IWebUiConfig webUiConfig) {
        this.webUiConfig = webUiConfig;
    }

    @Override
    public IWebUiBuilder setMinDesktopWidth(final int width) {
        this.webUiConfig.setMinDesktopWidth(width);
        return this;
    }

    @Override
    public IWebUiBuilder setMinTabletWidth(final int width) {
        this.webUiConfig.setMinTabletWidth(width);
        return this;
    }

    @Override
    public IWebUiBuilder setLocale(final String locale) {
        this.webUiConfig.setLocale(locale);
        return this;
    }

    @Override
    public IWebUiBuilder setTimeFormat(final String timeFormat) {
        this.webUiConfig.setTimeFormat(timeFormat);
        return this;
    }

    @Override
    public IWebUiBuilder setTimeWithMillisFormat(final String timeWithMillisFormat) {
        this.webUiConfig.setTimeWithMillisFormat(timeWithMillisFormat);
        return this;
    }

    @Override
    public IWebUiBuilder setDateFormat(final String dateFormat) {
        this.webUiConfig.setDateFormat(dateFormat);
        return this;
    }

    @Override
    public IWebUiConfig done() {
        return webUiConfig;
    }

    @Override
    public <T extends AbstractEntity<?>> IWebUiBuilder addMaster(final EntityMaster<T> master) {
        final Optional<EntityMaster<T>> masterOptional = getMaster(master.getEntityType());
        if (masterOptional.isPresent()) {
            if (masterOptional.get() != master) {
                throw new WebUiBuilderException(format("The master configuration for type [%s] has been already registered.", master.getEntityType().getSimpleName()));
            } else {
                logger.debug(format("\tThere is a try to register exactly the same master configuration instance for type [%s], that has been already registered.", master.getEntityType().getSimpleName()));
                return this;
            }
        } else {
            mastersMap.put(master.getEntityType(), master);
            return this;
        }
    }

    @Override
    public <ENTITY_TYPE extends AbstractEntity<?>> EntityMaster<ENTITY_TYPE> register(final EntityMaster<ENTITY_TYPE> master) {
        addMaster(master);
        return master;
    }

    @Override
    public <T extends AbstractEntity<?>> Optional<EntityMaster<T>> getMaster(final Class<T> entityType) {
        final EntityMaster<T> master = (EntityMaster<T>) mastersMap.get(entityType); // could be 'null', and type casting will not throw any exception in that case
        return Optional.ofNullable(master);
    }

    @Override
    public <T extends AbstractEntity<?>> IWebUiBuilder registerOpenMasterAction(final Class<T> entityType, final EntityActionConfig openMasterActionConfig) {
        if (entityType == null || openMasterActionConfig == null) {
            throw new WebUiBuilderException("None of the arguments to register open master actions can be null.");
        }

        if (openMasterActions.containsKey(entityType)) {
            throw new WebUiBuilderException(format("An open-master action config is already present for entity [%s].", entityType.getName()));
        }

        openMasterActions.putIfAbsent(entityType, openMasterActionConfig);
        return this;
    }


    @Override
    public <T extends AbstractEntity<?>> Supplier<Optional<EntityActionConfig>> getOpenMasterAction(final Class<T> entityType) {
        return () -> {
            if (openMasterActions.containsKey(entityType)) {
                return Optional.of(openMasterActions.get(entityType));
            }
            throw new WebUiBuilderException(format("An attempt is made to obtain open-master action configuration for entity [%s], but none is found. Please register a corresonding action configuration by using WebUiBuilder.registerOpenMasterAction.", entityType.getName()));
        };
    }

    @Override
    public IWebUiBuilder registerExtraAction(final EntityActionConfig actionConfig) {
        if (actionConfig.actionIdentifier.isEmpty()) {
            throw new WebUiBuilderException("Action identifier must be present to register an action configuration.");
        }
        final var actionIdentifier = actionConfig.actionIdentifier.get();
        final var prev = extraActionsMap.putIfAbsent(actionIdentifier, actionConfig);
        if (prev != null) {
            throw new WebUiBuilderException("An action with identifier [%s] has already been registered.".formatted(actionIdentifier));
        }
        return this;
    }

    @Override
    public <M extends MiWithConfigurationSupport<?>> IWebUiBuilder addCentre(final EntityCentre<?> centre) {
        final Optional<EntityCentre<?>> centreOptional = getCentre(centre.getMenuItemType());
        if (centreOptional.isPresent()) {
            if (centreOptional.get() != centre) {
                throw new WebUiBuilderException(format("The centre configuration for type [%s] has been already registered.", centre.getMenuItemType().getSimpleName()));
            } else {
                logger.debug(format("\tThere is a try to register exactly the same centre configuration instance for type [%s], that has been already registered.", centre.getMenuItemType().getSimpleName()));
                return this;
            }
        } else {
            centreMap.put(centre.getMenuItemType(), centre);
            centre.eventSourceClass().ifPresent(eventSourceClass -> webUiConfig.createAndRegisterEventSource(eventSourceClass));
            return this;
        }
    }

    @Override
    public <ENTITY_TYPE extends AbstractEntity<?>> EntityCentre<ENTITY_TYPE> register(final EntityCentre<ENTITY_TYPE> centre) {
        addCentre(centre);
        return centre;
    }

    @Override
    public <M extends MiWithConfigurationSupport<?>> Optional<EntityCentre<?>> getCentre(final Class<M> menuType) {
        return Optional.ofNullable(centreMap.get(menuType));
    }

    public Map<Class<? extends AbstractEntity<?>>, EntityMaster<? extends AbstractEntity<?>>> getMasters() {
        return mastersMap;
    }

    public Map<Class<? extends MiWithConfigurationSupport<?>>, EntityCentre<?>> getCentres() {
        return centreMap;
    }

    public Collection<EntityActionConfig> getExtraActions() {
        return extraActionsMap.values();
    }

    public Map<String, AbstractCustomView> getCustomViews() {
        return viewMap;
    }

    @Override
    public IWebUiBuilder addCustomView(final AbstractCustomView customView) {
        viewMap.put(customView.getViewName(), customView);
        return this;
    }

    @Override
    public IWebUiBuilder withTopPanelStyle(final Optional<String> backgroundColour, final Optional<String> watermark, final Optional<String> cssWatermark) {
        this.webUiConfig.setMainPanelColor(backgroundColour.orElse(""));
        this.webUiConfig.setWatermark(watermark.orElse(""));
        this.webUiConfig.setWatermarkStyle(cssWatermark.orElse(""));
        return this;
    }
}
