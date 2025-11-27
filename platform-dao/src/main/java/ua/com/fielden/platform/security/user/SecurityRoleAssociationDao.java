package ua.com.fielden.platform.security.user;

import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.FetchModelReconstructor;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.security_matrix.SecurityRoleAssociation_CanSave_Token;
import ua.com.fielden.platform.streaming.SequentialGroupingStream;
import ua.com.fielden.platform.types.either.Either;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.partitioningBy;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.security.provider.ISecurityTokenProvider.MissingSecurityTokenPlaceholder;
import static ua.com.fielden.platform.security.user.SecurityRoleAssociation.ROLE;
import static ua.com.fielden.platform.security.user.SecurityRoleAssociation.SECURITY_TOKEN;

/// DAO implementation of [SecurityRoleAssociationCo].
///
@EntityType(SecurityRoleAssociation.class)
public class SecurityRoleAssociationDao extends CommonEntityDao<SecurityRoleAssociation> implements SecurityRoleAssociationCo {

    private static final Logger LOGGER = getLogger();
    private static final int MAX_NUMBER_OF_PARAMS = 500;

    public static final String MSG_DELETED_SECURITY_ROLE_ASSOCIATIONS_WITH_NON_EXISTING_TOKENS = "Deleted [%s] security role associations with non-existing tokens.";

    @Override
    @SessionRequired
    public List<SecurityRoleAssociation> findAssociationsFor(final Class<? extends ISecurityToken> securityToken) {
        final var model = select(SecurityRoleAssociation.class).where()
                .prop(SECURITY_TOKEN).eq().val(securityToken.getName()).and()
                .prop(ACTIVE).eq().val(true).model();
        final var orderBy = orderBy().prop(ROLE).asc().model();
        return getAllEntities(from(model).with(fetchAll(SecurityRoleAssociation.class)).with(orderBy).model());
    }

    @Override
    @SessionRequired
    public Map<Class<? extends ISecurityToken>, Set<UserRole>> findAllAssociations() {

        final var model = select(SecurityRoleAssociation.class).where().prop(ACTIVE).eq().val(true).model();

        final Map<Boolean, List<SecurityRoleAssociation>> partitionedAssociations = getAllEntities(from(model).with(fetchAll(SecurityRoleAssociation.class)).model()).stream()
                .collect(partitioningBy(association -> !association.getSecurityToken().equals(MissingSecurityTokenPlaceholder.class)));

        // Delete [SecurityRoleAssociation] records that reference non-existing security tokens.
        // This is more of an opportunistic data cleanup.
        // The `defaultBatchDelete` is used to delete records by their IDs.
        final var toDelete = partitionedAssociations.get(false).stream().map(SecurityRoleAssociation::getId).toList();
        if (!toDelete.isEmpty()) {
            final var deletedCount = defaultBatchDelete(toDelete);
            LOGGER.info(() -> MSG_DELETED_SECURITY_ROLE_ASSOCIATIONS_WITH_NON_EXISTING_TOKENS.formatted(deletedCount));
        }

        // Transform [SecurityRoleAssociation] to a map between security tokens and [UserRole] records with access to those tokens.
        final Map<Class<? extends ISecurityToken>, Set<UserRole>> associationMap = new HashMap<>();
        partitionedAssociations.get(true).forEach(association -> {
            final var roles = associationMap.computeIfAbsent(association.getSecurityToken(), k -> new HashSet<>());
            roles.add(association.getRole());
        });
        return associationMap;
    }

    @Override
    @SessionRequired
    public int countActiveAssociations(final User user, final Class<? extends ISecurityToken> token) {
        return count(selectActiveAssociations(user, token));
    }

    @Override
    public EntityResultQueryModel<SecurityRoleAssociation> selectActiveAssociations(final User user, final Class<? extends ISecurityToken>... tokens) {
        final var slaveModel = select(UserAndRoleAssociation.class)
                .where()
                .prop("user").eq().val(user)
                .and().prop("userRole.active").eq().val(true) // filter out association with inactive roles
                .and().prop("userRole.id").eq().prop("sra.role.id").model();
        return select(SecurityRoleAssociation.class).as("sra")
                .where()
                .prop("sra.securityToken").in().values(Stream.of(tokens).map(Class::getName).toList())
                .and().prop("sra.active").eq().val(true)
                .and().exists(slaveModel).model();
    }

    @Override
    @SessionRequired
    public SecurityRoleAssociation save(final SecurityRoleAssociation entity) {
        return save(entity, of(FetchModelReconstructor.reconstruct(entity))).asRight().value();
    }

    @Override
    @SessionRequired
    @Authorise(SecurityRoleAssociation_CanSave_Token.class)
    protected Either<Long, SecurityRoleAssociation> save(final SecurityRoleAssociation entity, final Optional<fetch<SecurityRoleAssociation>> maybeFetch) {
        return super.save(entity, maybeFetch);
    }

    @Override
    @SessionRequired
    public void removeAssociations(final Collection<SecurityRoleAssociation> associations) {
        fetchAssociationsAndModifyOrElse(
                associations,
                fetchedAssociation -> {
                    fetchedAssociation.setActive(false);
                    this.save(fetchedAssociation, empty());
                },empty());
    }

    @Override
    @SessionRequired
    public void addAssociations(final Collection<SecurityRoleAssociation> associations) {
        fetchAssociationsAndModifyOrElse(
                associations,
                fetchedAssociation -> {
                    fetchedAssociation.setActive(true);
                    this.save(fetchedAssociation, empty());
                },
                of(notFoundAssociation -> {
                    notFoundAssociation.setActive(true);
                    this.save(notFoundAssociation, empty());
                }));
    }

    private void fetchAssociationsAndModifyOrElse(
            final Collection<SecurityRoleAssociation> associations,
            final Consumer<SecurityRoleAssociation> modifier,
            final Optional<Consumer<SecurityRoleAssociation>> orElseOpt) {

        final Set<SecurityRoleAssociation> notFoundAssociations = new HashSet<>(associations);

        // First, update all existing associations.
        SequentialGroupingStream.stream(associations.stream(), (association, group) -> group.size() < MAX_NUMBER_OF_PARAMS)
                .forEach(group -> {
                    final var query = queryForAssociations(group);
                    try (final Stream<SecurityRoleAssociation> stream = stream(from(query).with(FETCH_MODEL).model())) {
                        stream.forEach(association -> {
                            modifier.accept(association);
                            if (notFoundAssociations.contains(association)) {
                                notFoundAssociations.remove(association);
                            }
                        });
                    }
                });
        // Then, perform action on those that weren’t fetched.
        orElseOpt.ifPresent(orElse -> {
            for (final SecurityRoleAssociation notFoundAssociation : notFoundAssociations) {
                orElse.accept(notFoundAssociation);
            }
        });
    }

    private static EntityResultQueryModel<SecurityRoleAssociation> queryForAssociations(final List<SecurityRoleAssociation> group) {
        return select(SecurityRoleAssociation.class).where()
                .prop(SECURITY_TOKEN).in().values(group.stream().map(a -> a.getSecurityToken().getName()).toList()).and()
                .prop(ROLE).in().values(group.stream().map(SecurityRoleAssociation::getRole).toList())
                .model();
    }

    @Override
    protected IFetchProvider<SecurityRoleAssociation> createFetchProvider() {
        return FETCH_PROVIDER;
    }

    @Override
    public SecurityRoleAssociation new_() {
        return super.new_().setActive(true);
    }
}
