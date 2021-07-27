package ua.com.fielden.platform.web.resources.webui;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.ResourceLoader.getStream;
import static ua.com.fielden.platform.web.action.CentreConfigShareActionProducer.createPostAction;
import static ua.com.fielden.platform.web.action.CentreConfigShareActionProducer.createPreAction;
import static ua.com.fielden.platform.web.centre.CentreUpdater.getDefaultCentre;
import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;
import static ua.com.fielden.platform.web.resources.webui.FileResource.generateFileName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Injector;

import ua.com.fielden.platform.attachment.AttachmentPreviewEntityAction;
import ua.com.fielden.platform.basic.config.Workflows;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.EntityDeleteAction;
import ua.com.fielden.platform.entity.EntityDeleteActionProducer;
import ua.com.fielden.platform.entity.EntityEditAction;
import ua.com.fielden.platform.entity.EntityExportAction;
import ua.com.fielden.platform.entity.EntityNewAction;
import ua.com.fielden.platform.menu.Menu;
import ua.com.fielden.platform.menu.MenuSaveAction;
import ua.com.fielden.platform.ref_hierarchy.ReferenceHierarchy;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.action.CentreConfigurationWebUiConfig;
import ua.com.fielden.platform.web.action.StandardMastersWebUiConfig;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.app.config.WebUiBuilder;
import ua.com.fielden.platform.web.centre.CentreConfigShareAction;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.exceptions.EntityCentreConfigurationException;
import ua.com.fielden.platform.web.custom_view.AbstractCustomView;
import ua.com.fielden.platform.web.interfaces.DeviceProfile;
import ua.com.fielden.platform.web.ioc.exceptions.MissingWebResourceException;
import ua.com.fielden.platform.web.menu.IMainMenuBuilder;
import ua.com.fielden.platform.web.menu.impl.MainMenuBuilder;
import ua.com.fielden.platform.web.ref_hierarchy.ReferenceHierarchyWebUiConfig;
import ua.com.fielden.platform.web.view.master.EntityMaster;

/**
 * The base implementation for Web UI configuration, which should be inherited from in concrete applications for defining the final application specific Web UI configuration.
 * <p>
 * Method {@link IWebUiConfig#initConfiguration()} should be implemented in the application specific Web UI configuration, where menus, entity centres and entity master should be
 * registered by obtaining corresponding builders via methods {@link #configApp()} and {@link #configDesktopMainMenu()}.
 *
 * @author TG Team
 *
 */
public abstract class AbstractWebUiConfig implements IWebUiConfig {
    private final Logger logger = Logger.getLogger(getClass());
    private final String title;
    private WebUiBuilder webUiBuilder;
    private Injector injector;

    protected MainMenuBuilder desktopMainMenuConfig;
    protected MainMenuBuilder mobileMainMenuConfig;
    /**
     * The paths for any kind of file resources those are needed for browser client. These are mapped to the '/resources/' router path. Also these resource paths might be augmented
     * with other custom paths. When client asks for a resource then this application will search for that resource in these paths starting from the custom ones.
     */
    private final List<String> resourcePaths;
    private final Workflows workflow;
    private final Map<String, String> checksums;
    private final boolean independentTimeZone;
    /**
     * Holds the map between embedded entity centre's menu item type and [entity centre; entity master] pair.
     */
    private Map<Class<? extends MiWithConfigurationSupport<?>>, T2<EntityCentre<?>, EntityMaster<? extends AbstractEntity<?>>>> embeddedCentreMap;

    /**
     * Creates abstract {@link IWebUiConfig}.
     *
     * @param title -- application title displayed by the web client
     * @param workflow -- indicates development or deployment workflow, which affects how web resources get loaded.
     * @param externalResourcePaths
     * - additional root paths for file resources. (see {@link #resourcePaths} for more information).
     * @param independentTimeZone -- if {@code true} is passed then user requests are treated as if they are made from the same timezone as defined for the application server.
     */
    public AbstractWebUiConfig(final String title, final Workflows workflow, final String[] externalResourcePaths, final boolean independentTimeZone) {
        this.title = title;
        this.independentTimeZone = independentTimeZone;
        this.webUiBuilder = new WebUiBuilder(this);
        this.desktopMainMenuConfig = new MainMenuBuilder(this);
        this.mobileMainMenuConfig = new MainMenuBuilder(this);

        this.workflow = workflow;

        final LinkedHashSet<String> allResourcePaths = new LinkedHashSet<>();
        allResourcePaths.addAll(Arrays.asList("", "ua/com/fielden/platform/web/"));
        allResourcePaths.addAll(Arrays.asList(externalResourcePaths));
        this.resourcePaths = new ArrayList<>(Collections.unmodifiableSet(allResourcePaths));
        Collections.reverse(this.resourcePaths);

        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            checksums = objectMapper.readValue(getStream(generateFileName(resourcePaths, "checksums.json")), LinkedHashMap.class);
        } catch (final Exception ex) {
            throw new MissingWebResourceException("Could not read checksums from file.", ex);
        }
    }

    /**
     * The same as {@link #AbstractWebUiConfig(String, Workflows, String[], boolean), but with the last argument {@code false}.
     * This value is suitable for most applications.
     */
    public AbstractWebUiConfig(final String title, final Workflows workflow, final String[] externalResourcePaths) {
            this(title, workflow, externalResourcePaths, false);
    }

    @Override
    public void initConfiguration() {
        final EntityMaster<EntityNewAction> genericEntityNewActionMaster = StandardMastersWebUiConfig.createEntityNewMaster(injector());
        final EntityMaster<EntityEditAction> genericEntityEditActionMaster = StandardMastersWebUiConfig.createEntityEditMaster(injector());
        final EntityMaster<ReferenceHierarchy> genericReferenceHierarchyMaster = ReferenceHierarchyWebUiConfig.createReferenceHierarchyMaster(injector());
        final EntityMaster<EntityExportAction> genericEntityExportActionMaster = StandardMastersWebUiConfig.createExportMaster(injector());
        final EntityMaster<AttachmentPreviewEntityAction> attachmentPreviewMaster = StandardMastersWebUiConfig.createAttachmentPreviewMaster(injector());
        final EntityMaster<EntityDeleteAction> genericEntityDeleteActionMaster = EntityMaster.noUiFunctionalMaster(EntityDeleteAction.class, EntityDeleteActionProducer.class, injector());
        final EntityMaster<MenuSaveAction> genericMenuSaveMaster = EntityMaster.noUiFunctionalMaster(MenuSaveAction.class, injector());
        final CentreConfigurationWebUiConfig centreConfigurationWebUiConfig = new CentreConfigurationWebUiConfig(injector());

        AcknowledgeWarningsWebUiConfig.register(injector(), configApp()); // generic TG functionality for warnings acknowledgement

        configApp()
        // register generic actions
        .addMaster(genericEntityNewActionMaster)
        .addMaster(genericEntityEditActionMaster)
        .addMaster(genericReferenceHierarchyMaster)
        .addMaster(attachmentPreviewMaster)
        .addMaster(genericEntityDeleteActionMaster)
        .addMaster(genericEntityExportActionMaster)
        .addMaster(genericMenuSaveMaster)
        .addMaster(new MenuWebUiConfig(injector(), desktopMainMenuConfig, mobileMainMenuConfig).master)
        // centre configuration management
        .addMaster(centreConfigurationWebUiConfig.centreConfigUpdaterMaster)
        .addMaster(centreConfigurationWebUiConfig.centreColumnWidthConfigUpdaterMaster)
        .addMaster(centreConfigurationWebUiConfig.centrePreferredViewUpdaterMaster)
        // centre config actions
        .addMaster(centreConfigurationWebUiConfig.centreConfigShareActionMaster)
        .addMaster(centreConfigurationWebUiConfig.centreConfigNewActionMaster)
        .addMaster(centreConfigurationWebUiConfig.centreConfigDuplicateActionMaster)
        .addMaster(centreConfigurationWebUiConfig.centreConfigLoadActionMaster)
        .addMaster(centreConfigurationWebUiConfig.centreConfigEditActionMaster)
        .addMaster(centreConfigurationWebUiConfig.centreConfigConfigureActionMaster)
        .addMaster(centreConfigurationWebUiConfig.centreConfigDeleteActionMaster)
        .addMaster(centreConfigurationWebUiConfig.centreConfigSaveActionMaster)
        .addMaster(centreConfigurationWebUiConfig.overrideCentreConfigMaster);
    }

    @Override
    public IWebUiBuilder configApp() {
        return webUiBuilder;
    }

    @Override
    public IMainMenuBuilder configDesktopMainMenu() {
        return desktopMainMenuConfig;
    }

    @Override
    public IMainMenuBuilder configMobileMainMenu() {
        return mobileMainMenuConfig;
    }

    @Override
    public final String genWebUiPreferences() {
        return webUiBuilder.genWebUiPrefComponent();
    }

    @Override
    public final String genMainWebUIComponent() {
        final String mainWebUiComponent = ResourceLoader.getText("ua/com/fielden/platform/web/app/tg-app-template.js");
        if (Workflows.deployment == workflow || Workflows.vulcanizing == workflow) {
            return mainWebUiComponent.replace("//@use-empty-console.log", "console.log = () => {};\n");
        } else {
            return mainWebUiComponent;
        }
    }

    @Override
    public final String genAppIndex() {
        final String indexSource = webUiBuilder.getAppIndex().replace("@title", title);
        if (isDevelopmentWorkflow(this.workflow)) {
            return indexSource.replace("@startupResources", "startup-resources-origin");
        } else {
            return indexSource.replace("@startupResources", "startup-resources-vulcanized");
        }

    }

    private static boolean isDevelopmentWorkflow(final Workflows workflow) {
        return Workflows.development == workflow || Workflows.vulcanizing == workflow;
    }

    /**
     * Returns the map of entity masters for this web application.
     *
     * @return
     */
    @Override
    public final Map<Class<? extends AbstractEntity<?>>, EntityMaster<? extends AbstractEntity<?>>> getMasters() {
        return webUiBuilder.getMasters();
    }

    /**
     * Returns the map of entity centres for this web application.
     *
     * @return
     */
    @Override
    public final Map<Class<? extends MiWithConfigurationSupport<?>>, EntityCentre<?>> getCentres() {
        return webUiBuilder.getCentres();
    }

    @Override
    public Map<String, AbstractCustomView> getCustomViews() {
        return webUiBuilder.getCustomViews();
    }

    public void setInjector(final Injector injector) {
        this.injector = injector;
    }

    protected Injector injector() {
        return injector;
    }

    @Override
    public List<String> resourcePaths() {
        return this.resourcePaths;
    }

    @Override
    public Workflows workflow() {
        return workflow;
    }

    @Override
    public final void clearConfiguration() {
        logger.error("Clearing configurations...");
        this.webUiBuilder = new WebUiBuilder(this);
        this.desktopMainMenuConfig = new MainMenuBuilder(this);
        this.mobileMainMenuConfig = new MainMenuBuilder(this);
        this.embeddedCentreMap = null;
        logger.error("Clearing configurations...done");
    }

    @Override
    public Menu getMenuEntity(final DeviceProfile deviceProfile) {
        return DeviceProfile.DESKTOP.equals(deviceProfile) ? desktopMainMenuConfig.getMenu() : mobileMainMenuConfig.getMenu();
    }

    @Override
    public Optional<String> checksum(final String resourceURI) {
        return ofNullable(checksums.get(resourceURI));
    }

    @Override
    public boolean independentTimeZone() {
        return independentTimeZone;
    }

    @Override
    public List<EntityActionConfig> centreConfigShareActions() {
        return asList(
            action(CentreConfigShareAction.class)
            .withContext(context().withSelectionCrit().build())
            .preAction(createPreAction())
            .postActionSuccess(createPostAction("errorMessage"))
            .icon("tg-icons:share")
            .shortDesc("Share")
            .longDesc("Share centre configuration")
            .withNoParentCentreRefresh()
            .build()
        );
    }

    @Override
    public void createDefaultConfigurationsForAllCentres() {
        final Set<Class<? extends MiWithConfigurationSupport<?>>> miTypes = getCentres().keySet();
        final int size = miTypes.size();
        final String log = format("Creating default configurations for [%s] centres (caching)...", size);
        logger.info(log);
        for (final Class<? extends MiWithConfigurationSupport<?>> miType: miTypes) {
            getDefaultCentre(miType, this);
        }
        logger.info(log + "done");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Iterates through all registered {@link EntityMaster}s, determines whether they have embedded centres, and calculates map between {@code miType} of embedded centre and [{@link EntityCentre}; {@link EntityMaster}] pair.
     */
    @Override
    public Map<Class<? extends MiWithConfigurationSupport<?>>, T2<EntityCentre<?>, EntityMaster<? extends AbstractEntity<?>>>> getEmbeddedCentres() {
        if (embeddedCentreMap == null) {
            final String log = "Calculating embedded centres...";
            logger.info(log);
            embeddedCentreMap = new ConcurrentHashMap<>();
            for (final EntityMaster<? extends AbstractEntity<?>> master: getMasters().values()) {
                final Optional<EntityCentre<?>> embeddedCentreOpt = master.getEmbeddedCentre();
                if (embeddedCentreOpt.isPresent()) {
                    final Class<? extends MiWithConfigurationSupport<?>> miType = embeddedCentreOpt.get().getMenuItemType();
                    if (embeddedCentreMap.containsKey(miType)) {
                        throw new EntityCentreConfigurationException(format("Centre [%s] has been added as embedded for both [%s] and [%s] masters.", miType.getSimpleName(), embeddedCentreMap.get(miType)._2.getEntityType().getSimpleName(), master.getEntityType().getSimpleName()));
                    }
                    embeddedCentreMap.put(miType, t2(embeddedCentreOpt.get(), master));
                }
            }
            logger.info(format(log + "done [%s]", embeddedCentreMap.size()));
        }
        return embeddedCentreMap;
    }

}