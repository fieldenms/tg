package ua.com.fielden.platform.web.utils;

import com.google.inject.Inject;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.criteria.generator.impl.SynchroniseCriteriaWithModelHandler;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.either.Either;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.EntityCentreConfigCo;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.MainMenuItemCo;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.utils.CollectionUtil;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.ICentreConfigSharingModel;
import ua.com.fielden.platform.web.interfaces.DeviceProfile;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.apache.poi.hslf.usermodel.HSLFFontInfo.FontRenderType.device;
import static ua.com.fielden.platform.criteria.generator.impl.SynchroniseCriteriaWithModelHandler.CRITERIA_ENTITY_ID;
import static ua.com.fielden.platform.data.generator.IGenerator.FORCE_REGENERATION_KEY;
import static ua.com.fielden.platform.data.generator.IGenerator.shouldForceRegeneration;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.streaming.ValueCollectors.toLinkedHashMap;
import static ua.com.fielden.platform.types.either.Either.left;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.mapOf;
import static ua.com.fielden.platform.web.centre.CentreUpdaterUtils.findConfigOptByUuid;
import static ua.com.fielden.platform.web.centre.WebApiUtils.LINK_CONFIG_TITLE;
import static ua.com.fielden.platform.web.factories.webui.ResourceFactoryUtils.getEntityCentre;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.*;
import static ua.com.fielden.platform.web.resources.webui.CriteriaResource.*;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.getOriginalManagedType;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.maybeVersion;

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

    @Override
    public <T extends AbstractEntity<?>, M extends EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>>> Either<Result, List<T>> entityCentreResult(
        final String miTypeNameForStandaloneCentre,
        final String configUuid
    ) {
        final Class<?> miTypeGen;
        try {
            miTypeGen = Class.forName(miTypeNameForStandaloneCentre);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        Class<? extends MiWithConfigurationSupport<?>> miType = (Class<? extends MiWithConfigurationSupport<?>>) miTypeGen;
        final IUser coUser = companionFinder.find(User.class, true);
        final EntityCentreConfigCo eccCompanion = companionFinder.find(EntityCentreConfig.class);
        // TODO
        final var device = DeviceProfile.DESKTOP;
        final Optional<EntityCentreConfig> freshConfigOpt = findConfigOptByUuid(configUuid, miType, device, FRESH_CENTRE_NAME, eccCompanion);
        if (!freshConfigOpt.isPresent()) {
            return left(Result.failure("Config with uuid %s does not exist.".formatted(configUuid)));
        }
        final User user = coUser.findUser(freshConfigOpt.get().getOwner().getKey());

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

            // There is a need to validate criteria entity with the check for 'required' properties. If it is not successful -- immediately return result without query running, fresh centre persistence, data generation etc.
            final Result validationResult = freshCriteriaEntity.isValid();
            if (!validationResult.isSuccessful()) {
                return left(validationResult);
            }

            final Result authorisationResult = authoriseCriteriaEntity(freshCriteriaEntity, miType, authorisationModel, securityTokenProvider);
            if (!authorisationResult.isSuccessful()) {
                return left(authorisationResult);
            }

            final EntityCentre<AbstractEntity<?>> centre = getEntityCentre(miType.getName(), webUiConfig);

            // if the run() invocation warrants data generation (e.g. it has nothing to do with sorting)
            // then for an entity centre configuration check if a generator was provided
            final boolean createdByConstraintShouldOccur = centre.getGeneratorTypes().isPresent();
            final boolean generationShouldOccur = isRunning && !isSorting && createdByConstraintShouldOccur;
            if (generationShouldOccur) {
                // obtain the type for entities to be generated
                final Class<? extends AbstractEntity<?>> generatorEntityType = (Class<? extends AbstractEntity<?>>) centre.getGeneratorTypes().get().getKey();

                // create and execute a generator instance
                final var generator = centre.createGeneratorInstance(centre.getGeneratorTypes().get().getValue());
                final Map<String, Optional<?>> params = freshCriteriaEntity.nonProxiedProperties().collect(toLinkedHashMap(
                        (final MetaProperty<?> mp) -> mp.getName(),
                        (final MetaProperty<?> mp) -> ofNullable(mp.getValue())));
                params.putAll(freshCriteriaEntity.getParameters().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> Optional.ofNullable(entry.getValue()))));
                if (shouldForceRegeneration(customObject)) { // TODO always force regeneration
                    params.put(FORCE_REGENERATION_KEY, of(true));
                }
                final Result generationResult = generator.gen(generatorEntityType, params);
                // if the data generation was unsuccessful based on the returned Result value then stop any further logic and return the obtained result
                // otherwise, proceed with the request handling further to actually query the data
                // in most cases, the generated and queried data would be represented by the same entity and, thus, the final query needs to be enhanced with user related filtering by property 'createdBy'
                if (!generationResult.isSuccessful()) {
                    return left(generationResult);
                }
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

            final List<T> list = new ArrayList<>();
//            list.add(isRunning ? previouslyRunCriteriaEntity : null);
//            list.add(pair.getKey());

            // TODO It looks like adding values directly to the list outside the map object leads to proper type/serialiser correspondence
            // FIXME Need to investigate why this is the case.
            processedEntities.forEach(entity -> list.add((T) entity) );
            return Either.right(list);

            //            // Build dynamic properties object
//            final List<Pair<ua.com.fielden.platform.web.centre.api.EntityCentreConfig.ResultSetProp<AbstractEntity<?>>, Optional<CentreContext<AbstractEntity<?>, ?>>>> resPropsWithContext = getDynamicResultProperties(
//                    centre,
//                    webUiConfig,
//                    companionFinder,
//                    user,
//                    critGenerator,
//                    entityFactory,
//                    centreContextHolder,
//                    previouslyRunCriteriaEntity,
//                    device,
//                    eccCompanion,
//                    mmiCompanion,
//                    userCompanion,
//                    sharingModel);
//
//            //Enhance entities with values defined with consumer in each dynamic property.
//            processedEntities = enhanceResultEntitiesWithDynamicPropertyValues(processedEntities, resPropsWithContext);
//            //Enhance rendering hints with styles for each dynamic column.
//            processedEntities = enhanceResultEntitiesWithDynamicPropertyRenderingHints(processedEntities, resPropsWithContext, (List) pair.getKey().get("renderingHints"));


        } finally {
            userProvider.setUser(currentUser);
        }
    }

}
