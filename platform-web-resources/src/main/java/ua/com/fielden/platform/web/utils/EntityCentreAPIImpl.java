package ua.com.fielden.platform.web.utils;

import com.google.inject.Inject;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity_centre.exceptions.EntityCentreExecutionException;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.types.either.Either;
import ua.com.fielden.platform.types.either.Left;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.EntityCentreConfigCo;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.MainMenuItemCo;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.ICentreConfigSharingModel;
import ua.com.fielden.platform.web.interfaces.DeviceProfile;
import ua.com.fielden.platform.web.resources.webui.ConfigSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.Class.forName;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.tika.utils.StringUtils.isBlank;
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
import static ua.com.fielden.platform.web.interfaces.DeviceProfile.DESKTOP;
import static ua.com.fielden.platform.web.interfaces.DeviceProfile.MOBILE;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.*;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.RunActions.RUN;
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

    /// Creates centre configuration query for config `uuid` and `surrogateName` regardless of the device profile where it was created.
    ///
    /// @param maybeAlias alias for [EntityCentreConfig] which can be used for some outer query
    ///
    private static ICompoundCondition0<EntityCentreConfig> centreConfigQueryFor(final String uuid, final String surrogateName, final Optional<String> maybeAlias) {
        final var selectStart = select(EntityCentreConfig.class);
        return maybeAlias.map(selectStart::as).orElse(selectStart)
            .where().begin()
                .prop("title").like().val(PREFIX_OF.apply(surrogateName).apply(DESKTOP))
                .or().prop("title").like().val(PREFIX_OF.apply(surrogateName).apply(MOBILE))
            .end()
            .and().condition(centreConfigCondFor(uuid));
    }

    /// Determines [ConfigSettings] for a configuration, defined by `configUuid`.
    /// These include who owns the configuration, it's "save-as" name and [DeviceProfile], where it was created.
    ///
    /// Returns [Left] for invalid configuration.
    ///
    private static Either<Result, ConfigSettings> determineConfigurationSettings(final String configUuid, final ICompanionObjectFinder companionFinder) {
        // Blank uuid does not represent any centre configuration.
        if (isBlank(configUuid)) {
            return left(failure("Configuration UUID [%s] is blank.".formatted(configUuid)));
        }

        // Find "fresh" persisted configuration instance for which there is a corresponding "saved" instance for the same owner.
        final EntityCentreConfigCo eccCompanion = companionFinder.find(EntityCentreConfig.class);
        final Optional<EntityCentreConfig> freshConfigOpt = eccCompanion.getEntityOptional(
            from(centreConfigQueryFor(configUuid, FRESH_CENTRE_NAME, of("ecc"))
                .and().exists(centreConfigQueryFor(configUuid, SAVED_CENTRE_NAME, empty())
                    .and().prop("owner").eq().extProp("ecc.owner")
                    .model()
                ).model()
            )
            .with(fetchWithKeyAndDesc(EntityCentreConfig.class, true)
                .with("owner", "menuItem", "title")
                .fetchModel()
            ).model()
        );

        // If there is no such configuration, return invalid `Result`.
        if (freshConfigOpt.isEmpty()) {
            return left(failure("Configuration with [%s] UUID does not exist.".formatted(configUuid)));
        }

        final var freshConfig = freshConfigOpt.get();
        // Determine owner.
        final IUser coUser = companionFinder.find(User.class, true);
        final User owner = coUser.findUser(freshConfig.getOwner().getKey());

        // Determine menu item type.
        final var miTypeName = freshConfig.getMenuItem().getKey();
        final Class<? extends MiWithConfigurationSupport<?>> miType;
        try {
            miType = (Class<? extends MiWithConfigurationSupport<?>>) forName(miTypeName);
        } catch (final ClassNotFoundException notFoundException) {
            return left(failure(new EntityCentreExecutionException("Configuration's menu item type [%s] can not be found.".formatted(miTypeName), notFoundException)));
        }

        // Determine device.
        final var device = freshConfig.getTitle().startsWith(MOBILE.name()) ? MOBILE : DESKTOP;

        // Determine "save-as" name.
        final Optional<String> saveAsName = of(obtainTitleFrom(freshConfig.getTitle(), FRESH_CENTRE_NAME, device));
        if (LINK_CONFIG_TITLE.equals(saveAsName.get())) {
            return left(failure("Link configuration [%s] is not available for API running.".formatted(saveAsName)));
        }

        return right(new ConfigSettings(saveAsName, owner, device, miType));
    }

    @Override
    public <T extends AbstractEntity<?>> Either<Result, List<T>> entityCentreResult(final String configUuid) {
        return entityCentreResult(configUuid, empty());
    }

    private <T extends AbstractEntity<?>> Either<Result, List<T>> entityCentreResult(
        final String configUuid,
        final Optional<Integer> maybeCustomPageCapacity
    ) {
        // Find out the settings for configuration. Stop execution if the settings can not be determined or inapplicable.
        final var resultOrConfigSettings = determineConfigurationSettings(configUuid, companionFinder);
        if (resultOrConfigSettings.isLeft()) {
            return left(resultOrConfigSettings.asLeft().value());
        }
        final var configSettings = resultOrConfigSettings.asRight().value();

        // Determine current user to be returned back into the user provider once the execution has been performed.
        final User currentUser = userProvider.getUser();
        try {
            // Apply the configuration owner to user provider temporarily.
            userProvider.setUser(configSettings.owner());

            // Create custom object for centre running, containing all settings.
            // The only necessary setting is indication that centre should be run (i.e. not a page refresh / navigate).
            final Map<String, Object> customObject = mapOf(t2(RUN_ACTION_KEY, RUN.toString()));

            final MainMenuItemCo mmiCompanion = companionFinder.find(MainMenuItem.class);
            final IUser userCompanion = companionFinder.find(User.class);
            final EntityCentreConfigCo eccCompanion = companionFinder.find(EntityCentreConfig.class);

            // Create criteria entity for "fresh" surrogate configuration.
            final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ?> freshCriteriaEntity = createCriteriaValidationPrototype(FRESH_CENTRE_NAME, configSettings, companionFinder, critGenerator, webUiConfig, eccCompanion, mmiCompanion, userCompanion, sharingModel);

            // Validate the criteria entity. Stop execution if it is invalid.
            final Result validationResult = validateCriteriaBeforeRunning(freshCriteriaEntity, authorisationModel, securityTokenProvider);
            if (!validationResult.isSuccessful()) {
                return left(validationResult);
            }

            // Generate entities if the centre has IGenerator defined. Stop execution if generation result is not successful.
            // `customObject` does not have parameter for generation forcing -- this parameter is not important.
            final Result generationResult = generateDataIfNeeded(freshCriteriaEntity, webUiConfig, true, false, customObject);
            if (!generationResult.isSuccessful()) {
                return left(generationResult);
            }

            // Adjust page capacity to some custom value, if present.
            maybeCustomPageCapacity.ifPresent(
                customPageCapacity -> freshCriteriaEntity.getCentreDomainTreeMangerAndEnhancer().getSecondTick().setPageCapacity(customPageCapacity)
            );

            // Perform actual running of `freshCriteriaEntity` with `configSettings`.
            final var resultList = executeEntityCentreConfiguration(
                configSettings,
                empty(),
                true,
                customObject,
                freshCriteriaEntity,
                webUiConfig,
                companionFinder,
                critGenerator,
                entityFactory,
                null,
                eccCompanion,
                mmiCompanion,
                userCompanion,
                sharingModel
            );

            final List<T> list = new ArrayList<>();
            resultList.forEach(entity -> list.add((T) entity));
            return right(list);
        } catch (final Exception exception) {
            return left(failure(new EntityCentreExecutionException("Configuration with UUID [%s] and settings [%s] could not be executed.".formatted(configUuid, configSettings), exception)));
        } finally {
            // Return original user back to user provider.
            userProvider.setUser(currentUser);
        }
    }

    @Override
    public Either<Result, Boolean> entityCentreResultExists(final String configUuid) {
        return entityCentreResult(configUuid, of(1))
            .map(result -> !result.isEmpty());
    }

}
