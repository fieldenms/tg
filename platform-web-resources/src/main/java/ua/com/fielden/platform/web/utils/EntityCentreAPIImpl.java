package ua.com.fielden.platform.web.utils;

import com.google.inject.Inject;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
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
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.ICentreConfigSharingModel;
import ua.com.fielden.platform.web.interfaces.DeviceProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.Class.forName;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.types.either.Either.left;
import static ua.com.fielden.platform.types.either.Either.right;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.mapOf;
import static ua.com.fielden.platform.utils.EntityUtils.fetchWithKeyAndDesc;
import static ua.com.fielden.platform.web.centre.CentreUpdater.PREFIX_OF;
import static ua.com.fielden.platform.web.centre.WebApiUtils.LINK_CONFIG_TITLE;
import static ua.com.fielden.platform.web.factories.webui.ResourceFactoryUtils.getEntityCentre;
import static ua.com.fielden.platform.web.interfaces.DeviceProfile.DESKTOP;
import static ua.com.fielden.platform.web.interfaces.DeviceProfile.MOBILE;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.*;
import static ua.com.fielden.platform.web.resources.webui.CriteriaResource.*;

public class EntityCentreAPIImpl implements EntityCentreAPI {
    private final ICompanionObjectFinder companionFinder;
    private final IUserProvider userProvider;
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
        final ICriteriaGenerator critGenerator,
        final IWebUiConfig webUiConfig,
        final EntityFactory entityFactory,
        final ICentreConfigSharingModel sharingModel,
        final IAuthorisationModel authorisationModel,
        final ISecurityTokenProvider securityTokenProvider
    ) {
        this.companionFinder = companionFinder;
        this.userProvider = userProvider;
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

    public record ConfigSettings (
        Optional<String> saveAsName,
        User owner,
        DeviceProfile device,
        Class<? extends MiWithConfigurationSupport<?>> miType
    ) {}

    private static Either<Result, ConfigSettings> findConfigSettings(final String configUuid, final ICompanionObjectFinder companionFinder) {
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

        if (freshConfigOpt.isEmpty()) {
            return left(Result.failure("Config with uuid %s does not exist.".formatted(configUuid)));
        }
        final User user = coUser.findUser(freshConfigOpt.get().getOwner().getKey());
        final Class<?> miTypeGen;
        try {
            miTypeGen = forName(freshConfigOpt.get().getMenuItem().getKey());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        Class<? extends MiWithConfigurationSupport<?>> miType = (Class<? extends MiWithConfigurationSupport<?>>) miTypeGen;
        final var device = freshConfigOpt.get().getTitle().startsWith(MOBILE.name()) ? MOBILE : DESKTOP;

        final Optional<String> saveAsName = of(obtainTitleFrom(freshConfigOpt.get().getTitle(), FRESH_CENTRE_NAME, device));
        if (LINK_CONFIG_TITLE.equals(saveAsName.get())) {
            return left(failure("Default / Link configs are not available for API running (%s).".formatted(saveAsName)));
        }

        return right(new ConfigSettings(saveAsName, user, device, miType));
    }

    @Override
    public <T extends AbstractEntity<?>, M extends EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>>> Either<Result, List<T>> entityCentreResult(
        final String configUuid
    ) {
        final var resultOrConfigSettings = findConfigSettings(configUuid, companionFinder);
        if (resultOrConfigSettings.isLeft()) {
            return left(resultOrConfigSettings.asLeft().value());
        }

        final var configSettings = resultOrConfigSettings.asRight().value();

        final User currentUser = userProvider.getUser();

        try {

            userProvider.setUser(configSettings.owner);

            final CentreContextHolder centreContextHolder = null;
            final Map<String, Object> customObject = mapOf(t2("@@action", RunActions.RUN.toString()));

            final boolean isRunning = true;
            final boolean isSorting = false;

            final MainMenuItemCo mmiCompanion = companionFinder.find(MainMenuItem.class);
            final IUser userCompanion = companionFinder.find(User.class);

            final EntityCentreConfigCo eccCompanion = companionFinder.find(EntityCentreConfig.class);

            final M freshCriteriaEntity = createCriteriaValidationPrototypeForAPI(
                FRESH_CENTRE_NAME,
                configSettings.miType, configSettings.saveAsName, companionFinder, critGenerator,
                configSettings.owner, configSettings.device, webUiConfig, eccCompanion, mmiCompanion, userCompanion, sharingModel
            );

            final Result validationResult = validateCriteriaBeforeRunning(freshCriteriaEntity, authorisationModel, securityTokenProvider);
            if (!validationResult.isSuccessful()) {
                return left(validationResult);
            }

            final EntityCentre<AbstractEntity<?>> centre = getEntityCentre(configSettings.miType.getName(), webUiConfig);
            final Result generationResult = generateDataIfNeeded(freshCriteriaEntity, centre, isRunning, isSorting, customObject);
            // if the data generation was unsuccessful based on the returned Result value then stop any further logic and return the obtained result
            // otherwise, proceed with the request handling further to actually query the data
            // in most cases, the generated and queried data would be represented by the same entity and, thus, the final query needs to be enhanced with user related filtering by property 'createdBy'
            if (!generationResult.isSuccessful()) {
                return left(generationResult);
            }

            final var resultList = run(
                empty(),
                isRunning,

                customObject,
                (EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ?>) freshCriteriaEntity,
                webUiConfig,
                companionFinder,
                configSettings.owner,
                critGenerator,
                entityFactory,
                centreContextHolder,
                eccCompanion,
                mmiCompanion,
                userCompanion,
                sharingModel,

                configSettings.miType,
                configSettings.saveAsName,
                configSettings.device,
                centre
            );

            final List<T> list = new ArrayList<>();
            resultList.forEach(entity -> list.add((T) entity) );
            return right(list);

        } finally {
            userProvider.setUser(currentUser);
        }
    }

}
