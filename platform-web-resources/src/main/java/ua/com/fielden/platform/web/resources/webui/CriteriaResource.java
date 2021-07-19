package ua.com.fielden.platform.web.resources.webui;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static ua.com.fielden.platform.data.generator.IGenerator.FORCE_REGENERATION_KEY;
import static ua.com.fielden.platform.data.generator.IGenerator.shouldForceRegeneration;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.streaming.ValueCollectors.toLinkedHashMap;
import static ua.com.fielden.platform.types.either.Either.left;
import static ua.com.fielden.platform.types.either.Either.right;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.EntityUtils.areEqual;
import static ua.com.fielden.platform.utils.EntityUtils.equalsEx;
import static ua.com.fielden.platform.web.action.CentreConfigShareActionProducer.CONFIG_DOES_NOT_EXIST;
import static ua.com.fielden.platform.web.centre.CentreConfigUpdaterUtils.applyNewOrderVisibilityAndSorting;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.isDefaultOrLink;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.isInherited;
import static ua.com.fielden.platform.web.centre.CentreUpdater.FRESH_CENTRE_NAME;
import static ua.com.fielden.platform.web.centre.CentreUpdater.NAME_OF;
import static ua.com.fielden.platform.web.centre.CentreUpdater.PREVIOUSLY_RUN_CENTRE_NAME;
import static ua.com.fielden.platform.web.centre.CentreUpdater.SAVED_CENTRE_NAME;
import static ua.com.fielden.platform.web.centre.CentreUpdater.commitCentre;
import static ua.com.fielden.platform.web.centre.CentreUpdater.commitCentreWithoutConflicts;
import static ua.com.fielden.platform.web.centre.CentreUpdater.loadableConfigurations;
import static ua.com.fielden.platform.web.centre.CentreUpdater.makePreferred;
import static ua.com.fielden.platform.web.centre.CentreUpdater.obtainTitleFrom;
import static ua.com.fielden.platform.web.centre.CentreUpdater.removeCentres;
import static ua.com.fielden.platform.web.centre.CentreUpdater.retrievePreferredConfigName;
import static ua.com.fielden.platform.web.centre.CentreUpdater.updateCentre;
import static ua.com.fielden.platform.web.centre.CentreUpdater.updateCentreConfigUuid;
import static ua.com.fielden.platform.web.centre.CentreUpdater.updateCentreDesc;
import static ua.com.fielden.platform.web.centre.CentreUpdaterUtils.FETCH_CONFIG;
import static ua.com.fielden.platform.web.centre.CentreUpdaterUtils.FETCH_CONFIG_AND_INSTRUMENT;
import static ua.com.fielden.platform.web.centre.CentreUpdaterUtils.findConfigOpt;
import static ua.com.fielden.platform.web.centre.CentreUpdaterUtils.findConfigOptByUuid;
import static ua.com.fielden.platform.web.centre.CentreUpdaterUtils.saveNewEntityCentreManager;
import static ua.com.fielden.platform.web.centre.CentreUtils.isFreshCentreChanged;
import static ua.com.fielden.platform.web.centre.WebApiUtils.LINK_CONFIG_TITLE;
import static ua.com.fielden.platform.web.factories.webui.ResourceFactoryUtils.extractSaveAsName;
import static ua.com.fielden.platform.web.factories.webui.ResourceFactoryUtils.wasLoadedPreviouslyAndConfigUuid;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.CENTRE_DIRTY;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.META_VALUES;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.STALE_CRITERIA_MESSAGE;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.createCriteriaEntityWithoutConflicts;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.createCriteriaMetaValues;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.createCriteriaMetaValuesCustomObject;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.createCriteriaMetaValuesCustomObjectWithResult;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.createCriteriaMetaValuesCustomObjectWithSaveAsInfo;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.createCriteriaValidationPrototype;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.isAutoRunning;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.isRunning;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.isSorting;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.removeWasRunIndication;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.updateInheritedFromShared;
import static ua.com.fielden.platform.web.resources.webui.EntityValidationResource.VALIDATION_COUNTER;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.getEntityType;
import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.handleUndesiredExceptions;
import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.restoreCentreContextHolder;
import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.restoreModifiedPropertiesHolderFrom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.data.generator.IGenerator;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancerCache;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.impl.CalculatedProperty;
import ua.com.fielden.platform.domaintree.impl.EnhancementPropertiesMap;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.types.either.Either;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfig;
import ua.com.fielden.platform.ui.config.api.IMainMenuItem;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.ICentreConfigSharingModel;
import ua.com.fielden.platform.web.centre.IQueryEnhancer;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig.ResultSetProp;
import ua.com.fielden.platform.web.centre.api.actions.multi.IEntityMultiActionSelector;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.resultset.ICustomPropsAssignmentHandler;
import ua.com.fielden.platform.web.centre.api.resultset.IRenderingCustomiser;
import ua.com.fielden.platform.web.centre.api.resultset.PropDef;
import ua.com.fielden.platform.web.interfaces.DeviceProfile;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * The web resource for criteria serves as a back-end mechanism of criteria retrieval. It provides a base implementation for handling the following methods:
 * <ul>
 * <li>retrieve entity -- GET request.
 * </ul>
 *
 * @author TG Team
 *
 */
public class CriteriaResource extends AbstractWebResource {
    private static final Logger logger = Logger.getLogger(CriteriaResource.class);
    private static final String CONFIG_COULD_NOT_BE_SHARED_WITH_BASE_USER = "No configuration can be shared with base users, e.g. with %s.";
    private static final String LINK_CONFIG_COULD_NOT_BE_SHARED = "Link configurations cannot be shared.";
    private static final String CONFLICTING_TITLE_SUFFIX = " (shared%s)";
    private static final String COULD_NOT_LOAD_CONFLICTING_SHARED_CONFIGURATION = "Cannot load a shared configuration with conflicting title [%s].";
    private static final String LINK_CONFIG_COULD_NOT_BE_LOADED = "A link configuration could not be loaded. Please try again.";
    private static final String ERR_STALE_CRITERIA = "Selection criteria have been changed, but not applied. "
                                                     + "Previously applied values are in effect. "
                                                     + "Please tap <b>RUN</b> to apply the updated selection criteria.";
    private final RestServerUtil restUtil;
    private final ICompanionObjectFinder companionFinder;
    private final ICriteriaGenerator critGenerator;
    private final EntityCentre<AbstractEntity<?>> centre;
    private final IWebUiConfig webUiConfig;
    private final IUserProvider userProvider;
    private final EntityFactory entityFactory;
    private final IDomainTreeEnhancerCache domainTreeEnhancerCache;
    private final ICentreConfigSharingModel sharingModel;
    private User user;
    private IEntityCentreConfig eccCompanion;
    private IMainMenuItem mmiCompanion;
    private IUser userCompanion;
    private Class<? extends MiWithConfigurationSupport<?>> miType;

    public CriteriaResource(
            final RestServerUtil restUtil,
            final EntityCentre<AbstractEntity<?>> centre,
            final IDomainTreeEnhancerCache domainTreeEnhancerCache,
            final IWebUiConfig webUiConfig,
            final ICompanionObjectFinder companionFinder,
            final IUserProvider userProvider,
            final IDeviceProvider deviceProvider,
            final IDates dates,
            final ICriteriaGenerator critGenerator,
            final EntityFactory entityFactory,
            final ICentreConfigSharingModel sharingModel,
            final Context context,
            final Request request,
            final Response response) {
        super(context, request, response, deviceProvider, dates);

        this.restUtil = restUtil;
        this.companionFinder = companionFinder;

        this.centre = centre;
        this.critGenerator = critGenerator;

        this.domainTreeEnhancerCache = domainTreeEnhancerCache;
        this.webUiConfig = webUiConfig;
        this.userProvider = userProvider;
        this.entityFactory = entityFactory;
        this.sharingModel = sharingModel;
    }

    /**
     * Handles GET requests resulting from tg-selection-criteria <code>retrieve()</code> method (new entity).
     */
    @Get
    @Override
    public Representation get() {
        return handleUndesiredExceptions(getResponse(), () -> {
            miType = centre.getMenuItemType();
            user = userProvider.getUser();
            eccCompanion = companionFinder.find(EntityCentreConfig.class);
            mmiCompanion = companionFinder.find(MainMenuItem.class);
            userCompanion = companionFinder.find(User.class);
            final T2<Boolean, Optional<String>> wasLoadedPreviouslyAndConfigUuid = wasLoadedPreviouslyAndConfigUuid(getRequest());
            final boolean wasLoadedPreviously = wasLoadedPreviouslyAndConfigUuid._1;
            final Optional<String> configUuid = wasLoadedPreviouslyAndConfigUuid._2;
            final Optional<String> actualSaveAsName;
            final Optional<String> resolvedConfigUuid;
            if (!getQuery().isEmpty()) {
                // start loading of link configuration with parameters;
                // create two empty FRESH / SAVED centres if not yet created;
                // return saveAsName and generated configUuid for further processing
                final T2<Optional<String>, Optional<String>> saveAsNameAndConfigUuid = prepareLinkConfigInfrastructure();
                actualSaveAsName = saveAsNameAndConfigUuid._1;
                resolvedConfigUuid = saveAsNameAndConfigUuid._2;
                // empty link config is taken from SAVED surrogate centre (which is always empty);
                final ICentreDomainTreeManagerAndEnhancer emptyCentre = updateCentre(user, miType, SAVED_CENTRE_NAME, actualSaveAsName, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);
                // clear current 'link' surrogate FRESH centre -- this is to make it empty before applying new selection criteria parameters (client-side action after this request's response will be delivered);
                commitCentre(user, miType, FRESH_CENTRE_NAME, actualSaveAsName, device(), emptyCentre, null /* newDesc */, webUiConfig, eccCompanion, mmiCompanion, userCompanion);
            } else if (configUuid.isPresent()) {
                // start loading of configuration defined by concrete uuid;
                // we look only through [link, own save-as, inherited from base, inherited from shared] set of configurations;
                // default configurations are excluded in the lookup;
                // only FRESH kind are looked for;
                final Optional<EntityCentreConfig> freshConfigOpt = findConfigOptByUuid(configUuid.get(), user, miType, device(), FRESH_CENTRE_NAME, eccCompanion);
                if (freshConfigOpt.isPresent()) {
                    // for current user we already have FRESH configuration with uuid loaded;
                    final Optional<String> preliminarySaveAsName = of(obtainTitleFrom(freshConfigOpt.get().getTitle(), FRESH_CENTRE_NAME, device()));
                    // updating is required from upstream configuration;
                    if (!LINK_CONFIG_TITLE.equals(preliminarySaveAsName.get())) { // (but not for link configuration);
                        actualSaveAsName = updateFromUpstream(configUuid.get(), preliminarySaveAsName);
                    } else {
                        actualSaveAsName = preliminarySaveAsName;
                    }
                } else {
                    // if there is no FRESH configuration then there are no [link, own save-as] configuration with the specified uuid;
                    // however there can exist [base, shared] config for other user with the specified uuid;
                    actualSaveAsName = firstTimeLoadingFrom(validateUuidAndGetUpstreamConfig(configUuid.get()).orElseThrow(Result::asRuntime));
                }
                // configuration being loaded need to become preferred
                if (!LINK_CONFIG_TITLE.equals(actualSaveAsName.get()) && !webUiConfig.getCentres().get(miType).isRunAutomatically()) {
                    makePreferred(user, miType, actualSaveAsName, device(), companionFinder);
                }
                resolvedConfigUuid = configUuid;
            } else {
                if (!wasLoadedPreviously) { // client-driven first time loading of centre's selection criteria
                    actualSaveAsName = retrievePreferredConfigName(user, miType, device(), companionFinder); // preferred configuration should be loaded
                    resolvedConfigUuid = updateCentreConfigUuid(user, miType, actualSaveAsName, device(), eccCompanion);
                } else {
                    actualSaveAsName = empty(); // in case where first time loading has been occurred earlier we still prefer configuration specified by absence of uuid: default
                    if (!webUiConfig.getCentres().get(miType).isRunAutomatically()) {
                        makePreferred(user, miType, actualSaveAsName, device(), companionFinder); // most likely transition from save-as configuration has been occurred and need to update preferred config; in other case we can go to other centre and back from already loaded default config and this call will make default config preferred again
                    }
                    resolvedConfigUuid = empty();
                }
            }
            final ICentreDomainTreeManagerAndEnhancer updatedFreshCentre = updateCentre(user, miType, FRESH_CENTRE_NAME, actualSaveAsName, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);
            final String customDesc = updateCentreDesc(user, miType, actualSaveAsName, device(), eccCompanion);
            return createCriteriaRetrievalEnvelope(updatedFreshCentre, miType, actualSaveAsName, user, restUtil, companionFinder, critGenerator, device(), customDesc, resolvedConfigUuid, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, sharingModel);
        }, restUtil);
    }
    
    /**
     * Validates {@code configUuid} on the subject of configuration existence and general ability to share it with current {@code user}.
     */
    private Either<Result, EntityCentreConfig> validateUuidAndGetUpstreamConfig(final String configUuid) {
        // we look only for owners; "owning" is indicated by presence of SAVED configuration with the specified uuid
        final Optional<EntityCentreConfig> savedConfigOptForOtherUser = findConfigOptByUuid(configUuid, miType, device(), SAVED_CENTRE_NAME, eccCompanion);
        if (!savedConfigOptForOtherUser.isPresent()) {
            // configuration does not exist (no SAVED surrogate centre) -- legitimate error; this can happen if configuration has been already deleted or didn't exist due to URI mistyping
            return left(failure(CONFIG_DOES_NOT_EXIST));
        } else if (user.isBase()) {
            // current user is base user;
            // we have configuration owner not equal to current user (because FRESH config with this uuid for current user didn't exist);
            // base user is not based on any other;
            // so from two categories [base, shared] we can only consider [shared];
            // so, at this stage, we prohibit loading of [inherited from shared] configurations for base users -- not really practical scenario and possibly will never be required
            return left(failure(format(CONFIG_COULD_NOT_BE_SHARED_WITH_BASE_USER, user)));
        } else if (LINK_CONFIG_TITLE.equals(obtainTitleFrom(savedConfigOptForOtherUser.get().getTitle(), SAVED_CENTRE_NAME, device()))) {
            // link-configs can not be shared anywhere neither from base user nor from base/non-base user that gave its uuid as part of sharing process
            return left(failure(LINK_CONFIG_COULD_NOT_BE_SHARED));
        }
        return right(savedConfigOptForOtherUser.get());
    }

    /**
     * Implements first time loading of configuration into 'inherited from base / shared'.
     */
    private Optional<String> firstTimeLoadingFrom(final EntityCentreConfig upstreamConfig) {
        final String configUuid = upstreamConfig.getConfigUuid();
        final User upstreamConfigCreator = upstreamConfig.getOwner();
        final String preliminarySaveAsName = obtainTitleFrom(upstreamConfig.getTitle(), SAVED_CENTRE_NAME, device());
        final Optional<String> actualSaveAsName;
        if (upstreamConfigCreator.isBase() && areEqual(upstreamConfigCreator, user.getBasedOnUser() /*id-only-proxy*/)) {
            // we have base => basedOn relationship between current user and the creator of savedConfig;
            // we now know the actualSaveAsName from which the configuration should be updated;
            // CentreUpdater.updateCentre and .updateDifferences method should take care of that process;
            // at least FRESH config should be prepared -- making it preferred requires existence
            actualSaveAsName = of(preliminarySaveAsName);
            updateCentre(user, miType, FRESH_CENTRE_NAME, actualSaveAsName, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);
        } else {
            // if current user does not have access to shared configuration then sharing process should be prevented
            sharingModel.isSharedWith(configUuid, user).ifFailure(Result::throwRuntime);
            // current user gets uuid as part of sharing process from other base/non-base user;
            // need to determine non-conflicting name for current user from preliminarySaveAsName
            actualSaveAsName = of(determineNonConflictingName(preliminarySaveAsName, -1));
            final Function<String, Function<String, Consumer<Optional<String>>>> createInheritedFromShared = surrogateName -> newDescription -> uuid -> saveNewEntityCentreManager(
                true,
                upstreamConfig.getConfigBody(),
                miType,
                user,
                NAME_OF.apply(surrogateName).apply(actualSaveAsName).apply(device()),
                newDescription,
                eccCompanion,
                mmiCompanion,
                ecc -> uuid.map(u -> ecc.setConfigUuid(u)).orElse(ecc)
            );
            final EntityCentreConfig freshConfigForCreator = findConfigOptByUuid(configUuid, upstreamConfigCreator, miType, device(), FRESH_CENTRE_NAME, eccCompanion).get(); // need to retrieve FRESH config to get 'desc' -- that's because SAVED centres haven't stored descriptions, only FRESH do; this config must be present, otherwise savedConfigForOtherUser would not exist
            createInheritedFromShared.apply(FRESH_CENTRE_NAME).apply(freshConfigForCreator.getDesc()).accept(of(configUuid)); // update (FRESH only) with upstream description and configUuid during creation
            createInheritedFromShared.apply(SAVED_CENTRE_NAME).apply(null).accept(empty());
        }
        return actualSaveAsName;
    }

    /**
     * Determines name of inherited from shared configuration.
     * Usually it is the same as {@code preliminaryName} (if there is no config with this name).
     * <p>
     * If there is conflicting name then '(shared)' suffix is added.
     * If even that did not work -- '(shared 1)' -> '(shared 9)' suffixes are tried.
     * If even that did not work -- error is thrown.
     */
    private String determineNonConflictingName(final String preliminaryName, final int index) {
        final String name;
        if (index > 9) {
            throw failure(format(COULD_NOT_LOAD_CONFLICTING_SHARED_CONFIGURATION, preliminaryName));
        } else {
            name = preliminaryName + (index == -1 ? "" : format(CONFLICTING_TITLE_SUFFIX, index == 0 ? "" : " " + index));
        }
        return findConfigOpt(miType, user, NAME_OF.apply(FRESH_CENTRE_NAME).apply(of(name)).apply(device()), eccCompanion, FETCH_CONFIG)
            .map(conflictingConfig -> determineNonConflictingName(preliminaryName, index + 1))
            .orElse(name);
    }

    /**
     * Updates already loaded by {@code user} configuration with concrete {@code configUuid} from its upstream configuration (if it is inherited).
     */
    private Optional<String> updateFromUpstream(final String configUuid, final Optional<String> saveAsName) {
        // look for config creator
        final Optional<EntityCentreConfig> savedConfigOpt = findConfigOptByUuid(configUuid, miType, device(), SAVED_CENTRE_NAME, eccCompanion);
        if (savedConfigOpt.isPresent()) {
            // the creator is current user or other
            final EntityCentreConfig savedConfig = savedConfigOpt.get();
            final User savedConfigCreator = savedConfig.getOwner();
            if (!areEqual(savedConfigCreator, user)) {
                // current user didn't create this config -> it is inherited and needs updating
                if (savedConfigCreator.isBase() && areEqual(savedConfigCreator, user.getBasedOnUser() /*id-only-proxy*/)) {
                    // inherited from base
                    if (isCentreChanged(saveAsName)) { // if there are some user changes, only SAVED surrogate must be updated; if such centre will be discarded the base user changes will be loaded immediately
                        removeCentres(user, miType, device(), saveAsName, eccCompanion, SAVED_CENTRE_NAME);
                    } else { // otherwise base user changes will be loaded immediately after centre loading
                        removeCentres(user, miType, device(), saveAsName, eccCompanion, FRESH_CENTRE_NAME, SAVED_CENTRE_NAME);
                    }
                    updateCentre(user, miType, FRESH_CENTRE_NAME, saveAsName, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);
                    updateCentre(user, miType, SAVED_CENTRE_NAME, saveAsName, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder); // do not leave only FRESH centre out of two (FRESH + SAVED) => update SAVED centre explicitly
                } else {
                    if (sharingModel.isSharedWith(configUuid, user).isSuccessful()) {
                        // inherited from shared
                        updateInheritedFromShared(savedConfig, miType, device(), saveAsName, user, eccCompanion, of(() -> isCentreChanged(saveAsName)));
                        return of(obtainTitleFrom(savedConfig.getTitle(), SAVED_CENTRE_NAME, device()));
                    } // already loaded inherited from shared config was made unshared; the inherited from shared configuration now acts like own save-as configuration
                }
            } // if the current user is creator then no 'updating from upstream' is needed -- it is own save-as
        } // else, there are no creator for this config; it means that it was shared / based but original config deleted; the inherited from shared / base configuration acts like own save-as configuration
        return saveAsName;
    }

    /**
     * Prepares FRESH / SAVED link configs if not yet created. Returns a pair of resultant saveAsName and configUuid.
     */
    private T2<Optional<String>, Optional<String>> prepareLinkConfigInfrastructure() {
        // 'link' configuration loading is not limited only to first time loading;
        // user can paste 'link' configuration URI into current app context;
        // usually it will look like default config URI (no uuid) with appended params;
        // however it is possible for user (or programmatically) to append params to save-as/link/inherited configuration that has uuid;
        // in that case it still should act as applying those params against empty configuration on 'link' configuration infrastructure
        final Optional<String> actualSaveAsName = of(LINK_CONFIG_TITLE); // 'link' configuration should saveAsName
        // ensure that FRESH link centre is present (creates automatically without configUuid if not)
        updateCentre(user, miType, FRESH_CENTRE_NAME, actualSaveAsName, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);
        // create configUuids there if not yet present
        final Optional<EntityCentreConfig> freshConfigOpt = findConfigOpt(miType, user, NAME_OF.apply(FRESH_CENTRE_NAME).apply(actualSaveAsName).apply(device()), eccCompanion, FETCH_CONFIG_AND_INSTRUMENT.with("configUuid"));
        if (!freshConfigOpt.isPresent()) {
            throw failure(LINK_CONFIG_COULD_NOT_BE_LOADED); // this should never happen, but just in case return a little bit more meaningful message
        } else if (freshConfigOpt.get().getConfigUuid() == null) {
            // if FRESH config does not have uuid yet then it was created just recently;
            // so create SAVED config first;
            updateCentre(user, miType, SAVED_CENTRE_NAME, actualSaveAsName, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);
            // and update both with newly generated config uuid
            final String newConfigUuid = randomUUID().toString();
            eccCompanion.saveWithConflicts(freshConfigOpt.get().setConfigUuid(newConfigUuid));
            findConfigOpt(miType, user, NAME_OF.apply(SAVED_CENTRE_NAME).apply(actualSaveAsName).apply(device()), eccCompanion, FETCH_CONFIG_AND_INSTRUMENT.with("configUuid"))
                .ifPresent(savedConfig -> eccCompanion.saveWithConflicts(savedConfig.setConfigUuid(newConfigUuid)));
            return t2(actualSaveAsName, of(newConfigUuid));
        } else {
            return t2(actualSaveAsName, of(freshConfigOpt.get().getConfigUuid()));
        }
    }

    /**
     * Returns whether FRESH config is changed from SAVED one.
     */
    private boolean isCentreChanged(final Optional<String> actualSaveAsName) {
        return isFreshCentreChanged(
            updateCentre(user, miType, FRESH_CENTRE_NAME, actualSaveAsName, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder),
            updateCentre(user, miType, SAVED_CENTRE_NAME, actualSaveAsName, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder)
        );
    }

    /**
     * Handles POST request resulting from tg-selection-criteria <code>validate()</code> method.
     */
    @Post
    @Override
    public Representation post(final Representation envelope) {
        return handleUndesiredExceptions(getResponse(), () -> {
            final Optional<String> saveAsName = extractSaveAsName(getRequest());
            eccCompanion = companionFinder.find(EntityCentreConfig.class);
            mmiCompanion = companionFinder.find(MainMenuItem.class);
            userCompanion = companionFinder.find(User.class);
            miType = centre.getMenuItemType();
            user = userProvider.getUser();
            final Map<String, Object> modifiedPropertiesHolder = restoreModifiedPropertiesHolderFrom(envelope, restUtil);
            final DeviceProfile device = device();
            final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> appliedCriteriaEntity = createCriteriaEntityWithoutConflicts(modifiedPropertiesHolder, companionFinder, critGenerator, miType, saveAsName, user, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, sharingModel);
            final ICentreDomainTreeManagerAndEnhancer updatedFreshCentre = appliedCriteriaEntity.getCentreDomainTreeMangerAndEnhancer();
            final Map<String, Object> customObject = createCriteriaMetaValuesCustomObject(
                createCriteriaMetaValues(updatedFreshCentre, getEntityType(miType)),
                appliedCriteriaEntity.centreDirtyCalculator().apply(saveAsName).apply(() -> updatedFreshCentre),
                createStaleCriteriaMessage((String) modifiedPropertiesHolder.get("@@wasRun"), updatedFreshCentre, miType, saveAsName, user, companionFinder, critGenerator, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion)
            );
            customObject.put(VALIDATION_COUNTER, modifiedPropertiesHolder.get(VALIDATION_COUNTER));
            return restUtil.rawListJsonRepresentation(appliedCriteriaEntity, customObject);
        }, restUtil);
    }

    public static Representation createCriteriaRetrievalEnvelope(
            final ICentreDomainTreeManagerAndEnhancer updatedFreshCentre,
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final Optional<String> saveAsName,
            final User user,
            final RestServerUtil restUtil,
            final ICompanionObjectFinder companionFinder,
            final ICriteriaGenerator critGenerator,
            final DeviceProfile device,
            final String saveAsDesc,
            final Optional<String> configUuid,
            final IDomainTreeEnhancerCache domainTreeEnhancerCache,
            final IWebUiConfig webUiConfig,
            final IEntityCentreConfig eccCompanion,
            final IMainMenuItem mmiCompanion,
            final IUser userCompanion,
            final ICentreConfigSharingModel sharingModel) {
        final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> appliedCriteriaEntity = createCriteriaValidationPrototype(miType, saveAsName, updatedFreshCentre, companionFinder, critGenerator, -1L, user, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, sharingModel);
        return restUtil.rawListJsonRepresentation(
            appliedCriteriaEntity,
            removeWasRunIndication(createCriteriaMetaValuesCustomObjectWithSaveAsInfo(
                createCriteriaMetaValues(updatedFreshCentre, getEntityType(miType)),
                appliedCriteriaEntity.centreDirtyCalculator().apply(saveAsName).apply(() -> updatedFreshCentre),
                of(saveAsName),
                of(configUuid),
                of(ofNullable(saveAsDesc)),
                empty(),
                of(updatedFreshCentre.getPreferredView())
            ))
        );
    }

    public static Representation createCriteriaDiscardEnvelope(
            final ICentreDomainTreeManagerAndEnhancer updatedFreshCentre,
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final Optional<String> saveAsName,
            final User user,
            final RestServerUtil restUtil,
            final ICompanionObjectFinder companionFinder,
            final ICriteriaGenerator critGenerator,
            final String staleCriteriaMessage,
            final DeviceProfile device,
            final Optional<Optional<String>> saveAsDesc,
            final IDomainTreeEnhancerCache domainTreeEnhancerCache,
            final IWebUiConfig webUiConfig,
            final IEntityCentreConfig eccCompanion,
            final IMainMenuItem mmiCompanion,
            final IUser userCompanion,
            final ICentreConfigSharingModel sharingModel) {
        return restUtil.rawListJsonRepresentation(
                createCriteriaValidationPrototype(miType, saveAsName, updatedFreshCentre, companionFinder, critGenerator, -1L, user, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, sharingModel),
                createCriteriaMetaValuesCustomObjectWithSaveAsInfo(
                        createCriteriaMetaValues(updatedFreshCentre, getEntityType(miType)),
                        isDefaultOrLink(saveAsName) || isInherited(saveAsName, () -> loadableConfigurations(user, miType, device, companionFinder, sharingModel).apply(of(saveAsName)).stream()), // if not [default, link, inherited] then it is own save-as; after discarding it is always not changed -- checking of isFreshCentreChanged is not needed
                        of(saveAsName),
                        empty(),
                        saveAsDesc,
                        of(ofNullable(staleCriteriaMessage)),
                        of(updatedFreshCentre.getPreferredView())
                )//
        );
    }

    public static <T extends AbstractEntity<?>, M extends EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>>> String createStaleCriteriaMessage(
            final String wasRun,
            final ICentreDomainTreeManagerAndEnhancer freshCentre,
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final Optional<String> saveAsName,
            final User user,
            final ICompanionObjectFinder companionFinder,
            final ICriteriaGenerator critGenerator,
            final DeviceProfile device,
            final IDomainTreeEnhancerCache domainTreeEnhancerCache,
            final IWebUiConfig webUiConfig,
            final IEntityCentreConfig eccCompanion,
            final IMainMenuItem mmiCompanion,
            final IUser userCompanion) {
        if (wasRun != null) {
            // When changing centre we can change selection criteria and mnemonics, but also columns sorting, order, visibility and width / grow factors.
            // From end-user perspective it is only relevant to 'know' whether selection criteria change was not applied against currently visible result-set.
            // Thus need to only compare 'firstTick's of centre managers.
            // Please be careful when adding some new contracts to 'firstTick' not to violate this premise.
            final boolean isCriteriaStale = !equalsEx(updateCentre(user, miType, PREVIOUSLY_RUN_CENTRE_NAME, saveAsName, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder).getFirstTick(), freshCentre.getFirstTick());
            if (isCriteriaStale) {
                return ERR_STALE_CRITERIA;
            }
        }
        return null;
    }

    /**
     * Handles PUT request resulting from tg-selection-criteria <code>run()</code> method.
     */
    @SuppressWarnings("unchecked")
    @Put
    @Override
    public Representation put(final Representation envelope) {
        return handleUndesiredExceptions(getResponse(), () -> {
            logger.debug("CRITERIA_RESOURCE: run started.");
            final Optional<String> saveAsName = extractSaveAsName(getRequest());
            user = userProvider.getUser();
            eccCompanion = companionFinder.find(EntityCentreConfig.class);
            mmiCompanion = companionFinder.find(MainMenuItem.class);
            userCompanion = companionFinder.find(User.class);
            miType = centre.getMenuItemType();

            final CentreContextHolder centreContextHolder = restoreCentreContextHolder(envelope, restUtil);
            final Map<String, Object> customObject = new LinkedHashMap<>(centreContextHolder.getCustomObject());

            final boolean isRunning = isRunning(customObject);
            final boolean isSorting = isSorting(customObject);

            final ICentreDomainTreeManagerAndEnhancer updatedFreshCentre;
            final EnhancedCentreEntityQueryCriteria<?, ?> freshCentreAppliedCriteriaEntity;

            if (isRunning) {
                if (isAutoRunning(customObject) && !saveAsName.isPresent()) {
                    // clear current 'default' surrogate centres -- this is to make them empty before auto-running; saved configurations will not be touched -- they should not appear when first time loading occur; this is because in autoRun centres named configurations never become preferred
                    final ICentreDomainTreeManagerAndEnhancer previousFreshCentre = updateCentre(user, miType, FRESH_CENTRE_NAME, saveAsName, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);
                    final Class<?> root = centre.getEntityType();

                    // store current column layout into local variables to be applied after centre clearing
                    final List<Pair<String, Ordering>> previousSortingProps = previousFreshCentre.getSecondTick().orderedProperties(root);
                    final List<String> previousUsedProps = previousFreshCentre.getSecondTick().usedProperties(root);
                    final T2<EnhancementPropertiesMap<Integer>, EnhancementPropertiesMap<Integer>> previousWidthsAndGrowFactors = previousFreshCentre.getSecondTick().getWidthsAndGrowFactors();
                    final int previousPageCapacity = previousFreshCentre.getSecondTick().getPageCapacity();
                    final int previousVisibleRowsCount = previousFreshCentre.getSecondTick().getVisibleRowsCount();
                    final int previousNumberOfHeaderLines = previousFreshCentre.getSecondTick().getNumberOfHeaderLines();

                    // clear all surrogate centres
                    removeCentres(user, miType, device(), saveAsName, eccCompanion, FRESH_CENTRE_NAME, SAVED_CENTRE_NAME, PREVIOUSLY_RUN_CENTRE_NAME);
                    final ICentreDomainTreeManagerAndEnhancer emptyFreshCentre = updateCentre(user, miType, FRESH_CENTRE_NAME, saveAsName, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);

                    // restore previous non-distracting centre changes; at first apply widths and grow factors
                    emptyFreshCentre.getSecondTick().setWidthsAndGrowFactors(previousWidthsAndGrowFactors);
                    // then apply order, visibility and sorting
                    applyNewOrderVisibilityAndSorting(emptyFreshCentre.getSecondTick(), root, previousUsedProps, previousSortingProps);
                    // also pageCapacity, visibleRowsCount and numberOfHeaderLines
                    emptyFreshCentre.getSecondTick().setPageCapacity(previousPageCapacity);
                    emptyFreshCentre.getSecondTick().setVisibleRowsCount(previousVisibleRowsCount);
                    emptyFreshCentre.getSecondTick().setNumberOfHeaderLines(previousNumberOfHeaderLines);
                    // save the centre into the database (configUuid is not applicable here -- this is default configuration)
                    updatedFreshCentre = commitCentre(user, miType, FRESH_CENTRE_NAME, saveAsName, device(), emptyFreshCentre, null /* newDesc */, webUiConfig, eccCompanion, mmiCompanion, userCompanion);

                    freshCentreAppliedCriteriaEntity = createCriteriaValidationPrototype(miType, saveAsName, updatedFreshCentre, companionFinder, critGenerator, -1L, user, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, sharingModel);
                } else {
                    freshCentreAppliedCriteriaEntity = createCriteriaEntityWithoutConflicts(centreContextHolder.getModifHolder(), companionFinder, critGenerator, miType, saveAsName, user, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, sharingModel);
                    updatedFreshCentre = freshCentreAppliedCriteriaEntity.getCentreDomainTreeMangerAndEnhancer();
                }

                // There is a need to validate criteria entity with the check for 'required' properties. If it is not successful -- immediately return result without query running, fresh centre persistence, data generation etc.
                final Result validationResult = freshCentreAppliedCriteriaEntity.isValid();
                if (!validationResult.isSuccessful()) {
                    logger.debug("CRITERIA_RESOURCE: run finished (validation failed).");
                    final String staleCriteriaMessage = createStaleCriteriaMessage((String) centreContextHolder.getModifHolder().get("@@wasRun"), updatedFreshCentre, miType, saveAsName, user, companionFinder, critGenerator, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion);
                    return restUtil.rawListJsonRepresentation(freshCentreAppliedCriteriaEntity, updateResultantCustomObject(freshCentreAppliedCriteriaEntity.centreDirtyCalculator(), miType, saveAsName, user, updatedFreshCentre, new LinkedHashMap<>(), staleCriteriaMessage, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder));
                }
            } else {
                updatedFreshCentre = null;
                freshCentreAppliedCriteriaEntity = null;
            }

            // if the run() invocation warrants data generation (e.g. it has nothing to do with sorting)
            // then for an entity centre configuration check if a generator was provided
            final boolean createdByConstraintShouldOccur = centre.getGeneratorTypes().isPresent();
            final boolean generationShouldOccur = isRunning && !isSorting && createdByConstraintShouldOccur;
            if (generationShouldOccur) {
                // obtain the type for entities to be generated
                final Class<? extends AbstractEntity<?>> generatorEntityType = (Class<? extends AbstractEntity<?>>) centre.getGeneratorTypes().get().getKey();

                // create and execute a generator instance
                final IGenerator generator = centre.createGeneratorInstance(centre.getGeneratorTypes().get().getValue());
                final Map<String, Optional<?>> params = freshCentreAppliedCriteriaEntity.nonProxiedProperties().collect(toLinkedHashMap(
                        (final MetaProperty<?> mp) -> mp.getName(),
                        (final MetaProperty<?> mp) -> ofNullable(mp.getValue())));
                params.putAll(freshCentreAppliedCriteriaEntity.getParameters().entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey(), entry -> Optional.ofNullable(entry.getValue()))));
                if (shouldForceRegeneration(customObject)) {
                    params.put(FORCE_REGENERATION_KEY, of(true));
                }
                final Result generationResult = generator.gen(generatorEntityType, params);
                // if the data generation was unsuccessful based on the returned Result value then stop any further logic and return the obtained result
                // otherwise, proceed with the request handling further to actually query the data
                // in most cases, the generated and queried data would be represented by the same entity and, thus, the final query needs to be enhanced with user related filtering by property 'createdBy'
                if (!generationResult.isSuccessful()) {
                    logger.debug("CRITERIA_RESOURCE: run finished (generation failed).");
                    final String staleCriteriaMessage = createStaleCriteriaMessage((String) centreContextHolder.getModifHolder().get("@@wasRun"), updatedFreshCentre, miType, saveAsName, user, companionFinder, critGenerator, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion);
                    final Result result = generationResult.copyWith(new ArrayList<>(Arrays.asList(freshCentreAppliedCriteriaEntity, updateResultantCustomObject(freshCentreAppliedCriteriaEntity.centreDirtyCalculator(), miType, saveAsName, user, updatedFreshCentre, new LinkedHashMap<>(), staleCriteriaMessage, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder))));
                    return restUtil.resultJSONRepresentation(result);
                }
            }

            if (isRunning) {
                commitCentreWithoutConflicts(user, miType, PREVIOUSLY_RUN_CENTRE_NAME, saveAsName, device(), updatedFreshCentre, null, webUiConfig, eccCompanion, mmiCompanion, userCompanion);
            }

            final ICentreDomainTreeManagerAndEnhancer previouslyRunCentre = updateCentre(user, miType, PREVIOUSLY_RUN_CENTRE_NAME, saveAsName, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);
            final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ?> previouslyRunCriteriaEntity = createCriteriaValidationPrototype(miType, saveAsName, previouslyRunCentre, companionFinder, critGenerator, 0L, user, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, sharingModel);

            final Optional<Pair<IQueryEnhancer<AbstractEntity<?>>, Optional<CentreContext<AbstractEntity<?>, ?>>>> queryEnhancerAndContext = createQueryEnhancerAndContext(
                    webUiConfig,
                    companionFinder,
                    user,
                    critGenerator,
                    entityFactory,
                    centreContextHolder,
                    centre.getQueryEnhancerConfig(),
                    previouslyRunCriteriaEntity,
                    device(),
                    domainTreeEnhancerCache,
                    eccCompanion,
                    mmiCompanion,
                    userCompanion,
                    sharingModel);

            final Pair<Map<String, Object>, List<?>> pair =
                    createCriteriaMetaValuesCustomObjectWithResult(
                            customObject,
                            previouslyRunCriteriaEntity,
                            centre.getAdditionalFetchProvider(),
                            centre.getAdditionalFetchProviderForTooltipProperties(),
                            queryEnhancerAndContext,
                            // There could be cases where the generated data and the queried data would have different types.
                            // For example, the queried data could be modelled by a synthesized entity that includes a subquery based on some generated data.
                            // In such cases, it is unpossible to enhance the final query with a user related condition automatically.
                            // This should be the responsibility of the application developer to properly construct a subquery that is based on the generated data.
                            // The query will be enhanced with condition createdBy=currentUser if createdByConstraintShouldOccur and generatorEntityType equal to the type of queried data (otherwise end-developer should do that itself by using queryEnhancer or synthesized model).
                            createdByConstraintShouldOccur && centre.getGeneratorTypes().get().getKey().equals(getEntityType(miType)) ? of(user) : empty());
            if (isRunning) {
                updateResultantCustomObject(previouslyRunCriteriaEntity.centreDirtyCalculator(), miType, saveAsName, user, previouslyRunCentre, pair.getKey(), null, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);
            }

            // Running the rendering customiser for result set of entities.
            pair.getKey().put("renderingHints", createRenderingHints(pair.getValue()));

            // Apply primary and secondary action selectors
            pair.getKey().put("primaryActionIndices", createPrimaryActionIndices(pair.getValue()));
            pair.getKey().put("secondaryActionIndices", createSecondaryActionIndices(pair.getValue()));

            // Build dynamic properties object
            final List<Pair<ResultSetProp<AbstractEntity<?>>, Optional<CentreContext<AbstractEntity<?>, ?>>>> resPropsWithContext = getDynamicResultProperties(
                    centre,
                    webUiConfig,
                    companionFinder,
                    user,
                    critGenerator,
                    entityFactory,
                    centreContextHolder,
                    previouslyRunCriteriaEntity,
                    device(),
                    domainTreeEnhancerCache,
                    eccCompanion,
                    mmiCompanion,
                    userCompanion,
                    sharingModel);

            pair.getKey().put("dynamicColumns", createDynamicProperties(resPropsWithContext));

            Stream<AbstractEntity<?>> processedEntities = enhanceResultEntitiesWithCustomPropertyValues(
                    centre,
                    centre.getCustomPropertiesDefinitions(),
                    centre.getCustomPropertiesAsignmentHandler(),
                    ((List<AbstractEntity<?>>) pair.getValue()).stream());

            //Enhance entities with values defined with consumer in each dynamic property.
            processedEntities = enhanceResultEntitiesWithDynamicPropertyValues(processedEntities, resPropsWithContext);

            final ArrayList<Object> list = new ArrayList<>();
            list.add(isRunning ? previouslyRunCriteriaEntity : null);
            list.add(pair.getKey());

            // TODO It looks like adding values directly to the list outside the map object leads to proper type/serialiser correspondence
            // FIXME Need to investigate why this is the case.
            processedEntities.forEach(entity -> list.add(entity));

            // NOTE: the following line can be the example how 'criteria running' server errors manifest to the client application
            // throw new IllegalStateException("Illegal state during criteria running.");
            logger.debug("CRITERIA_RESOURCE: run finished.");
            return restUtil.rawListJsonRepresentation(list.toArray());
        }, restUtil);
    }

    /**
     * Calculates indices of active secondary actions for {@code entities}.
     * 
     * @param entities
     * @return
     */
    private List<List<Integer>> createSecondaryActionIndices(final List<?> entities) {
        final List<? extends IEntityMultiActionSelector> selectors = centre.createSecondaryActionSelectors(); // create all selectors before entities streaming (and reuse them for every entity)
        return entities.stream()
            .map(entity -> selectors.stream()
                .map(selector -> selector.getActionFor((AbstractEntity<?>) entity))
                .collect(toList())
            )
            .collect(toList());
    }

    /**
     * Calculates indices of active primary action for {@code entities}.
     * 
     * @param entities
     * @return
     */
    private List<Integer> createPrimaryActionIndices(final List<?> entities) {
        return centre.createPrimaryActionSelector().map(selector -> {
            return entities.stream().map(entity -> selector.getActionFor((AbstractEntity<?>) entity)).collect(toList());
        }).orElse(emptyList());
    }

    /**
     * Calculates rendering hints for {@code entities}.
     * 
     * @param entities
     * @return
     */
    private List<Object> createRenderingHints(final List<?> entities) {
        final Optional<IRenderingCustomiser<?>> renderingCustomiser = centre.getRenderingCustomiser();
        if (renderingCustomiser.isPresent()) {
            final IRenderingCustomiser<?> renderer = renderingCustomiser.get();
            final List<Object> renderingHints = new ArrayList<>();
            for (final Object entity : entities) {
                renderingHints.add(renderer.getCustomRenderingFor((AbstractEntity<?>)entity).get());
            }
            return renderingHints;
        } else {
            return new ArrayList<>();
        }
    }

    public static Stream<AbstractEntity<?>> enhanceResultEntitiesWithDynamicPropertyValues(final Stream<AbstractEntity<?>> stream, final List<Pair<ResultSetProp<AbstractEntity<?>>, Optional<CentreContext<AbstractEntity<?>, ?>>>> resPropsWithContext) {
        return stream.map(entity -> {
            resPropsWithContext.forEach(resPropWithContext -> {
                final Collection<? extends AbstractEntity<?>> collection = ((AbstractEntity<?>) entity).get(resPropWithContext.getKey().propName.get());
                collection.forEach(e -> resPropWithContext.getKey().entityPreProcessor.get().accept(e, resPropWithContext.getValue()));
            });
            return entity;
        });
    }

    public static List<Pair<ResultSetProp<AbstractEntity<?>>, Optional<CentreContext<AbstractEntity<?>, ?>>>> getDynamicResultProperties(
            final EntityCentre<AbstractEntity<?>> centre,
            final IWebUiConfig webUiConfig,
            final ICompanionObjectFinder companionFinder,
            final User user,
            final ICriteriaGenerator critGenerator,
            final EntityFactory entityFactory,
            final CentreContextHolder centreContextHolder,
            final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ?> criteriaEntity,
            final DeviceProfile device,
            final IDomainTreeEnhancerCache domainTreeEnhancerCache,
            final IEntityCentreConfig eccCompanion,
            final IMainMenuItem mmiCompanion,
            final IUser userCompanion,
            final ICentreConfigSharingModel sharingModel) {
        final List<Pair<ResultSetProp<AbstractEntity<?>>, Optional<CentreContext<AbstractEntity<?>, ?>>>> resList = new ArrayList<>();
        centre.getDynamicProperties().forEach(resProp -> {
            resProp.dynamicColBuilderType.ifPresent(propDefinerClass -> {
                final Optional<CentreContext<AbstractEntity<?>, ?>> optionalCentreContext = CentreResourceUtils.createCentreContext(
                        true, // full context, fully-fledged restoration. This means that IQueryEnhancer descendants (centre query enhancers) could use IContextDecomposer for context decomposition on deep levels.
                        webUiConfig,
                        companionFinder,
                        user,
                        critGenerator,
                        entityFactory,
                        centreContextHolder,
                        criteriaEntity,
                        resProp.contextConfig,
                        null, /* chosenProperty is not applicable in queryEnhancer context */
                        device,
                        domainTreeEnhancerCache,
                        eccCompanion,
                        mmiCompanion,
                        userCompanion,
                        sharingModel
                    );
                resList.add(new Pair<>(resProp, optionalCentreContext));
            });
        });
        return resList;
    }

    private Map<String, List<Map<String, Object>>> createDynamicProperties(final List<Pair<ResultSetProp<AbstractEntity<?>>, Optional<CentreContext<AbstractEntity<?>, ?>>>> resPropsWithContext) {
        final Map<String, List<Map<String, Object>>> dynamicColumns = new LinkedHashMap<>();
        resPropsWithContext.forEach(resPropWithContext -> {
            centre.getDynamicColumnBuilderFor(resPropWithContext.getKey()).ifPresent(dynColumnBuilder ->
                dynColumnBuilder.getColumnsConfig(resPropWithContext.getValue()).ifPresent(config -> dynamicColumns.put(resPropWithContext.getKey().propName.get() + "Columns", config.build()))
            );
        });
        return dynamicColumns;
    }

    /**
     * Resultant custom object contains important result information such as 'centreDirty' (guards enablement of SAVE button) or 'metaValues'
     * (they bind to metaValues criteria editors) or information whether selection criteria is stale (config button colour).
     * <p>
     * This method updates such information just before returning resultant custom object to the client.
     *
     * @param miType
     * @param saveAsName
     * @param updatedFreshCentre
     * @param resultantCustomObject
     * @param staleCriteriaMessage
     *
     * @return
     */
    private static Map<String, Object> updateResultantCustomObject(
            final Function<Optional<String>, Function<Supplier<ICentreDomainTreeManagerAndEnhancer>, Boolean>> centreDirtyCalculator,
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final Optional<String> saveAsName,
            final User user,
            final ICentreDomainTreeManagerAndEnhancer updatedFreshCentre,
            final Map<String, Object> resultantCustomObject,
            final String staleCriteriaMessage,
            final DeviceProfile device,
            final IDomainTreeEnhancerCache domainTreeEnhancerCache,
            final IWebUiConfig webUiConfig,
            final IEntityCentreConfig eccCompanion,
            final IMainMenuItem mmiCompanion,
            final IUser userCompanion,
            final ICompanionObjectFinder companionFinder) {
        resultantCustomObject.put(CENTRE_DIRTY, centreDirtyCalculator.apply(saveAsName).apply(() -> updatedFreshCentre));
        resultantCustomObject.put(META_VALUES, createCriteriaMetaValues(updatedFreshCentre, getEntityType(miType)));

        // Resultant custom object contains information whether selection criteria is stale (config button colour).
        // Such information should be updated just before returning resultant custom object to the client.
        resultantCustomObject.put(STALE_CRITERIA_MESSAGE, staleCriteriaMessage);

        return resultantCustomObject;
    }

    public static Optional<Pair<IQueryEnhancer<AbstractEntity<?>>, Optional<CentreContext<AbstractEntity<?>, ?>>>> createQueryEnhancerAndContext(
            final IWebUiConfig webUiConfig,
            final ICompanionObjectFinder companionFinder,
            final User user,
            final ICriteriaGenerator critGenerator,
            final EntityFactory entityFactory,
            final CentreContextHolder centreContextHolder,
            final Optional<Pair<IQueryEnhancer<AbstractEntity<?>>, Optional<CentreContextConfig>>> queryEnhancerConfig,
            final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ?> criteriaEntity,
            final DeviceProfile device,
            final IDomainTreeEnhancerCache domainTreeEnhancerCache,
            final IEntityCentreConfig eccCompanion,
            final IMainMenuItem mmiCompanion,
            final IUser userCompanion,
            final ICentreConfigSharingModel sharingModel) {
        if (queryEnhancerConfig.isPresent()) {
            return Optional.of(new Pair<>(
                queryEnhancerConfig.get().getKey(),
                CentreResourceUtils.createCentreContext(
                    true, // full context, fully-fledged restoration. This means that IQueryEnhancer descendants (centre query enhancers) could use IContextDecomposer for context decomposition on deep levels.
                    webUiConfig,
                    companionFinder,
                    user,
                    critGenerator,
                    entityFactory,
                    centreContextHolder,
                    criteriaEntity,
                    queryEnhancerConfig.get().getValue(),
                    null, /* chosenProperty is not applicable in queryEnhancer context */
                    device,
                    domainTreeEnhancerCache,
                    eccCompanion,
                    mmiCompanion,
                    userCompanion,
                    sharingModel
                )
            ));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Assigns the values for custom properties.
     *
     * @param propertiesDefinitions
     * @param customPropertiesAsignmentHandler
     * @param entities
     */
    public static Stream<AbstractEntity<?>> enhanceResultEntitiesWithCustomPropertyValues(
            final EntityCentre<AbstractEntity<?>> centre,
            final Optional<List<ResultSetProp<AbstractEntity<?>>>> propertiesDefinitions,
            final Optional<Class<? extends ICustomPropsAssignmentHandler>> customPropertiesAsignmentHandler,
            final Stream<AbstractEntity<?>> entities) {

        final Optional<Stream<AbstractEntity<?>>> assignedEntitiesOp = customPropertiesAsignmentHandler
                .map(handlerType -> centre.createAssignmentHandlerInstance(handlerType))
                .map(handler -> entities.map(entity -> {handler.assignValues(entity); return entity;}));

        final Stream<AbstractEntity<?>> assignedEntities = assignedEntitiesOp.orElse(entities);

        final Optional<Stream<AbstractEntity<?>>> completedEntitiesOp = propertiesDefinitions.map(customProps -> assignedEntities.map(entity -> {
            for (final ResultSetProp<AbstractEntity<?>> customProp : customProps) {
                if (customProp.propDef.isPresent()) {
                    final PropDef<?> propDef = customProp.propDef.get();
                    final String propertyName = CalculatedProperty.generateNameFrom(propDef.title);
                    if (propDef.value.isPresent()) {
                        entity.set(propertyName, propDef.value.get());
                    }
                }
            }
            return entity;
        }));

        return completedEntitiesOp.orElse(assignedEntities);
    }

}