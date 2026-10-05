package ua.com.fielden.platform.web.resources.webui;

import com.google.inject.Inject;
import org.junit.After;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.EntityCentreConfigCo;
import ua.com.fielden.platform.ui.menu.sample.MiUserRole;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.ICentreConfigSharingModel;
import ua.com.fielden.platform.web.centre.LoadableCentreConfig;
import ua.com.fielden.platform.web.interfaces.DeviceProfile;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.test.AbstractWebResourceWithDaoTestCase;
import ua.com.fielden.platform.web.resources.webui.CentreResource.DiscardedCentre;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.findLoadableConfig;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.inherited;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.inheritedFromBase;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.prepareDefaultCentre;
import static ua.com.fielden.platform.web.centre.CentreUpdater.*;
import static ua.com.fielden.platform.web.centre.CentreUpdaterUtils.*;
import static ua.com.fielden.platform.web.centre.CentreUtils.isFreshCentreChanged;
import static ua.com.fielden.platform.web.resources.webui.CentreResource.discardCentre;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.createCriteriaValidationPrototype;
import static ua.com.fielden.platform.web.resources.webui.CriteriaResource.loadConfigByUuid;
import static ua.com.fielden.platform.web.resources.webui.CriteriaResource.loadLinkConfig;
import static ua.com.fielden.platform.web.resources.webui.CriteriaResource.loadPreferredConfig;

/// A base class for tests of Entity Centre configurations.
///
/// Configurations are driven through the operations a user performs in the application.
/// They belong to the web resources of the standalone User Roles centre, and to the actions on its configurations.
/// The device profile of the request being served is set with [#on(DeviceProfile)].
///
public abstract class AbstractEntityCentreConfigTestCase extends AbstractWebResourceWithDaoTestCase {

    protected static final Class<MiUserRole> MI_TYPE = MiUserRole.class;

    protected static final fetch<EntityCentreConfig> CONFIG_FETCH = FETCH_CONFIG_AND_INSTRUMENT
        .with("preferred").with("preferredOnMobile").with("configUuid").with("configBody")
        .with("runAutomatically").with("dashboardable");

    @Inject protected ICompanionObjectFinder coFinder;
    @Inject protected IWebUiConfig webUiConfig;
    @Inject protected ICriteriaGenerator critGenerator;
    @Inject protected ICentreConfigSharingModel sharingModel;

    @After
    public void forgetTheDevice() {
        getInstance(IDeviceProvider.class).setDeviceProfile(null);
    }

    //////////////////////////////////// Users ////////////////////////////////////

    /// Creates a base user if `basedOnUser` is `null`, and a user derived from `basedOnUser` otherwise.
    ///
    protected User newUser(final String name, final User basedOnUser) {
        return new_(User.class, name)
            .setBase(basedOnUser == null)
            .setBasedOnUser(basedOnUser)
            .setEmail(name + "@unit-test.software")
            .setActive(true);
    }

    protected User findUser(final String name) {
        final IUser coUser = coFinder.find(User.class);
        return coUser.findUser(name);
    }

    //////////////////////////////////// Operations ////////////////////////////////////

    /// Makes `device` the device profile of the request being served.
    ///
    protected void on(final DeviceProfile device) {
        getInstance(IDeviceProvider.class).setDeviceProfile(device);
    }

    /// Opens the `saveAsName`d configuration of `owner`, as loading the centre in the application does.
    /// This initialises its FRESH, SAVED and PREVIOUSLY_RUN centres, where they are missing.
    ///
    protected EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> open(
        final User owner,
        final Optional<String> saveAsName
    ) {
        final var freshCentre = updateCentre(owner, MI_TYPE, FRESH_CENTRE_NAME, saveAsName, webUiConfig, coFinder);
        updateCentre(owner, MI_TYPE, SAVED_CENTRE_NAME, saveAsName, webUiConfig, coFinder);
        updateCentre(owner, MI_TYPE, PREVIOUSLY_RUN_CENTRE_NAME, saveAsName, webUiConfig, coFinder);
        return createCriteriaValidationPrototype(
            MI_TYPE,
            saveAsName,
            freshCentre,
            coFinder,
            critGenerator,
            0L,
            owner,
            webUiConfig,
            sharingModel
        );
    }

    /// Opens the configuration with `configUuid` for `owner`, as opening a centre URI with that uuid does.
    /// This is also how a configuration selected in the Load dialog gets opened.
    /// Returns the name of the opened configuration.
    ///
    protected Optional<String> openByUuid(final User owner, final String configUuid) {
        final var saveAsName = loadConfigByUuid(configUuid, owner, MI_TYPE, webUiConfig, coFinder, sharingModel)._1;
        open(owner, saveAsName);
        return saveAsName;
    }

    /// Opens a centre URI with criteria parameters, which loads the link configuration of `owner`.
    /// Returns the `configUuid` of the link configuration.
    ///
    protected String openLink(final User owner) {
        final var saveAsNameAndConfigUuid = loadLinkConfig(owner, MI_TYPE, webUiConfig, coFinder);
        open(owner, saveAsNameAndConfigUuid._1);
        return saveAsNameAndConfigUuid._2.orElseThrow();
    }

    /// Opens the centre URI without a uuid, after the centre has been loaded before.
    /// This loads the default configuration, which becomes preferred.
    ///
    protected void openDefault(final User owner) {
        open(owner, loadPreferredConfig(true, owner, MI_TYPE, webUiConfig, coFinder, sharingModel)._1);
    }

    /// Saves the configuration that `criteria` is open with as a new one named `saveAsName`.
    /// This is what the Save As action does.
    /// The new configuration becomes preferred.
    ///
    protected void saveAs(final EnhancedCentreEntityQueryCriteria<?, ?> criteria, final String saveAsName) {
        withUnchangedCriteria(criteria).saveCentre(saveAsName, "%s description".formatted(saveAsName), false, null);
    }

    /// Changes the title of the configuration that `criteria` is open with to `newSaveAsName`, as the Edit action does.
    ///
    protected void edit(final EnhancedCentreEntityQueryCriteria<?, ?> criteria, final String newSaveAsName) {
        withUnchangedCriteria(criteria).editCentre(newSaveAsName, "%s description".formatted(newSaveAsName), false, null);
    }

    /// Loads the `saveAsName`d configuration from the Load dialog of the centre that `criteria` is open with.
    /// This mirrors `CentreConfigLoadActionDao.save`, which restores `criteria` from the context sent by the client.
    ///
    /// A configuration inherited from base or shared is updated from upstream first.
    /// The loaded configuration becomes preferred, unless it is inherited from shared.
    /// Returns the name of the loaded configuration.
    ///
    protected Optional<String> load(final EnhancedCentreEntityQueryCriteria<?, ?> criteria, final String saveAsName) {
        final var loadableConfig = findLoadableConfig(of(saveAsName), criteria);
        if (inherited(loadableConfig).isPresent()) {
            if (inheritedFromBase(loadableConfig).isPresent()) {
                criteria.updateInheritedFromBaseCentre(saveAsName);
            } else {
                return criteria.updateInheritedFromSharedCentre(saveAsName, loadableConfig.get().getConfig().getConfigUuid());
            }
        }
        criteria.makePreferredConfig(of(saveAsName));
        return of(saveAsName);
    }

    /// Deletes the configuration that `criteria` is open with, as the Delete action does.
    /// The default configuration is then loaded, and becomes preferred.
    ///
    protected void delete(final EnhancedCentreEntityQueryCriteria<?, ?> criteria) {
        criteria.deleteCentre();
        prepareDefaultCentre(criteria);
    }

    /// Discards the changes of the `saveAsName`d configuration of `owner`, as the Discard action does.
    ///
    protected DiscardedCentre discard(final User owner, final Optional<String> saveAsName) {
        return discardCentre(saveAsName, owner, MI_TYPE, webUiConfig, coFinder, sharingModel);
    }

    protected static Consumer<ICentreDomainTreeManagerAndEnhancer> withPageCapacity(final int pageCapacity) {
        return centre -> centre.getSecondTick().setPageCapacity(pageCapacity);
    }

    /// Sets the modifications holder that the client sends for an unchanged criteria form.
    /// Actions that respond with the resulting criteria entity require it.
    ///
    private EnhancedCentreEntityQueryCriteria<?, ?> withUnchangedCriteria(final EnhancedCentreEntityQueryCriteria<?, ?> criteria) {
        final var modifHolder = new HashMap<String, Object>(Map.of(VERSION, 0, "@@metaValues", Map.of()));
        criteria.setCentreContextHolder(new_(CentreContextHolder.class).setModifHolder(modifHolder));
        return criteria;
    }

    //////////////////////////////////// Inspection ////////////////////////////////////

    /// The preferred configuration of `owner` on the device profile of the request being served.
    ///
    protected Optional<String> preferred(final User owner) {
        return retrievePreferredConfigName(owner, MI_TYPE, coFinder, webUiConfig);
    }

    /// The configurations of `owner` in the Load dialog, by their names.
    ///
    protected Map<String, LoadableCentreConfig> loadable(final User owner) {
        return loadableConfigurations(owner, MI_TYPE, coFinder, sharingModel).apply(empty()).stream()
            .collect(toMap(LoadableCentreConfig::getKey, identity()));
    }

    protected static void assertOwn(final Map<String, LoadableCentreConfig> configs, final String saveAsName) {
        final var config = configs.get(saveAsName);
        assertNotNull("[%s] must be loadable.".formatted(saveAsName), config);
        assertFalse("[%s] must be an own configuration.".formatted(saveAsName), config.isInherited());
        assertNull("[%s] must not be reported as orphaned.".formatted(saveAsName), config.getOrphanedSharingMessage());
    }

    protected static void assertInheritedFromBase(final Map<String, LoadableCentreConfig> configs, final String saveAsName) {
        final var config = configs.get(saveAsName);
        assertNotNull("[%s] must be loadable.".formatted(saveAsName), config);
        assertTrue("[%s] must be inherited.".formatted(saveAsName), config.isInherited());
        assertTrue("[%s] must be inherited from base.".formatted(saveAsName), config.isBase());
    }

    protected static void assertInheritedFromShared(
        final Map<String, LoadableCentreConfig> configs,
        final String saveAsName,
        final User sharer
    ) {
        final var config = configs.get(saveAsName);
        assertNotNull("[%s] must be loadable.".formatted(saveAsName), config);
        assertTrue("[%s] must be inherited.".formatted(saveAsName), config.isInherited());
        assertTrue("[%s] must be inherited from shared.".formatted(saveAsName), config.isShared());
        assertEquals(sharer, config.getSharedBy());
    }

    protected void assertOpensUnchanged(final User owner, final String saveAsName) {
        final var freshCentre = updateCentre(owner, MI_TYPE, FRESH_CENTRE_NAME, of(saveAsName), webUiConfig, coFinder);
        final var savedCentre = updateCentre(owner, MI_TYPE, SAVED_CENTRE_NAME, of(saveAsName), webUiConfig, coFinder);
        assertFalse("[%s] must open unchanged.".formatted(saveAsName), isFreshCentreChanged(freshCentre, savedCentre));
    }

    protected String uuidOf(final User owner, final String saveAsName) {
        return config(owner, FRESH_CENTRE_NAME, of(saveAsName)).getConfigUuid();
    }

    /// Finds the `surrogateName` centre of the `saveAsName`d configuration of `owner`.
    ///
    protected EntityCentreConfig config(final User owner, final String surrogateName, final Optional<String> saveAsName) {
        return find(owner, NAME_OF.apply(surrogateName).apply(saveAsName));
    }

    protected Optional<EntityCentreConfig> configOpt(final User owner, final String surrogateName, final Optional<String> saveAsName) {
        return findConfigOpt(MI_TYPE, owner, NAME_OF.apply(surrogateName).apply(saveAsName), coFinder, CONFIG_FETCH);
    }

    protected EntityCentreConfig find(final User owner, final String title) {
        return findConfigOpt(MI_TYPE, owner, title, coFinder, CONFIG_FETCH)
            .orElseThrow(() -> new IllegalStateException("Configuration [%s] of [%s] does not exist.".formatted(title, owner)));
    }

    protected List<EntityCentreConfig> allConfigs() {
        final EntityCentreConfigCo co = coFinder.find(EntityCentreConfig.class);
        return co.getAllEntities(from(select(EntityCentreConfig.class).model()).with(CONFIG_FETCH).model());
    }

}
