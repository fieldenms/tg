package ua.com.fielden.platform.web.utils;

import com.google.inject.Inject;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.types.either.Either;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.EntityCentreConfigCo;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.MainMenuItemCo;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.ICentreConfigSharingModel;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.*;
import static ua.com.fielden.platform.criteria.generator.impl.SynchroniseCriteriaWithModelHandler.CRITERIA_ENTITY_ID;
import static ua.com.fielden.platform.data.generator.IGenerator.FORCE_REGENERATION_KEY;
import static ua.com.fielden.platform.data.generator.IGenerator.shouldForceRegeneration;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.streaming.ValueCollectors.toLinkedHashMap;
import static ua.com.fielden.platform.types.either.Either.left;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.mapOf;
import static ua.com.fielden.platform.utils.EntityUtils.fetchWithKeyAndDesc;
import static ua.com.fielden.platform.web.centre.CentreUpdater.PREFIX_OF;
import static ua.com.fielden.platform.web.centre.CentreUpdaterUtils.findConfigOptByUuid;
import static ua.com.fielden.platform.web.centre.WebApiUtils.LINK_CONFIG_TITLE;
import static ua.com.fielden.platform.web.factories.webui.ResourceFactoryUtils.getEntityCentre;
import static ua.com.fielden.platform.web.interfaces.DeviceProfile.DESKTOP;
import static ua.com.fielden.platform.web.interfaces.DeviceProfile.MOBILE;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.*;
import static ua.com.fielden.platform.web.resources.webui.CriteriaResource.*;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.getOriginalManagedType;

public class EntityCentreAPIImpl implements EntityCentreAPI {
    private final ICompanionObjectFinder companionFinder;
    private final IUserProvider userProvider;
//    private final IDeviceProvider deviceProvider;
    private final ICriteriaGenerator critGenerator;
    private final IWebUiConfig webUiConfig;
    private final EntityFactory entityFactory;
    private final ICentreConfigSharingModel sharingModel;
    private final IAuthorisationModel authorisationModel;
    private final ISecurityTokenProvider securityTokenProvider;

    @Inject
    public EntityCentreAPIImpl(
        final ICompanionObjectFinder companionFinder,
        final IUserProvider userProvider,
//        final IDeviceProvider deviceProvider,
        final ICriteriaGenerator critGenerator,
        final IWebUiConfig webUiConfig,
        final EntityFactory entityFactory,
        final ICentreConfigSharingModel sharingModel,
        final IAuthorisationModel authorisationModel,
        final ISecurityTokenProvider securityTokenProvider
    ) {
        this.companionFinder = companionFinder;
        this.userProvider = userProvider;
//        this.deviceProvider = deviceProvider;
        this.critGenerator = critGenerator;
        this.webUiConfig = webUiConfig;
        this.entityFactory = entityFactory;
        this.sharingModel = sharingModel;
        this.authorisationModel = authorisationModel;
        this.securityTokenProvider = securityTokenProvider;
    }

    private static ICompoundCondition0<EntityCentreConfig> centreConfigQueryFor(final String surrogateName, final Optional<String> maybeAlias) {
        final var selectStart = select(EntityCentreConfig.class);
        return maybeAlias.map(selectStart::as).orElse(selectStart)
            .where().begin()
                .prop("title").like().val(PREFIX_OF.apply(surrogateName).apply(DESKTOP))
                .or().prop("title").like().val(PREFIX_OF.apply(surrogateName).apply(MOBILE))
            .end();
    }

    private static ICompoundCondition0<EntityCentreConfig> centreConfigQueryFor(final String uuid, final String surrogateName, final Optional<String> maybeAlias) {
        return centreConfigQueryFor(surrogateName, maybeAlias)
            .and().condition(centreConfigCondFor(uuid));
    }

    @Override
    public <T extends AbstractEntity<?>, M extends EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>>> Either<Result, List<T>> entityCentreResult(
        final String configUuid
    ) {
        final IUser coUser = companionFinder.find(User.class, true);
        final EntityCentreConfigCo eccCompanion = companionFinder.find(EntityCentreConfig.class);
        final Optional<EntityCentreConfig> freshConfigOpt = eccCompanion.getEntityOptional(
                from(centreConfigQueryFor(configUuid, FRESH_CENTRE_NAME, of("ecc"))
                        .and().exists(
                                centreConfigQueryFor(configUuid, SAVED_CENTRE_NAME, empty())
                                        .and().prop("owner").eq().extProp("ecc.owner")
                                        .model()
                        ).model())
                        .with(
                                fetchWithKeyAndDesc(EntityCentreConfig.class, true)
                                        .with("owner", "menuItem", "title")
                                        .fetchModel()
                        ).model()
        );

        if (!freshConfigOpt.isPresent()) {
            return left(Result.failure("Config with uuid %s does not exist.".formatted(configUuid)));
        }
        final User user = coUser.findUser(freshConfigOpt.get().getOwner().getKey());
        final Class<?> miTypeGen;
        try {
            miTypeGen = Class.forName(freshConfigOpt.get().getMenuItem().getKey());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        Class<? extends MiWithConfigurationSupport<?>> miType = (Class<? extends MiWithConfigurationSupport<?>>) miTypeGen;
        final var device = freshConfigOpt.get().getTitle().startsWith(MOBILE.name()) ? MOBILE : DESKTOP;

        final User currentUser = userProvider.getUser();

        try {

            userProvider.setUser(user);

            final CentreContextHolder centreContextHolder = null;
            final Map<String, Object> customObject = mapOf(t2("@@action", RunActions.RUN.toString()));

            final boolean isRunning = true;
            final boolean isSorting = false;

            final MainMenuItemCo mmiCompanion = companionFinder.find(MainMenuItem.class);
            final IUser userCompanion = companionFinder.find(User.class);


//            final EnhancedCentreEntityQueryCriteria<?, ?> freshCentreAppliedCriteriaEntity = createCriteriaEntityWithoutConflicts(
//                centreContextHolder.getModifHolder(),
//                companionFinder,
//                critGenerator,
//                miType,
//                saveAsName,
//                user,
//                DeviceProfile.DESKTOP,
//                webUiConfig,
//                eccCompanion,
//                mmiCompanion,
//                userCompanion,
//                sharingModel
//            );


            // start loading of configuration defined by concrete uuid;
            // we look only through [link, own save-as, inherited from base, inherited from shared] set of configurations;
            // default configurations are excluded in the lookup;
            // only FRESH kind are looked for;
            final Optional<String> actualSaveAsName;
            // for current user we already have FRESH configuration with uuid loaded;
            final Optional<String> preliminarySaveAsName = of(obtainTitleFrom(freshConfigOpt.get().getTitle(), FRESH_CENTRE_NAME, device));
//            // updating is required from upstream configuration;
//            if (!LINK_CONFIG_TITLE.equals(preliminarySaveAsName.get())) { // (but not for link configuration);
//                actualSaveAsName = updateFromUpstream(configUuid, preliminarySaveAsName, miType, device, eccCompanion, user, webUiConfig, mmiCompanion, userCompanion, sharingModel, companionFinder)._1;
//            } else {
            actualSaveAsName = preliminarySaveAsName;
//            }
            if (!actualSaveAsName.isPresent() || LINK_CONFIG_TITLE.equals(actualSaveAsName.get())) {
                return left(failure("Default / Link configs are not available for API running (%s).".formatted(actualSaveAsName)));
            }
            final var saveAsName = actualSaveAsName;

            // load / update fresh centre if it is not loaded yet / stale
            final ICentreDomainTreeManagerAndEnhancer originalCdtmae = updateCentre(user, miType, FRESH_CENTRE_NAME, saveAsName, device, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);
            //final ICentreDomainTreeManagerAndEnhancer originalCdtmae = updateCentre(user, miType, SAVED_CENTRE_NAME, saveAsName, device, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);

            final M validationPrototype = createCriteriaValidationPrototype(miType, saveAsName, originalCdtmae, companionFinder, critGenerator, CRITERIA_ENTITY_ID /* TODO prevVersion + 1 */, user, device, webUiConfig, eccCompanion, mmiCompanion, userCompanion, sharingModel);

            final M freshCriteriaEntity = resetMetaStateForCriteriaValidationPrototype(
                validationPrototype,
                getOriginalManagedType(validationPrototype.getType(), originalCdtmae)
            );

            final Result validationResult = validateCriteriaBeforeRunning(freshCriteriaEntity, miType, authorisationModel, securityTokenProvider);
            if (!validationResult.isSuccessful()) {
                return left(validationResult);
            }

            final EntityCentre<AbstractEntity<?>> centre = getEntityCentre(miType.getName(), webUiConfig);
            final Result generationResult = generateDataIfNeeded(freshCriteriaEntity, centre, isRunning, isSorting, customObject);
            // if the data generation was unsuccessful based on the returned Result value then stop any further logic and return the obtained result
            // otherwise, proceed with the request handling further to actually query the data
            // in most cases, the generated and queried data would be represented by the same entity and, thus, the final query needs to be enhanced with user related filtering by property 'createdBy'
            if (!generationResult.isSuccessful()) {
                return left(generationResult);
            }

            //final ICentreDomainTreeManagerAndEnhancer updatedSavedCentre = savedCriteriaEntity.getCentreDomainTreeMangerAndEnhancer();

            //commitCentreWithoutConflicts(user, miType, PREVIOUSLY_RUN_CENTRE_NAME, saveAsName, device, updatedSavedCentre, null, webUiConfig, eccCompanion, mmiCompanion, userCompanion);

            //final ICentreDomainTreeManagerAndEnhancer previouslyRunCentre = updateCentre(user, miType, PREVIOUSLY_RUN_CENTRE_NAME, saveAsName, device, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);
            //final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ?> previouslyRunCriteriaEntity = createCriteriaValidationPrototype(miType, saveAsName, previouslyRunCentre, companionFinder, critGenerator, 0L, user, device, webUiConfig, eccCompanion, mmiCompanion, userCompanion, sharingModel);

            final Pair<Map<String, Object>, List<AbstractEntity<?>>> pair = createCriteriaMetaValuesCustomObjectWithResult(
                customObject,
                complementCriteriaEntityBeforeRunning( // complements previouslyRunCriteriaEntity instance
                        freshCriteriaEntity,
                        webUiConfig,
                        companionFinder,
                        user,
                        critGenerator,
                        entityFactory,
                        centreContextHolder,
                        eccCompanion,
                        mmiCompanion,
                        userCompanion,
                        sharingModel
                )
            );

            Stream<AbstractEntity<?>> processedEntities = enhanceResultEntitiesWithCustomPropertyValues(
                    centre,
                    centre.getCustomPropertiesDefinitions(),
                    centre.getCustomPropertiesAsignmentHandler(),
                    pair.getValue().stream());

            // Build dynamic properties object
            final var resPropsWithContext = getDynamicResultProperties(
                    centre,
                    webUiConfig,
                    companionFinder,
                    user,
                    critGenerator,
                    entityFactory,
                    centreContextHolder,
                    (EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ?>) freshCriteriaEntity,
                    device,
                    eccCompanion,
                    mmiCompanion,
                    userCompanion,
                    sharingModel);

            //Enhance entities with values defined with consumer in each dynamic property.
            processedEntities = enhanceResultEntitiesWithDynamicPropertyValues(processedEntities, resPropsWithContext);
//            //Enhance rendering hints with styles for each dynamic column.
//            processedEntities = enhanceResultEntitiesWithDynamicPropertyRenderingHints(processedEntities, resPropsWithContext, (List) pair.getKey().get("renderingHints"));

            final List<T> list = new ArrayList<>();
            //            list.add(isRunning ? previouslyRunCriteriaEntity : null);
            //            list.add(pair.getKey());

            // TODO It looks like adding values directly to the list outside the map object leads to proper type/serialiser correspondence
            // FIXME Need to investigate why this is the case.
            processedEntities.forEach(entity -> list.add((T) entity) );
            return Either.right(list);

        } finally {
            userProvider.setUser(currentUser);
        }
    }

}
