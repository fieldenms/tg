package ua.com.fielden.platform.data;

import org.joda.time.DateTime;
import ua.com.fielden.platform.companion.ISaveWithFetch;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.security.provider.ISecurityTokenNodeTransformation;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;
import ua.com.fielden.platform.security.provider.SecurityTokenNodeTransformations;
import ua.com.fielden.platform.security.user.*;
import ua.com.fielden.platform.types.either.Either;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

public interface IDomainDrivenData {

    public static final String ADMIN = "ADMIN";
    public static final String BASE_SUFFIX = "_BASE";
    public static final String SUPER_SECRET_PASSWORD = "cooking with rocket fuel";

    /// Saves the specified entity and returns a refetched instance.
    ///
    /// If the return value is not needed, it is strongly recommended to use [#saveNoFetch(AbstractEntity)],
    /// as refetching is a costly operation.
    ///
    /// To specify a custom fetch model for refetching, use [#save(AbstractEntity, Optional)].
    ///
    <T extends AbstractEntity<?>> T save(final T instance);

    /// Calls _save-with-fetch_ on the companion of the specified entity.
    ///
    /// This method must be used only with those entities whose companion implements [ISaveWithFetch].
    /// Otherwise, a runtime exception will be thrown.
    ///
    /// To specify an empty optional for `maybeFetch`, consider using [#noFetch()], or simply use [#saveNoFetch(AbstractEntity)] instead.
    ///
    <T extends AbstractEntity<?>> Either<Long, T> save(T instance, Optional<fetch<T>> maybeFetch);

    /// A convenient method that returns an empty optional typed with a fetch model parameterised with an entity type inferred from context.
    /// It is intended to be used with [#save(AbstractEntity, Optional)].
    ///
    /// ```
    /// WorkOrder wo = ...;
    /// save(wo, noFetch());
    /// ```
    ///
    /// Alternatively, [#saveNoFetch(AbstractEntity)] can be used to achieve the same result.
    ///
    static <T extends AbstractEntity<?>> Optional<fetch<T>> noFetch() {
        return Optional.empty();
    }

    /// Saves the specified entity without refetching it.
    ///
    /// This method must be used only with those entities whose companion implements [ISaveWithFetch].
    /// Otherwise, a runtime exception will be thrown.
    ///
    /// @see #save(AbstractEntity)
    /// @see #save(AbstractEntity, Optional)
    ///
    default Long saveNoFetch(AbstractEntity<?> instance) {
        return save(instance, noFetch()).asLeft().value();
    }

    <T extends AbstractEntity<K>, K extends Comparable<?>> T new_(final Class<T> entityClass);

    <T extends AbstractEntity<K>, K extends Comparable<?>> T new_(final Class<T> entityClass, final K key);

    <T extends AbstractEntity<K>, K extends Comparable<?>> T new_(final Class<T> entityClass, final K key, final String desc);

    <T extends AbstractEntity<DynamicEntityKey>> T new_composite(final Class<T> entityClass, final Object... keys);

    <T> T getInstance(final Class<T> type);

    <T extends IEntityDao<E>, E extends AbstractEntity<?>> T co$(final Class<E> type);

    <T extends IEntityDao<E>, E extends AbstractEntity<?>> T co(final Class<E> type);

    Date date(final String dateTime);

    DateTime dateTime(final String dateTime);

    default BigDecimal decimal(final String value) {
        return new BigDecimal(value);
    }

    default Integer integer(final String value) {
        return Integer.valueOf(value);
    }

    default boolean saveDataPopulationScriptToFile() {
        return false;
    }

    default boolean useSavedDataPopulationScript() {
        return false;
    }

    default void setupUser(final User.system_users defaultUser, final String emailDomain) {
        if (useSavedDataPopulationScript()) {
            final IUser coUser = co$(User.class);
            final User su = coUser.findUser(defaultUser.name());
            final IUserProvider up = getInstance(IUserProvider.class);
            up.setUser(su);
        } else {

            // VIRTUAL_USER is a virtual user (cannot be persisted) and has full access to all security tokens
            // It should always be used as the current user for data population activities
            final IUser co$User = co$(User.class);
            final User u = new_(User.class, User.system_users.VIRTUAL_USER.name()).setBase(true);
            final IUserProvider up = getInstance(IUserProvider.class);
            up.setUser(u);

            final User _su = co$(User.class).findByKeyOptional(defaultUser.name())
                    .orElseGet(() -> save(new_(User.class, defaultUser.name()).setBase(true).setEmail(defaultUser + "@" + emailDomain).setActive(true)));
            final User su = co$User.resetPasswd(_su, SUPER_SECRET_PASSWORD).getKey();

            final UserRole admin = co$(UserRole.class).findByKeyOptional(ADMIN)
                    .orElseGet(() -> save(new_(UserRole.class, ADMIN, "A role, which has a full access to the the system and should be used only for users who need administrative previligies.").setActive(true)));

            if (!co(UserAndRoleAssociation.class).entityWithKeyExists(su, admin)) {
                save(new_composite(UserAndRoleAssociation.class, su, admin));
            }

            try {
                final var tokenTransformation = getInstance(ISecurityTokenNodeTransformation.class);
                final var transformedTree = tokenTransformation.transform(getInstance(ISecurityTokenProvider.class).getTopLevelSecurityTokenNodes());
                final SecurityRoleAssociationCo coSecurityRoleAssociation = co(SecurityRoleAssociation.class);
                coSecurityRoleAssociation.addAssociations(SecurityTokenNodeTransformations.flatten(transformedTree)
                                                                  .map(node -> coSecurityRoleAssociation.new_()
                                                                          .setRole(admin)
                                                                          .setSecurityToken(node.getToken())).toList());
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }

            up.setUser(su);
        }
    }

}
