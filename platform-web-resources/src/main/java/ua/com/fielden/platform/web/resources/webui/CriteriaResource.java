package ua.com.fielden.platform.web.resources.webui;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static ua.com.fielden.platform.data.generator.IGenerator.FORCE_REGENERATION_KEY;
import static ua.com.fielden.platform.data.generator.IGenerator.shouldForceRegeneration;
import static ua.com.fielden.platform.streaming.ValueCollectors.toLinkedHashMap;
import static ua.com.fielden.platform.utils.EntityUtils.equalsEx;
import static ua.com.fielden.platform.web.centre.CentreConfigUpdaterUtils.applyNewOrderVisibilityAndSorting;
import static ua.com.fielden.platform.web.centre.CentreUpdater.FRESH_CENTRE_NAME;
import static ua.com.fielden.platform.web.centre.CentreUpdater.PREVIOUSLY_RUN_CENTRE_NAME;
import static ua.com.fielden.platform.web.centre.CentreUpdater.SAVED_CENTRE_NAME;
import static ua.com.fielden.platform.web.centre.CentreUpdater.commitCentre;
import static ua.com.fielden.platform.web.centre.CentreUpdater.commitCentreWithoutConflicts;
import static ua.com.fielden.platform.web.centre.CentreUpdater.removeCentres;
import static ua.com.fielden.platform.web.centre.CentreUpdater.retrievePreferredConfigName;
import static ua.com.fielden.platform.web.centre.CentreUpdater.updateCentre;
import static ua.com.fielden.platform.web.centre.CentreUpdater.updateCentreDesc;
import static ua.com.fielden.platform.web.centre.CentreUtils.isFreshCentreChanged;
import static ua.com.fielden.platform.web.centre.WebApiUtils.LINK_CONFIG_TITLE;
import static ua.com.fielden.platform.web.centre.WebApiUtils.UNDEFINED_CONFIG_TITLE;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.createCriteriaEntityWithoutConflicts;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.createCriteriaMetaValues;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.createCriteriaMetaValuesCustomObject;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.createCriteriaMetaValuesCustomObjectWithResult;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.createCriteriaMetaValuesCustomObjectWithSaveAsInfo;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.createCriteriaValidationPrototype;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.isAutoRunning;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.isRunning;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.isSorting;
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
import ua.com.fielden.platform.web.centre.IQueryEnhancer;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig.ResultSetProp;
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
    private final static Logger logger = Logger.getLogger(CriteriaResource.class);

    private final static String staleCriteriaMessage = "Selection criteria have been changed, but not applied. "
                                                     + "Previously applied values are in effect. "
                                                     + "Please tap action <b>RUN</b> to apply the updated selection criteria.";

    private final RestServerUtil restUtil;
    private final ICompanionObjectFinder companionFinder;

    private final ICriteriaGenerator critGenerator;
    private final EntityCentre<AbstractEntity<?>> centre;
    private final Optional<String> saveAsName;

    private final IWebUiConfig webUiConfig;
    private final IUserProvider userProvider;
    private final EntityFactory entityFactory;
    private final IDomainTreeEnhancerCache domainTreeEnhancerCache;

    public CriteriaResource(
            final RestServerUtil restUtil,
            final EntityCentre<AbstractEntity<?>> centre,
            final Optional<String> saveAsName,
            final IDomainTreeEnhancerCache domainTreeEnhancerCache,
            final IWebUiConfig webUiConfig,
            final ICompanionObjectFinder companionFinder,
            final IUserProvider userProvider,
            final IDeviceProvider deviceProvider,
            final IDates dates,
            final ICriteriaGenerator critGenerator,
            final EntityFactory entityFactory,
            final Context context,
            final Request request,
            final Response response) {
        super(context, request, response, deviceProvider, dates);

        this.restUtil = restUtil;
        this.companionFinder = companionFinder;

        this.centre = centre;
        this.saveAsName = saveAsName;
        this.critGenerator = critGenerator;

        this.domainTreeEnhancerCache = domainTreeEnhancerCache;
        this.webUiConfig = webUiConfig;
        this.userProvider = userProvider;
        this.entityFactory = entityFactory;
    }

    /**
     * Handles GET requests resulting from tg-selection-criteria <code>retrieve()</code> method (new entity).
     */
    @Get
    @Override
    public Representation get() {
        return handleUndesiredExceptions(getResponse(), () -> {
            final Class<? extends MiWithConfigurationSupport<?>> miType = centre.getMenuItemType();
            final User user = userProvider.getUser();
            final IEntityCentreConfig eccCompanion = companionFinder.find(EntityCentreConfig.class);
            final IMainMenuItem mmiCompanion = companionFinder.find(MainMenuItem.class);
            final IUser userCompanion = companionFinder.find(User.class);

            final Optional<String> actualSaveAsName =
                saveAsName.flatMap(
                    name -> UNDEFINED_CONFIG_TITLE.equals(name) // client-driven first time loading of centre's selection criteria
                    ? (getQuery().isEmpty()
                        ? retrievePreferredConfigName(user, miType, device(), companionFinder) // preferred configuration should be loaded
                        : of(LINK_CONFIG_TITLE)) // 'link' configuration should be loaded
                    : of(name) // in case where first time loading has been occurred earlier then 'saveAsName' has non-empty actual configuration that needs to be loaded
                ); // in case where 'saveAsName' has empty value then first time loading has been occurred earlier and default configuration needs to be loaded
            if (saveAsName.isPresent() && UNDEFINED_CONFIG_TITLE.equals(saveAsName.get()) && !getQuery().isEmpty()) { // if first time loading with centre criteria parameters occurs then
                // clear current 'link' surrogate centres -- this is to make them empty before applying new selection criteria parameters (client-side action after this request's response will be delivered)
                removeCentres(user, miType, device(), actualSaveAsName, eccCompanion, FRESH_CENTRE_NAME, SAVED_CENTRE_NAME, PREVIOUSLY_RUN_CENTRE_NAME);
            }

            final ICentreDomainTreeManagerAndEnhancer updatedFreshCentre = updateCentre(user, userProvider, miType, FRESH_CENTRE_NAME, actualSaveAsName, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);
            final String customDesc = updateCentreDesc(user, miType, actualSaveAsName, device(), eccCompanion);
            return createCriteriaRetrievalEnvelope(updatedFreshCentre, miType, actualSaveAsName, user, userProvider, restUtil, companionFinder, critGenerator, device(), customDesc, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion);
        }, restUtil);
    }

    /**
     * Handles POST request resulting from tg-selection-criteria <code>validate()</code> method.
     */
    @Post
    @Override
    public Representation post(final Representation envelope) {
        return handleUndesiredExceptions(getResponse(), () -> {
            final IEntityCentreConfig eccCompanion = companionFinder.find(EntityCentreConfig.class);
            final IMainMenuItem mmiCompanion = companionFinder.find(MainMenuItem.class);
            final IUser userCompanion = companionFinder.find(User.class);
            final Map<String, Object> modifiedPropertiesHolder = restoreModifiedPropertiesHolderFrom(envelope, restUtil);
            final Class<? extends MiWithConfigurationSupport<?>> miType = centre.getMenuItemType();
            final User user = userProvider.getUser();
            final DeviceProfile device = device();
            final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> appliedCriteriaEntity = createCriteriaEntityWithoutConflicts(modifiedPropertiesHolder, companionFinder, critGenerator, miType, saveAsName, user, userProvider, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion);
            final ICentreDomainTreeManagerAndEnhancer updatedFreshCentre = appliedCriteriaEntity.getCentreDomainTreeMangerAndEnhancer();
            final Map<String, Object> customObject = createCriteriaMetaValuesCustomObject(
                createCriteriaMetaValues(updatedFreshCentre, getEntityType(miType)),
                isFreshCentreChanged(updatedFreshCentre, updateCentre(user, userProvider, miType, SAVED_CENTRE_NAME, saveAsName, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder)),
                createStaleCriteriaMessage((String) modifiedPropertiesHolder.get("@@wasRun"), updatedFreshCentre, miType, saveAsName, user, userProvider, companionFinder, critGenerator, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion)
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
            final IUserProvider userProvider,
            final RestServerUtil restUtil,
            final ICompanionObjectFinder companionFinder,
            final ICriteriaGenerator critGenerator,
            final DeviceProfile device,
            final String saveAsDesc,
            final IDomainTreeEnhancerCache domainTreeEnhancerCache,
            final IWebUiConfig webUiConfig,
            final IEntityCentreConfig eccCompanion,
            final IMainMenuItem mmiCompanion,
            final IUser userCompanion) {
        return restUtil.rawListJsonRepresentation(
                createCriteriaValidationPrototype(miType, saveAsName, updatedFreshCentre, companionFinder, critGenerator, -1L, user, userProvider, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion),
                createCriteriaMetaValuesCustomObjectWithSaveAsInfo(
                        createCriteriaMetaValues(updatedFreshCentre, getEntityType(miType)),
                        isFreshCentreChanged(updatedFreshCentre, updateCentre(user, userProvider, miType, SAVED_CENTRE_NAME, saveAsName, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder)),
                        of(saveAsName),
                        of(ofNullable(saveAsDesc)),
                        empty()
                )//
        );
    }

    public static Representation createCriteriaDiscardEnvelope(
            final ICentreDomainTreeManagerAndEnhancer updatedFreshCentre,
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final Optional<String> saveAsName,
            final User user,
            final IUserProvider userProvider,
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
            final IUser userCompanion) {
        return restUtil.rawListJsonRepresentation(
                createCriteriaValidationPrototype(miType, saveAsName, updatedFreshCentre, companionFinder, critGenerator, -1L, user, userProvider, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion),
                createCriteriaMetaValuesCustomObjectWithSaveAsInfo(
                        createCriteriaMetaValues(updatedFreshCentre, getEntityType(miType)),
                        false,
                        empty(),
                        saveAsDesc,
                        of(ofNullable(staleCriteriaMessage))
                )//
        );
    }

    public static <T extends AbstractEntity<?>, M extends EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>>> String createStaleCriteriaMessage(
            final String wasRun,
            final ICentreDomainTreeManagerAndEnhancer freshCentre,
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final Optional<String> saveAsName,
            final User user,
            final IUserProvider userProvider,
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
            final boolean isCriteriaStale = !equalsEx(updateCentre(user, userProvider, miType, PREVIOUSLY_RUN_CENTRE_NAME, saveAsName, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder).getFirstTick(), freshCentre.getFirstTick());
            if (isCriteriaStale) {
                return staleCriteriaMessage;
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
            final User user = userProvider.getUser();
            final IEntityCentreConfig eccCompanion = companionFinder.find(EntityCentreConfig.class);
            final IMainMenuItem mmiCompanion = companionFinder.find(MainMenuItem.class);
            final IUser userCompanion = companionFinder.find(User.class);

            final Class<? extends MiWithConfigurationSupport<?>> miType = centre.getMenuItemType();
            final CentreContextHolder centreContextHolder = restoreCentreContextHolder(envelope, restUtil);

            final Map<String, Object> customObject = new LinkedHashMap<>(centreContextHolder.getCustomObject());

            final boolean isRunning = isRunning(customObject);
            final boolean isSorting = isSorting(customObject);

            final ICentreDomainTreeManagerAndEnhancer updatedFreshCentre;
            final EnhancedCentreEntityQueryCriteria<?, ?> freshCentreAppliedCriteriaEntity;

            if (isRunning) {
                if (isAutoRunning(customObject) && !saveAsName.isPresent()) {
                    // clear current 'default' surrogate centres -- this is to make them empty before auto-running; saved configurations will not be touched -- they should not appear when first time loading occur; this is because in autoRun centres named configurations never become preferred
                    final ICentreDomainTreeManagerAndEnhancer previousFreshCentre = updateCentre(user, userProvider, miType, FRESH_CENTRE_NAME, saveAsName, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);
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
                    final ICentreDomainTreeManagerAndEnhancer emptyFreshCentre = updateCentre(user, userProvider, miType, FRESH_CENTRE_NAME, saveAsName, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);

                    // restore previous non-distracting centre changes; at first apply widths and grow factors
                    emptyFreshCentre.getSecondTick().setWidthsAndGrowFactors(previousWidthsAndGrowFactors);
                    // then apply order, visibility and sorting
                    applyNewOrderVisibilityAndSorting(emptyFreshCentre.getSecondTick(), root, previousUsedProps, previousSortingProps);
                    // also pageCapacity, visibleRowsCount and numberOfHeaderLines
                    emptyFreshCentre.getSecondTick().setPageCapacity(previousPageCapacity);
                    emptyFreshCentre.getSecondTick().setVisibleRowsCount(previousVisibleRowsCount);
                    emptyFreshCentre.getSecondTick().setNumberOfHeaderLines(previousNumberOfHeaderLines);
                    // save the centre into the database
                    updatedFreshCentre = commitCentre(user, userProvider, miType, FRESH_CENTRE_NAME, saveAsName, device(), emptyFreshCentre, null /* newDesc */, webUiConfig, eccCompanion, mmiCompanion, userCompanion);

                    freshCentreAppliedCriteriaEntity = createCriteriaValidationPrototype(miType, saveAsName, updatedFreshCentre, companionFinder, critGenerator, -1L, user, userProvider, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion);
                } else {
                    freshCentreAppliedCriteriaEntity = createCriteriaEntityWithoutConflicts(centreContextHolder.getModifHolder(), companionFinder, critGenerator, miType, saveAsName, user, userProvider, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion);
                    updatedFreshCentre = freshCentreAppliedCriteriaEntity.getCentreDomainTreeMangerAndEnhancer();
                }

                // There is a need to validate criteria entity with the check for 'required' properties. If it is not successful -- immediately return result without query running, fresh centre persistence, data generation etc.
                final Result validationResult = freshCentreAppliedCriteriaEntity.isValid();
                if (!validationResult.isSuccessful()) {
                    logger.debug("CRITERIA_RESOURCE: run finished (validation failed).");
                    final String staleCriteriaMessage = createStaleCriteriaMessage((String) centreContextHolder.getModifHolder().get("@@wasRun"), updatedFreshCentre, miType, saveAsName, user, userProvider, companionFinder, critGenerator, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion);
                    return restUtil.rawListJsonRepresentation(freshCentreAppliedCriteriaEntity, updateResultantCustomObject(miType, saveAsName, user, userProvider, updatedFreshCentre, new LinkedHashMap<>(), staleCriteriaMessage, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder));
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
                    final String staleCriteriaMessage = createStaleCriteriaMessage((String) centreContextHolder.getModifHolder().get("@@wasRun"), updatedFreshCentre, miType, saveAsName, user, userProvider, companionFinder, critGenerator, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion);
                    final Result result = generationResult.copyWith(new ArrayList<>(Arrays.asList(freshCentreAppliedCriteriaEntity, updateResultantCustomObject(miType, saveAsName, user, userProvider, updatedFreshCentre, new LinkedHashMap<>(), staleCriteriaMessage, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder))));
                    return restUtil.resultJSONRepresentation(result);
                }
            }

            if (isRunning) {
                commitCentreWithoutConflicts(user, userProvider, miType, PREVIOUSLY_RUN_CENTRE_NAME, saveAsName, device(), updatedFreshCentre, null, webUiConfig, eccCompanion, mmiCompanion, userCompanion);
            }

            final ICentreDomainTreeManagerAndEnhancer previouslyRunCentre = updateCentre(user, userProvider, miType, PREVIOUSLY_RUN_CENTRE_NAME, saveAsName, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);
            final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ?> previouslyRunCriteriaEntity = createCriteriaValidationPrototype(miType, saveAsName, previouslyRunCentre, companionFinder, critGenerator, 0L, user, userProvider, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion);

            final Optional<Pair<IQueryEnhancer<AbstractEntity<?>>, Optional<CentreContext<AbstractEntity<?>, ?>>>> queryEnhancerAndContext = createQueryEnhancerAndContext(
                    webUiConfig,
                    companionFinder,
                    user,
                    userProvider,
                    critGenerator,
                    entityFactory,
                    centreContextHolder,
                    centre.getQueryEnhancerConfig(),
                    previouslyRunCriteriaEntity,
                    device(),
                    domainTreeEnhancerCache,
                    eccCompanion,
                    mmiCompanion,
                    userCompanion);

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
                            createdByConstraintShouldOccur && centre.getGeneratorTypes().get().getKey().equals(getEntityType(miType)) ? of(userProvider.getUser()) : empty());
            if (isRunning) {
                updateResultantCustomObject(miType, saveAsName, user, userProvider, previouslyRunCentre, pair.getKey(), null, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);
            }

            // Running the rendering customiser for result set of entities.
            final Optional<IRenderingCustomiser<?>> renderingCustomiser = centre.getRenderingCustomiser();
            if (renderingCustomiser.isPresent()) {
                final IRenderingCustomiser<?> renderer = renderingCustomiser.get();
                final List<Object> renderingHints = new ArrayList<>();
                for (final Object entity : pair.getValue()) {
                    renderingHints.add(renderer.getCustomRenderingFor((AbstractEntity<?>)entity).get());
                }
                pair.getKey().put("renderingHints", renderingHints);
            } else {
                pair.getKey().put("renderingHints", new ArrayList<>());
            }

            //Build dynamic properties object
            final List<Pair<ResultSetProp<AbstractEntity<?>>, Optional<CentreContext<AbstractEntity<?>, ?>>>> resPropsWithContext = getDynamicResultProperties(
                    centre,
                    webUiConfig,
                    companionFinder,
                    user,
                    userProvider,
                    critGenerator,
                    entityFactory,
                    centreContextHolder,
                    previouslyRunCriteriaEntity,
                    device(),
                    domainTreeEnhancerCache,
                    eccCompanion,
                    mmiCompanion,
                    userCompanion);

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
            final IUserProvider userProvider,
            final ICriteriaGenerator critGenerator,
            final EntityFactory entityFactory,
            final CentreContextHolder centreContextHolder,
            final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ?> criteriaEntity,
            final DeviceProfile device,
            final IDomainTreeEnhancerCache domainTreeEnhancerCache,
            final IEntityCentreConfig eccCompanion,
            final IMainMenuItem mmiCompanion,
            final IUser userCompanion) {
        final List<Pair<ResultSetProp<AbstractEntity<?>>, Optional<CentreContext<AbstractEntity<?>, ?>>>> resList = new ArrayList<>();
        centre.getDynamicProperties().forEach(resProp -> {
            resProp.dynamicColBuilderType.ifPresent(propDefinerClass -> {
                final Optional<CentreContext<AbstractEntity<?>, ?>> optionalCentreContext = CentreResourceUtils.createCentreContext(
                        true, // full context, fully-fledged restoration. This means that IQueryEnhancer descendants (centre query enhancers) could use IContextDecomposer for context decomposition on deep levels.
                        webUiConfig,
                        companionFinder,
                        user,
                        userProvider,
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
                        userCompanion
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
     * Resultant custom object contains important result information such as 'isCentreChanged' (guards enablement of SAVE / DISCARD buttons) or 'metaValues'
     * (they bind to metaValues criteria editors) or information whether selection criteria is stale (config button colour).
     * <p>
     * This method updates such information just before returning resultant custom object to the client.
     *
     * @param miType
     * @param saveAsName
     * @param gdtm
     * @param updatedFreshCentre
     * @param resultantCustomObject
     * @param staleCriteriaMessage
     *
     * @return
     */
    private static Map<String, Object> updateResultantCustomObject(
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final Optional<String> saveAsName,
            final User user, final IUserProvider userProvider,
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
        resultantCustomObject.put("isCentreChanged", isFreshCentreChanged(updatedFreshCentre, updateCentre(user, userProvider, miType, SAVED_CENTRE_NAME, saveAsName, device, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder)));
        resultantCustomObject.put("metaValues", createCriteriaMetaValues(updatedFreshCentre, getEntityType(miType)));

        // Resultant custom object contains information whether selection criteria is stale (config button colour).
        // Such information should be updated just before returning resultant custom object to the client.
        resultantCustomObject.put("staleCriteriaMessage", staleCriteriaMessage);

        return resultantCustomObject;
    }

    public static Optional<Pair<IQueryEnhancer<AbstractEntity<?>>, Optional<CentreContext<AbstractEntity<?>, ?>>>> createQueryEnhancerAndContext(
            final IWebUiConfig webUiConfig,
            final ICompanionObjectFinder companionFinder,
            final User user,
            final IUserProvider userProvider,
            final ICriteriaGenerator critGenerator,
            final EntityFactory entityFactory,
            final CentreContextHolder centreContextHolder,
            final Optional<Pair<IQueryEnhancer<AbstractEntity<?>>, Optional<CentreContextConfig>>> queryEnhancerConfig,
            final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ?> criteriaEntity,
            final DeviceProfile device,
            final IDomainTreeEnhancerCache domainTreeEnhancerCache,
            final IEntityCentreConfig eccCompanion,
            final IMainMenuItem mmiCompanion,
            final IUser userCompanion) {
        if (queryEnhancerConfig.isPresent()) {
            return Optional.of(new Pair<>(
                queryEnhancerConfig.get().getKey(),
                CentreResourceUtils.createCentreContext(
                    true, // full context, fully-fledged restoration. This means that IQueryEnhancer descendants (centre query enhancers) could use IContextDecomposer for context decomposition on deep levels.
                    webUiConfig,
                    companionFinder,
                    user,
                    userProvider,
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
                    userCompanion
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