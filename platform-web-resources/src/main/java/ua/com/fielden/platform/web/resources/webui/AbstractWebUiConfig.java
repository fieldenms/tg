package ua.com.fielden.platform.web.resources.webui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Injector;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.attachment.AttachmentPreviewEntityAction;
import ua.com.fielden.platform.basic.config.Workflows;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.entity.*;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.menu.Menu;
import ua.com.fielden.platform.menu.MenuSaveAction;
import ua.com.fielden.platform.ref_hierarchy.ReferenceHierarchy;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.utils.StreamUtils;
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

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.apache.commons.validator.routines.UrlValidator.ALLOW_LOCAL_URLS;
import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.types.Hyperlink.SupportedProtocols.HTTPS;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.ResourceLoader.getStream;
import static ua.com.fielden.platform.web.centre.CentreUpdater.getDefaultCentre;
import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;
import static ua.com.fielden.platform.web.minijs.JsCode.jsCode;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.SAVE_OWN_COPY_MSG;
import static ua.com.fielden.platform.web.resources.webui.FileResource.generateFileName;
import static ua.com.fielden.platform.web.view.master.api.actions.impl.MasterActionOptions.ALL_OFF;

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
    private static final String CREATE_DEFAULT_CONFIG_INFO = "Creating default configurations for [%s]-typed centres (caching)...";
    private static final int DEFAULT_EXTERNAL_SITE_EXPIRY_DAYS = 183;

    private final String title;
    private final Optional<String> ideaUri;
    private WebUiBuilder webUiBuilder;
    private Injector injector;

    private final EventSourceDispatchingEmitter dispatchingEmitter;

    protected MainMenuBuilder desktopMainMenuConfig;
    protected MainMenuBuilder mobileMainMenuConfig;

    /**
     * The paths for any kind of file resources that are needed for a web-client.
     * These are mapped to the '/resources/' router path.
     * Also, these resource paths might be augmented with other custom paths.
     * When a web-client requests a resource, then the application will search for that resource in these paths, starting with the custom paths.
     */
    private final List<String> resourcePaths;
    private final Workflows workflow;
    private final Map<String, String> checksums;
    private final boolean independentTimeZone;
    private final MasterActionOptions masterActionOptions;

    /**
     * Holds the map between embedded entity centres' menu item types and [entity centre, entity master] pair.
     */
    private Map<Class<? extends MiWithConfigurationSupport<?>>, T2<EntityCentre<?>, EntityMaster<? extends AbstractEntity<?>>>> embeddedCentreMap;

    /**
     * Creates abstract {@link IWebUiConfig}.
     *
     * @param title  application title displayed by the web client.
     * @param workflow  indicates development or deployment workflow, which affects how web resources get loaded.
     * @param externalResourcePaths  additional root paths for file resources (see {@link #resourcePaths} for more information).
     * @param independentTimeZone  if {@code true} is passed then user requests are treated as if they are made from the same timezone as defined for the application server.
     * @param masterActionOptions  determines what options are available for master's save and cancel actions.
     * @param ideaUri  an optional idea page URI.
     */
    public AbstractWebUiConfig(
            final String title,
            final Workflows workflow,
            final String[] externalResourcePaths,
            final boolean independentTimeZone,
            final Optional<MasterActionOptions> masterActionOptions,
            final Optional<String> ideaUri)
    {
        this.title = title;
        this.ideaUri = ideaUri.map(uri -> validateIdeaUri(uri).getInstanceOrElseThrow());
        this.independentTimeZone = independentTimeZone;
        this.masterActionOptions = masterActionOptions.orElse(ALL_OFF);
        this.webUiBuilder = new WebUiBuilder(this);
        this.dispatchingEmitter = new EventSourceDispatchingEmitter();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                logger.info("Closing Event Source Dispatching Emitter with all registered emitters...");
                dispatchingEmitter.close();
            } catch (final Exception ex) {
                logger.error("Closing Event Source Dispatching Emitter encountered an error.", ex);
            }
        }));
        this.desktopMainMenuConfig = new MainMenuBuilder(this);
        this.mobileMainMenuConfig = new MainMenuBuilder(this);

        this.workflow = workflow;

        final LinkedHashSet<String> allResourcePaths = new LinkedHashSet<>();
        allResourcePaths.addAll(asList("", "ua/com/fielden/platform/web/"));
        allResourcePaths.addAll(asList(externalResourcePaths));
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
     * The same as {@link #AbstractWebUiConfig}, but without {@code ideaUri}.
     */
    public AbstractWebUiConfig(
            final String title,
            final Workflows workflow,
            final String[] externalResourcePaths,
            final boolean independentTimeZone,
            final Optional<MasterActionOptions> masterActionOptions) {
        this(title, workflow, externalResourcePaths, independentTimeZone, masterActionOptions, Optional.empty());
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
        final EntityMaster<ApplicationConfigEntity> applicationConfigMaster = StandardMastersWebUiConfig.createApplicationConfigMaster(injector(), desktopMainMenuConfig, mobileMainMenuConfig);
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
        final EntityMaster<PersistentEntityInfo> persistentEntityInfoMaster = StandardMastersWebUiConfig.createPersistentEntityInfoMaster(injector());
        final var shareEntityActionWebUiConfig = ShareActionWebUiConfig.register(injector());

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
        .addMaster(applicationConfigMaster)
        .addMaster(userMenuAssociatorWebUiConfig.master)
        .addMaster(userDefinableHelpMaster)
        .addMaster(persistentEntityInfoMaster)
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
        .addMaster(centreConfigurationWebUiConfig.overrideCentreConfigMaster)
        .addMaster(shareEntityActionWebUiConfig.master)
        ;
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
        final String indexSource = webUiBuilder.getAppIndex(injector().getInstance(IDates.class))
                .replace("@title", title)
                .replace("@ideaUri", ideaUri.orElse(""));
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
    public Stream<EntityActionConfig> streamActionConfigs() {
        return StreamUtils.concat(getCentres().values().stream().flatMap(EntityCentre::streamActionConfigs),
                                  getMasters().values().stream().flatMap(EntityMaster::streamActions),
                                  getExtraActions().stream(),
                                  configDesktopMainMenu().streamActionConfigs(),
                                  configMobileMainMenu().streamActionConfigs());
    }

    @Override
    public Collection<EntityActionConfig> getExtraActions() {
        return webUiBuilder.getExtraActions();
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
    protected static JsCode onSuccessfulShare() {
        return jsCode("""
            if (!self.shareError) { // 'self' is enclosing centre
                self._openShareAction();
            }
            """);
    }

    @Override
    public List<EntityActionConfig> centreConfigShareActions() {
        // default Share action is implemented specially through cached 'shareError' property in its 'preAction';
        // if Share is not possible it shows toast for an error, otherwise it immediately copies a link to the clipboard and shows toast for a success;
        // it is very important to copy link inside preAction as a part of UI callback, otherwise we get permission error in Safari browsers (see #2116)
        return asList(
            action(CentreConfigShareAction.class)
            .withContext(context().withSelectionCrit().build())
            .preAction(() -> jsCode(showToastForShareError().toString() + onSuccessfulShare().toString() + "return Promise.reject('Share action completed.');\n"))
            .icon("tg-icons:share")
            .shortDesc("Share")
            .longDesc("Share centre configuration")
            .withNoParentCentreRefresh()
            .build()
        );
    }

    @Override
    public Optional<EntityActionConfig> findAction(final CharSequence actionIdentifier) {
        return streamActionConfigs().filter(config -> config.actionIdentifier.filter(it -> it.contentEquals(actionIdentifier)).isPresent()).findAny();
    }

    @Override
    public void loadCentreGeneratedTypesAndCriteriaTypes(final Class<?> entityType) {
        final var critGenerator = injector.getInstance(ICriteriaGenerator.class);
        // load all centres (standalone and embedded) for concrete 'entityType' (with their generated types and criteria types)
        final var log = CREATE_DEFAULT_CONFIG_INFO.formatted(entityType.getSimpleName());
        logger.info(log);
        getCentres().entrySet().stream()
            .filter(entry -> entry.getValue().getEntityType().equals(entityType))
            .map(Entry::getKey)
            .forEach(miType -> critGenerator.generateCentreQueryCriteria(getDefaultCentre(miType, this)));
        logger.info(log + "done");
    }

    @Override
    public void createDefaultConfigurationsForAllCentres() {
        final var miTypes = getCentres().keySet();
        final var size = miTypes.size();
        final var logMessage = "Creating default configurations for [%s] centres (caching)...".formatted(size);
        logger.info(logMessage);

        // preload embedded centres map first
        getEmbeddedCentres();

        // preload all registered centres (including embedded) using getDefaultCentre(...) method;
        int embeddedSize = 0, genSize = 0, embeddedGenSize = 0;
        for (final var miType: miTypes) {
            final var isEmbedded = isEmbeddedCentre(miType);
            if (isEmbedded) {
                embeddedSize++;
            }
            // perform default config creation with heavy calculated properties processing:
            //   (use critGenerator.generateCentreQueryCriteria(getDefaultCentre(miType, this))) to generate also criteria entity type)
            final var centreManager = getDefaultCentre(miType, this);
            final var rootType = centreManager.getRepresentation().rootTypes().iterator().next();
            if (!centreManager.getEnhancer().getManagedType(rootType).equals(rootType)) {
                genSize++;
                if (isEmbedded) {
                    embeddedGenSize++;
                }
            }
        }
        logger.info("              all: %s standalone: %s embedded: %s".formatted(size, size - embeddedSize, embeddedSize));
        logger.info("    generated all: %s standalone: %s embedded: %s".formatted(genSize, genSize - embeddedGenSize, embeddedGenSize));
        logger.info(logMessage + "done");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Iterates through all registered {@link EntityMaster}s, determines whether they have embedded centres, and calculates map between {@code miType} of embedded centre and [{@link EntityCentre}; {@link EntityMaster}] pair.
     */
    @Override
    public Map<Class<? extends MiWithConfigurationSupport<?>>, T2<EntityCentre<?>, EntityMaster<? extends AbstractEntity<?>>>> getEmbeddedCentres() {
        if (embeddedCentreMap == null) {
            final String log = "    Calculating embedded centres...";
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

    private static Result validateIdeaUri(final String webAddress) {
        if (StringUtils.isBlank(webAddress)) {
            return successful();
        }

        final var validator = new UrlValidator(new String[] { HTTPS.name(), HTTPS.name().toLowerCase() }, ALLOW_LOCAL_URLS);
        if (!validator.isValid(webAddress)) {
            return failuref("Idea URI [%s] is not a valid HTTPS address.", webAddress);
        }
        return successful(webAddress);
    }

}
