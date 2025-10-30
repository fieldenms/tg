package ua.com.fielden.platform.security.user;

import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.streaming.SequentialGroupingStream;

import java.util.*;

import static java.util.stream.Collectors.partitioningBy;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.companion.helper.KeyConditionBuilder.createQueryByKeyFor;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.security.provider.ISecurityTokenProvider.MissingSecurityTokenPlaceholder;

/// DAO implementation of [SecurityRoleAssociationCo].
///
@EntityType(SecurityRoleAssociation.class)
public class SecurityRoleAssociationDao extends CommonEntityDao<SecurityRoleAssociation> implements SecurityRoleAssociationCo {

    private static final Logger LOGGER = getLogger();

    public static final String MSG_DELETED_SECURITY_ROLE_ASSOCIATIONS_WITH_NON_EXISTING_TOKENS = "Deleted [%s] security role associations with non-existing tokens.";

    @Override
    @SessionRequired
    public List<SecurityRoleAssociation> findAssociationsFor(final Class<? extends ISecurityToken> securityToken) {
        final var model = select(SecurityRoleAssociation.class).where().prop("securityToken").eq().val(securityToken.getName()).model();
        final var orderBy = orderBy().prop("role").asc().model();
        return getAllEntities(from(model).with(fetchAll(SecurityRoleAssociation.class)).with(orderBy).model());
    }

    @Override
    @SessionRequired
    public Map<Class<? extends ISecurityToken>, Set<UserRole>> findAllAssociations() {

        final var model = select(SecurityRoleAssociation.class).model();

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
                .and().exists(slaveModel).model();
        return count(model);
    }
    
    @Override
    @SessionRequired
    public void removeAssociations(final Collection<SecurityRoleAssociation> associations) {
        SequentialGroupingStream.stream(associations.stream(), (assoc, group) -> group.size() < 1000)
        .forEach(group -> createQueryByKeyFor(getDbVersion(), getEntityType(), getKeyType(), group).map(this::defaultBatchDelete));
    }

}
