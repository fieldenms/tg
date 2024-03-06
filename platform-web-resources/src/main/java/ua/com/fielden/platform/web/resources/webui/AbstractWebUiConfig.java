package ua.com.fielden.platform.web.resources.webui;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.ResourceLoader.getStream;
import static ua.com.fielden.platform.web.centre.CentreUpdater.getDefaultCentre;
import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;
import static ua.com.fielden.platform.web.minijs.JsCode.jsCode;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.SAVE_OWN_COPY_MSG;
import static ua.com.fielden.platform.web.resources.webui.FileResource.generateFileName;
import static ua.com.fielden.platform.web.view.master.api.actions.impl.MasterActionOptions.ALL_OFF;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Injector;

import ua.com.fielden.platform.attachment.AttachmentPreviewEntityAction;
import ua.com.fielden.platform.basic.config.Workflows;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.EntityDeleteAction;
import ua.com.fielden.platform.entity.EntityDeleteActionProducer;
import ua.com.fielden.platform.entity.EntityEditAction;
import ua.com.fielden.platform.entity.EntityExportAction;
import ua.com.fielden.platform.entity.EntityNewAction;
import ua.com.fielden.platform.entity.UserDefinableHelp;
import ua.com.fielden.platform.menu.Menu;
import ua.com.fielden.platform.menu.MenuSaveAction;
import ua.com.fielden.platform.ref_hierarchy.ReferenceHierarchy;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.utils.IDates;
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
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.ref_hierarchy.ReferenceHierarchyWebUiConfig;
import ua.com.fielden.platform.web.resources.webui.exceptions.InvalidUiConfigException;
import ua.com.fielden.platform.web.sse.EventSourceDispatchingEmitter;
import ua.com.fielden.platform.web.sse.IEventSource;
import ua.com.fielden.platform.web.sse.IEventSourceEmitterRegister;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.actions.impl.MasterActionOptions;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;

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
    private final Logger logger = LogManager.getLogger(getClass());
    private static final String ERR_IN_COMPOUND_EMITTER = "Event source compound emitter should have cought this error. Something went wrong in WebUiConfig.";
    private static final String CREATE_DEFAULT_CONFIG_INFO = "Creating default %s configurations for [%s]-typed centres (caching)...";

    private final String title;
    private WebUiBuilder webUiBuilder;
    private Injector injector;

    private final EventSourceDispatchingEmitter dispatchingEmitter;

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
    private final MasterActionOptions masterActionOptions;
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
     * @param masterActionOptions -- determines what options are available for master's save and cancel actions.
     */
    public AbstractWebUiConfig(final String title, final Workflows workflow, final String[] externalResourcePaths, final boolean independentTimeZone, final Optional<MasterActionOptions> masterActionOptions) {
        this.title = title;
        this.independentTimeZone = independentTimeZone;
        this.masterActionOptions = masterActionOptions.orElse(ALL_OFF);
        this.webUiBuilder = new WebUiBuilder(this);
        this.dispatchingEmitter = new EventSourceDispatchingEmitter();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    logger.info("Closing Event Source Dispatching Emitter with all registered emitters...");
                    dispatchingEmitter.close();
                } catch (final Exception ex) {
                    logger.error("Closing Event Source Dispatching Emitter encountered an error.", ex);
                }
            }
        });
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
     * Creates abstract {@link IWebUiConfig}.
     *
     * @param title -- application title displayed by the web client
     * @param workflow -- indicates development or deployment workflow, which affects how web resources get loaded.
     * @param externalResourcePaths
     * - additional root paths for file resources. (see {@link #resourcePaths} for more information).
     * @param masterActionOptions -- determines what options are available for master's save and cancel actions.
     */
    public AbstractWebUiConfig(final String title, final Workflows workflow, final String[] externalResourcePaths, final Optional<MasterActionOptions> masterActionOptions) {
        this(title, workflow, externalResourcePaths, false, masterActionOptions);
    }

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
        this(title, workflow, externalResourcePaths, independentTimeZone, of(ALL_OFF));
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
        final UserMenuVisibilityAssociatorWebUiConfig userMenuAssociatorWebUiConfig = new UserMenuVisibilityAssociatorWebUiConfig(injector);
        final CentreConfigurationWebUiConfig centreConfigurationWebUiConfig = new CentreConfigurationWebUiConfig(injector());
        final EntityMaster<UserDefinableHelp> userDefinableHelpMaster = StandardMastersWebUiConfig.createUserDefinableHelpMaster(injector());

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
        .addMaster(userMenuAssociatorWebUiConfig.master)
        .addMaster(userDefinableHelpMaster)
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
        final String indexSource = webUiBuilder.getAppIndex(injector().getInstance(IDates.class)).replace("@title", title);
        if (isDevelopmentWorkflow(this.workflow)) {
            return indexSource.replace("@startupResources", "startup-resources-origin");
        } else {
            return indexSource.replace("@startupResources", "startup-resources-vulcanized");
        }

    }

    @Override
    public IEventSourceEmitterRegister getEventSourceEmitterRegister() {
        return dispatchingEmitter;
    }

    @Override
    public IWebUiConfig createAndRegisterEventSource(final Class<? extends IEventSource> eventSourceClass) {
        try {
            dispatchingEmitter.createAndRegisterEventSource(eventSourceClass, () -> injector.getInstance(eventSourceClass));
        } catch (final Exception ex) {
            logger.error(ex);
            throw new InvalidUiConfigException(ERR_IN_COMPOUND_EMITTER, ex);
        }

        return this;
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
    public MasterActionOptions masterActionOptions() {
        return masterActionOptions;
    }

    /**
     * Returns {@link JsCode} suitable for {@link IPreAction} for centre configuration sharing actions.
     * <p>
     * It makes entity centre's {@code _actionInProgress} property true if the action has started and false if it has completed.
     * This is suitable for asynchronous share actions (e.g. with UI, where Entity Master opens, or without UI but with some custom server-side producer/companion logic).
     * Default Share action is synchronous, client-side-only, action (see {@link #centreConfigShareActions()}).
     */
    protected static JsCode promoteShareActionProgressToCentreActions() {
        return jsCode(
              "if (!action.oldIsActionInProgressChanged) { // 'action' is current tg-ui-action \n"
            + "    action.oldIsActionInProgressChanged = action.isActionInProgressChanged.bind(action);\n"
            + "    action.isActionInProgressChanged = (newValue, oldValue) => {\n"
            + "        action.oldIsActionInProgressChanged(newValue, oldValue);\n"
            + "        self._actionInProgress = newValue; // 'self' is enclosing centre; enhance action's observer for isActionInProgress to set _actionInProgress to whole centre which controls disablement of all other buttons \n"
            + "    };\n"
            + "}\n"
        );
    }

    /**
     * Returns {@link JsCode} suitable for {@link IPreAction} for centre configuration sharing actions.
     * <p>
     * It shows informational toast about inability to share currently loaded configuration (if sharing validation was indeed erroneous).
     */
    protected static JsCode showToastForShareError() {
        return jsCode(
              "if (self.shareError) { // 'self' is enclosing centre \n"
            + "    if (self.shareError === '" + SAVE_OWN_COPY_MSG + "') {\n"
            + "        action.toaster.openToastWithoutEntity(self.shareError, true, self.shareError, false);\n"
            + "    } else {\n"
            + "        action.toaster.openToastWithoutEntity(self.shareError, false, '', false);\n"
            + "    }\n"
            + "}\n"
        );
    }

    /**
     * Returns {@link JsCode} suitable for {@link IPreAction} for centre configuration sharing actions.
     * <p>
     * It copies currently loaded configuration URL (window.location.href) to the clipboard and shows informational message about it (if sharing validation was successful).
     */
    protected static JsCode copyToClipboardForSuccessfulShare() {
        return jsCode(
              "if (!self.shareError) { // 'self' is enclosing centre \n"
            + "    const link = window.location.href;\n"
            + "    navigator && navigator.clipboard && navigator.clipboard.writeText(link).then(() => { // Writing into clipboard is always permitted for currently open tab (https://developer.mozilla.org/en-US/docs/Web/API/Clipboard/writeText) -- that's why promise error should never occur; \n"
            + "        action.toaster.openToastWithoutEntity('Copied to clipboard.', true, link, false); // if for some reason the promise will be rejected then 'Unexpected error occurred.' will be shown to the user and global handler will report that to the server. \n"
            + "    });\n"
            + "}\n"
        );
    }

    @Override
    public List<EntityActionConfig> centreConfigShareActions() {
        // default Share action is implemented specially through cached 'shareError' property in its 'preAction';
        // if Share is not possible it shows toast for an error, otherwise it immediately copies a link to the clipboard and shows toast for a success;
        // it is very important to copy link inside preAction as a part of UI callback, otherwise we get permission error in Safari browsers (see #2116)
        return asList(
            action(CentreConfigShareAction.class)
            .withContext(context().withSelectionCrit().build())
            .preAction(() -> jsCode(showToastForShareError().toString() + copyToClipboardForSuccessfulShare().toString() + "return Promise.reject('Share action completed.');\n"))
            .icon("tg-icons:share")
            .shortDesc("Share")
            .longDesc("Share centre configuration")
            .withNoParentCentreRefresh()
            .build()
        );
    }

    @Override
    public void loadCentreGeneratedTypesAndCriteriaTypes(final Class<?> entityType) {
        final var critGenerator = injector.getInstance(ICriteriaGenerator.class);
        // load all standalone centres for concrete 'entityType' (with their generated types and criteria types)
        String log = CREATE_DEFAULT_CONFIG_INFO.formatted("standalone", entityType.getSimpleName());
        logger.info(log);
        getCentres().entrySet().stream()
            .filter(entry -> entry.getValue().getEntityType().equals(entityType))
            .map(Entry::getKey)
            .forEach(miType -> critGenerator.generateCentreQueryCriteria(getDefaultCentre(miType, this)));
        logger.info(log + "done");

        // load all embedded centres for concrete 'entityType' (with their generated types and criteria types)
        log = CREATE_DEFAULT_CONFIG_INFO.formatted("embedded", entityType.getSimpleName());
        logger.info(log);
        getEmbeddedCentres().entrySet().stream()
            .filter(entry -> entry.getValue()._1.getEntityType().equals(entityType))
            .map(Entry::getKey)
            .forEach(miType -> critGenerator.generateCentreQueryCriteria(getDefaultCentre(miType, this)));
        logger.info(log + "done");
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