package ua.com.fielden.platform.security.user;

import com.google.inject.Inject;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.continuation.NeedMoreData;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.exceptions.SecurityException;
import ua.com.fielden.platform.security.tokens.security_matrix.SecurityRoleAssociation_CanRead_Token;
import ua.com.fielden.platform.security.tokens.security_matrix.SecurityRoleAssociation_CanSave_Token;
import ua.com.fielden.platform.streaming.SequentialGroupingStream;
import ua.com.fielden.platform.types.either.Either;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.EntityUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.*;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.companion.helper.KeyConditionBuilder.createQueryByKeyFor;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.security.provider.ISecurityTokenProvider.MissingSecurityTokenPlaceholder;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.EntityUtils.fetchEntityForPropOf;

/// DAO implementation of [SecurityRoleAssociationCo].
///
@EntityType(SecurityRoleAssociation.class)
public class SecurityRoleAssociationDao extends CommonEntityDao<SecurityRoleAssociation> implements SecurityRoleAssociationCo {

    private static final Logger LOGGER = getLogger();

    public static final String MSG_DELETED_SECURITY_ROLE_ASSOCIATIONS_WITH_NON_EXISTING_TOKENS = "Deleted [%s] security role associations with non-existing tokens.";
    public static final String ERR_DONT_DELETE_ASSOCIATIONS_FOR_READING = "Unchecking [%s] security tokens will block access to the Security Matrix".formatted(SecurityRoleAssociation_CanRead_Token.TITLE);
    public static final String ERR_DONT_DELETE_ASSOCIATIONS_FOR_SAVING = "Unchecking [%s] security tokens will prevent you from editing the Security Matrix".formatted(SecurityRoleAssociation_CanSave_Token.TITLE);

    private final IUserProvider userProvider;

    @Inject
    public SecurityRoleAssociationDao(IUserProvider userProvider) {
        this.userProvider = userProvider;
    }


    @Override
    @SessionRequired
    public List<SecurityRoleAssociation> findAssociationsFor(final Class<? extends ISecurityToken> securityToken) {
        final var model = select(SecurityRoleAssociation.class).where()
                .prop("securityToken").eq().val(securityToken.getName()).and()
                .prop("active").eq().val(true).model();
        final var orderBy = orderBy().prop("role").asc().model();
        return getAllEntities(from(model).with(fetchAll(SecurityRoleAssociation.class)).with(orderBy).model());
    }

    @Override
    @SessionRequired
    public Map<Class<? extends ISecurityToken>, Set<UserRole>> findAllAssociations() {

        final var model = select(SecurityRoleAssociation.class).where().prop("active").eq().val(true).model();

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
        final var slaveModel = select(UserAndRoleAssociation.class)
                .where()
                .prop("user").eq().val(user)
                .and().prop("userRole.active").eq().val(true) // filter out association with inactive roles
                .and().prop("userRole.id").eq().prop("sra.role.id").model();
        final var model = select(SecurityRoleAssociation.class).as("sra")
                .where()
                .prop("sra.securityToken").eq().val(token.getName())
                .and().prop("sra.active").eq().val(true)
                .and().exists(slaveModel).model();
        return count(model);
    }

    @Override
    @SessionRequired
    public SecurityRoleAssociation save(final SecurityRoleAssociation entity) {
        return save(entity, Optional.of(FETCH_MODEL)).asRight().value();
    }

    @Override
    @SessionRequired
    @Authorise(SecurityRoleAssociation_CanSave_Token.class)
    protected Either<Long, SecurityRoleAssociation> save(final SecurityRoleAssociation entity, final Optional<fetch<SecurityRoleAssociation>> maybeFetch) {
        return super.save(entity, maybeFetch);
    }

    private List<SecurityRoleAssociation> findSecurityMatrixRelatedAssociationsForCurrentUser() {
        final var slaveModel = select(UserAndRoleAssociation.class)
                .where()
                .prop("user").eq().val(userProvider.getUser())
                .and().prop("userRole.active").eq().val(true) // filter out association with inactive roles
                .and().prop("userRole.id").eq().prop("sra.role.id").model();
        final var model = select(SecurityRoleAssociation.class).as("sra")
                .where()
                .prop("sra.securityToken").in().values(SecurityRoleAssociation_CanSave_Token.class.getName(), SecurityRoleAssociation_CanRead_Token.class.getName())
                .and().prop("sra.active").eq().val(true)
                .and().exists(slaveModel).model();
        return getAllEntities(from(model).with(FETCH_MODEL).model());
    }

    private boolean hasSecurityMatrixRelatedAssociations(final Collection<SecurityRoleAssociation> associations) {
        return associations.stream().anyMatch(association ->
                SecurityRoleAssociation_CanSave_Token.class.equals(association.getSecurityToken()) ||
                        SecurityRoleAssociation_CanRead_Token.class.equals(association.getSecurityToken()));
    }

    private List<SecurityRoleAssociation> extractAssociationsFor(Class<? extends ISecurityToken> token, List<SecurityRoleAssociation> associations) {
        return associations.stream().filter(association -> token.equals(association.getSecurityToken())).collect(toList());
    }

    @Override
    @SessionRequired
    public void removeAssociations(final Collection<SecurityRoleAssociation> associations) {
        // Check whether security matrix read / edit tokens can be deleted for current user.
        if (hasSecurityMatrixRelatedAssociations(associations)) {
            final List<SecurityRoleAssociation> matrixRelatedAssociationsForCurrentUser = findSecurityMatrixRelatedAssociationsForCurrentUser();
            final StringBuilder errorMsg = new StringBuilder();
            if (associations.containsAll(extractAssociationsFor(SecurityRoleAssociation_CanRead_Token.class, matrixRelatedAssociationsForCurrentUser))) {
                errorMsg.append(ERR_DONT_DELETE_ASSOCIATIONS_FOR_READING);
            }
            if (associations.containsAll(extractAssociationsFor(SecurityRoleAssociation_CanSave_Token.class, matrixRelatedAssociationsForCurrentUser))) {
                if (!errorMsg.isEmpty()) {
                    errorMsg.append("<br>");
                }
                errorMsg.append(ERR_DONT_DELETE_ASSOCIATIONS_FOR_SAVING);
            }
            if (!errorMsg.isEmpty()) {
                throw new SecurityException(errorMsg.toString());
            }
        }
        // Remove specified associations.
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
                    this.save(notFoundAssociation, empty());
                }));
    }

    private void fetchAssociationsAndModifyOrElse(
            final Collection<SecurityRoleAssociation> associations,
            final Consumer<SecurityRoleAssociation> modifier,
            final Optional<Consumer<SecurityRoleAssociation>> orElseOpt) {

        final TreeSet<SecurityRoleAssociation> notFoundAssociations = new TreeSet<>(associations);

        SequentialGroupingStream.stream(associations.stream(), (association, group) -> group.size() < 500)
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

        orElseOpt.ifPresent(orElse -> {
            for (final SecurityRoleAssociation notFoundAssociation : notFoundAssociations) {
                orElse.accept(notFoundAssociation);
            }
        });
    }

    private static EntityResultQueryModel<SecurityRoleAssociation> queryForAssociations(final List<SecurityRoleAssociation> group) {
        return select(SecurityRoleAssociation.class).where()
                .prop("securityToken").in().values(group.stream().map(a -> a.getSecurityToken().getName()).collect(toList())).and()
                .prop("role").in().values(group.stream().map(a -> a.getRole()).collect(toList())).model();
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
