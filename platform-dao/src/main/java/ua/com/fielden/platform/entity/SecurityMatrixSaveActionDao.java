package ua.com.fielden.platform.entity;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchKeyAndDescOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.ISecurityRoleAssociation;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.SecurityRoleAssociationBatchAction;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;

@EntityType(SecurityMatrixSaveAction.class)
public class SecurityMatrixSaveActionDao extends CommonEntityDao<SecurityMatrixSaveAction> implements ISecurityMatrixSaveAction{

    @Inject
    protected SecurityMatrixSaveActionDao(final IFilter filter) {
        super(filter);
    }

    @Override
    @SessionRequired
    public SecurityMatrixSaveAction save(final SecurityMatrixSaveAction entity) {
        final Map<Long, UserRole> idRoleMap = getUserRoles(entity);
        if (!idRoleMap.isEmpty()) {
            final Set<SecurityRoleAssociation> addedAssociations = entity.getAssociationsToSave().entrySet().stream()
                .map(entry -> createSecurityRoleAssociations(entry.getKey(), entry.getValue(), idRoleMap))
                .flatMap(List::stream)
                .collect(Collectors.toSet());
            final Set<SecurityRoleAssociation> removedAssociations = entity.getAssociationsToRemove().entrySet().stream()
                    .map(entry -> createSecurityRoleAssociations(entry.getKey(), entry.getValue(), idRoleMap))
                    .flatMap(List::stream)
                    .collect(Collectors.toSet());
            final SecurityRoleAssociationBatchAction batchAction = new SecurityRoleAssociationBatchAction();
            batchAction.setSaveEntities(addedAssociations);
            batchAction.setRemoveEntities(removedAssociations);
            co$(SecurityRoleAssociationBatchAction.class).save(batchAction);
        }
        return super.save(entity.setAssociationsToRemove(new HashMap<>()).setAssociationsToSave(new HashMap<>()));
    }

    private List<SecurityRoleAssociation> createSecurityRoleAssociations(final String securityToken, final List<Integer> roleIds, final Map<Long, UserRole> idRoleMap) {
        final Class<? extends ISecurityToken> token = loadToken(securityToken);
        final ISecurityRoleAssociation associationCo = co$(SecurityRoleAssociation.class);
        return roleIds.stream().map(id -> associationCo.new_().setRole(idRoleMap.get(Long.valueOf(id.longValue()))).setSecurityToken(token)).collect(Collectors.toList());
    }

    private Map<Long, UserRole> getUserRoles(final SecurityMatrixSaveAction entity) {
        final Set<Integer> userRoleIds = Stream.concat(entity.getAssociationsToSave().values().stream().flatMap(List::stream),
                                    entity.getAssociationsToRemove().values().stream().flatMap(List::stream)).collect(Collectors.toSet());
        if (!userRoleIds.isEmpty()) {
            final EntityResultQueryModel<UserRole> userRolesQuery = select(UserRole.class).where().prop("id").in().values(userRoleIds.toArray()).model();
            try (Stream<UserRole> stream = co(UserRole.class).stream(from(userRolesQuery).with(fetchKeyAndDescOnly(UserRole.class)).model())) {
                return stream.collect(Collectors.toMap(UserRole::getId, role -> role));
            }
        }
        return new HashMap<>();
    }

    private Class<? extends ISecurityToken> loadToken(final String name) {
        final Class<? extends ISecurityToken> token;
        try {
            token = (Class<? extends ISecurityToken>) Class.forName(name);
        } catch (final ClassNotFoundException e) {
            throw Result.failure(new IllegalStateException(String.format("Security token [%s] could not be found.", name)));
        }
        return token;
    }
}
