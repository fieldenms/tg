package ua.com.fielden.platform.web.app.config;

import static java.lang.String.format;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.app.exceptions.WebUiBuilderException;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.custom_view.AbstractCustomView;
import ua.com.fielden.platform.web.view.master.EntityMaster;

/**
 * Implementation of the {@link IWebUiBuilder}.
 *
 * @author TG Team
 *
 */
public class WebUiBuilder implements IWebUiBuilder {
    private final Logger logger = Logger.getLogger(getClass());
    /**
     * The {@link IWebUiConfig} instance for which this configuration object was created.
     */
    private final IWebUiConfig webUiConfig;

    private int minDesktopWidth = 980, minTabletWidth = 768;
    private String locale = "en-AU";
    private String dateFormat = "DD/MM/YYYY";
    private String timeFormat = "h:mm A";
    private String timeWithMillisFormat = "h:mm:ss.SSS A";
    private Optional<String> panelColor = Optional.empty();
    private Optional<String> watermark = Optional.empty();
    private Optional<String> watermarkStyle = Optional.empty();

    /**
     * Holds the map between master's entity type and its master component.
     */
    private final Map<Class<? extends AbstractEntity<?>>, EntityMaster<? extends AbstractEntity<?>>> mastersMap = new ConcurrentHashMap<>();

    /**
     * Holds the map between entity centre's menu item type and entity centre.
     */
    private final Map<Class<? extends MiWithConfigurationSupport<?>>, EntityCentre<?>> centreMap = new ConcurrentHashMap<>();

    private final Map<Class<? extends AbstractEntity<?>>, EntityActionConfig> openMasterActions = new ConcurrentHashMap<>();

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
    public IWebUiBuilder setTimeWithMillisFormat(final String timeWithMillisFormat) {
        this.timeWithMillisFormat = timeWithMillisFormat;
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
    public <T extends AbstractEntity<?>> IWebUiBuilder addMaster(final EntityMaster<T> master) {
        final Optional<EntityMaster<T>> masterOptional = getMaster(master.getEntityType());
        if (masterOptional.isPresent()) {
            if (masterOptional.get() != master) {
                throw new WebUiBuilderException(String.format("The master configuration for type [%s] has been already registered.", master.getEntityType().getSimpleName()));
            } else {
                logger.info(String.format("There is a try to register exactly the same master configuration instance for type [%s], that has been already registered.", master.getEntityType().getSimpleName()));
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
    public <M extends MiWithConfigurationSupport<?>> IWebUiBuilder addCentre(final EntityCentre<?> centre) {
        final Optional<EntityCentre<?>> centreOptional = getCentre(centre.getMenuItemType());
        if (centreOptional.isPresent()) {
            if (centreOptional.get() != centre) {
                throw new WebUiBuilderException(String.format("The centre configuration for type [%s] has been already registered.", centre.getMenuItemType().getSimpleName()));
            } else {
                logger.info(format("There is a try to register exactly the same centre configuration instance for type [%s], that has been already registered.", centre.getMenuItemType().getSimpleName()));
                return this;
            }
        } else {
            centreMap.put(centre.getMenuItemType(), centre);
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
        return ResourceLoader.getText("ua/com/fielden/platform/web/app/config/tg-app-config.js").
                replace("@minDesktopWidth", Integer.toString(this.minDesktopWidth)).
                replace("@minTabletWidth", Integer.toString(this.minTabletWidth)).
                replace("@locale", "\"" + this.locale + "\"").
                replace("@independentTimeZoneSetting", webUiConfig.independentTimeZone() ? format("moment.tz.setDefault('%s');", TimeZone.getDefault().getID()) : "").
                replace("@dateFormat", "\"" + this.dateFormat + "\"").
                replace("@timeFormat", "\"" + this.timeFormat + "\"").
                replace("@timeWithMillisFormat", "\"" + this.timeWithMillisFormat + "\"");
    }

    public String getAppIndex () {
        return ResourceLoader.getText("ua/com/fielden/platform/web/index.html")
                .replace("@panelColor", panelColor.map(val -> "--tg-main-pannel-color: " + val + ";").orElse(""))
                .replace("@watermark", "'" + watermark.orElse("") + "'")
                .replace("@cssStyle", watermarkStyle.orElse("") );
    }

    @Override
    public IWebUiBuilder addCustomView(final AbstractCustomView customView) {
        viewMap.put(customView.getViewName(), customView);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IWebUiBuilder withTopPanelStyle(final Optional<String> backgroundColour, final Optional<String> watermark, final Optional<String> cssWatermark) {
        this.panelColor = backgroundColour;
        this.watermark = watermark;
        this.watermarkStyle = cssWatermark;
        return this;
    }
}
