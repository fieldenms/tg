package ua.com.fielden.platform.web.resources.webui;

import static ua.com.fielden.platform.utils.ResourceLoader.getStream;
import static ua.com.fielden.platform.web.resources.webui.FileResource.generateFileName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Injector;

import ua.com.fielden.platform.attachment.AttachmentPreviewEntityAction;
import ua.com.fielden.platform.basic.config.Workflows;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.EntityDeleteAction;
import ua.com.fielden.platform.entity.EntityDeleteActionProducer;
import ua.com.fielden.platform.entity.EntityEditAction;
import ua.com.fielden.platform.entity.EntityExportAction;
import ua.com.fielden.platform.entity.EntityNavigationAction;
import ua.com.fielden.platform.entity.EntityNewAction;
import ua.com.fielden.platform.menu.Menu;
import ua.com.fielden.platform.menu.MenuSaveAction;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.action.CentreConfigurationWebUiConfig;
import ua.com.fielden.platform.web.action.StandardMastersWebUiConfig;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.app.config.WebUiBuilder;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.custom_view.AbstractCustomView;
import ua.com.fielden.platform.web.interfaces.DeviceProfile;
import ua.com.fielden.platform.web.menu.IMainMenuBuilder;
import ua.com.fielden.platform.web.menu.impl.MainMenuBuilder;
import ua.com.fielden.platform.web.minijs.JsCode;
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

    /**
     * Creates abstract {@link IWebUiConfig}.
     *
     * @param title
     * @param externalResourcePaths
     * - additional root paths for file resources. (see {@link #resourcePaths} for more information).
     */
    public AbstractWebUiConfig(final String title, final Workflows workflow, final String[] externalResourcePaths) {
        this.title = title;
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
        } catch (final IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initConfiguration() {
        final EntityMaster<EntityNewAction> genericEntityNewActionMaster = StandardMastersWebUiConfig.createEntityNewMaster(injector());
        final EntityMaster<EntityEditAction> genericEntityEditActionMaster = StandardMastersWebUiConfig.createEntityEditMaster(injector());
        final EntityMaster<EntityNavigationAction> genericEntityNavigationActionMaster = StandardMastersWebUiConfig.createEntityNavigationMaster(injector());
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
        .addMaster(genericEntityNavigationActionMaster)
        .addMaster(attachmentPreviewMaster)
        .addMaster(genericEntityDeleteActionMaster)
        .addMaster(genericEntityExportActionMaster)
        .addMaster(genericMenuSaveMaster)
        .addMaster(new MenuWebUiConfig(injector(), desktopMainMenuConfig, mobileMainMenuConfig).master)
        // centre configuration management
        .addMaster(centreConfigurationWebUiConfig.centreConfigUpdater)
        .addMaster(centreConfigurationWebUiConfig.centreColumnWidthConfigUpdater)
        // centre config actions
        .addMaster(centreConfigurationWebUiConfig.centreConfigNewActionMaster)
        .addMaster(centreConfigurationWebUiConfig.centreConfigDuplicateActionMaster)
        .addMaster(centreConfigurationWebUiConfig.centreConfigLoadActionMaster)
        .addMaster(centreConfigurationWebUiConfig.centreConfigEditActionMaster)
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
        final Pair<DomElement, JsCode> generatedMenu = desktopMainMenuConfig.generateMenuActions();
        return ResourceLoader.getText("ua/com/fielden/platform/web/app/tg-app-template.js").
                replace("<!--menu action dom-->", generatedMenu.getKey().toString()).
                replace("//actionsObject", generatedMenu.getValue().toString());
    }

    @Override
    public final String genAppIndex() {
        final String indexSource = ResourceLoader.getText("ua/com/fielden/platform/web/index.html").replace("@title", title);
        if (isDevelopmentWorkflow(this.workflow)) {
            return indexSource.replace("@startupResources", "startup-resources-origin");
        } else {
            return indexSource.replace("@startupResources", "startup-resources-vulcanized");
        }
    }

    private static boolean isDevelopmentWorkflow(final Workflows workflow) {
        return Workflows.development.equals(workflow) || Workflows.vulcanizing.equals(workflow);
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
        logger.error("Clearing configurations...done");
    }

    @Override
    public Menu getMenuEntity(final DeviceProfile deviceProfile) {
        return DeviceProfile.DESKTOP.equals(deviceProfile) ? desktopMainMenuConfig.getMenu() : mobileMainMenuConfig.getMenu();
    }
    
    @Override
    public String checksum(final String resourceURI) {
        return checksums.get(resourceURI);
    }
    
}
