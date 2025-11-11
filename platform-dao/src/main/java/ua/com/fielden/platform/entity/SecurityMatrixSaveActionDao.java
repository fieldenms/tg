package ua.com.fielden.platform.entity;

import com.google.inject.Inject;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.exceptions.InvalidStateException;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;
import ua.com.fielden.platform.security.tokens.security_matrix.SecurityRoleAssociation_CanSave_Token;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.SecurityRoleAssociationCo;
import ua.com.fielden.platform.security.user.UserRole;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;

@EntityType(SecurityMatrixSaveAction.class)
public class SecurityMatrixSaveActionDao extends CommonEntityDao<SecurityMatrixSaveAction> implements SecurityMatrixSaveActionCo{

    public static final String ERR_SECURITY_TOKEN_NOT_FOUND = "Security token [%s] could not be found.";

    private final ISecurityTokenProvider securityTokenProvider;

    @Inject
    protected SecurityMatrixSaveActionDao(final IFilter filter, final ISecurityTokenProvider securityTokenProvider) {
        this.securityTokenProvider = securityTokenProvider;
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
            entity.setAssociationsToRemove(new HashMap<>()).setAssociationsToSave(new HashMap<>());
            SecurityRoleAssociationCo associationCo = co$(SecurityRoleAssociation.class);
            associationCo.addAssociations(addedAssociations);
            associationCo.removeAssociations(removedAssociations);
        }
        return super.save(entity);
    }

    private List<SecurityRoleAssociation> createSecurityRoleAssociations(final String securityToken, final List<Integer> roleIds, final Map<Long, UserRole> idRoleMap) {
        final Class<? extends ISecurityToken> token = loadToken(securityToken);
        final SecurityRoleAssociationCo associationCo = co$(SecurityRoleAssociation.class);
        return roleIds.stream().map(id -> associationCo.new_().setRole(idRoleMap.get(id.longValue())).setSecurityToken(token)).collect(Collectors.toList());
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

    private Class<? extends ISecurityToken> loadToken(final String tokenClassSimpleName) {
        return securityTokenProvider.getTokenByName(tokenClassSimpleName)
                .orElseThrow(() -> Result.failure(new InvalidStateException(ERR_SECURITY_TOKEN_NOT_FOUND.formatted(tokenClassSimpleName))));
    }

}
