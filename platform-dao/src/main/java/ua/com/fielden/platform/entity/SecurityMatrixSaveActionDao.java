package ua.com.fielden.platform.entity;

import com.google.inject.Inject;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.exceptions.InvalidStateException;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;
import ua.com.fielden.platform.security.tokens.security_matrix.SecurityRoleAssociation_CanRead_Token;
import ua.com.fielden.platform.security.tokens.security_matrix.SecurityRoleAssociation_CanSave_Token;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.SecurityRoleAssociationCo;
import ua.com.fielden.platform.security.user.UserRole;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;

@EntityType(SecurityMatrixSaveAction.class)
public class SecurityMatrixSaveActionDao extends CommonEntityDao<SecurityMatrixSaveAction> implements SecurityMatrixSaveActionCo {

    private final ISecurityTokenProvider securityTokenProvider;
    private final IUserProvider userProvider;

    @Inject
    protected SecurityMatrixSaveActionDao(final ISecurityTokenProvider securityTokenProvider, final IUserProvider userProvider) {
        this.securityTokenProvider = securityTokenProvider;
        this.userProvider = userProvider;
    }

    @Override
    @SessionRequired
    public SecurityMatrixSaveAction save(final SecurityMatrixSaveAction entity) {
        final Map<Long, UserRole> idRoleMap = getUserRoles(entity);
        if (!idRoleMap.isEmpty()) {
            final var assocationsToAdd = entity.getAssociationsToSave().entrySet().stream()
                .map(entry -> createSecurityRoleAssociations(entry.getKey(), entry.getValue(), idRoleMap))
                .flatMap(List::stream)
                .collect(toSet());
            final var assocationsToRemove = entity.getAssociationsToRemove().entrySet().stream()
                .map(entry -> createSecurityRoleAssociations(entry.getKey(), entry.getValue(), idRoleMap))
                .flatMap(List::stream)
                .collect(toSet());
            // Set empty associations for both save and remove operations to prevent client-side conversion errors if the save fails.
            entity.setAssociationsToRemove(Map.of()).setAssociationsToSave(Map.of());
            validateSecurityMatrixLockout(assocationsToRemove).ifFailure(Result::throwRuntime);
            // Save associations.
            final SecurityRoleAssociationCo co$Association = co$(SecurityRoleAssociation.class);
            co$Association.addAssociations(assocationsToAdd);
            co$Association.removeAssociations(assocationsToRemove);
        }
        return super.save(entity);
    }

    /// Returns a validation failure if, as a result of removing `assocations`, the current user would lock themselves out of the Security Matrix
    /// (either by blocking reading or saving for themselves).
    ///
    private Result validateSecurityMatrixLockout(final Set<SecurityRoleAssociation> assocationsToRemove) {
        // Check whether the associations to be removed include any associations related to Security Matrix tokens.
        if (hasSecurityMatrixRelatedAssociations(assocationsToRemove)) {
            final var msgBuilder = new StringBuilder();

            final SecurityRoleAssociationCo coAssociation = co(SecurityRoleAssociation.class);
            final var qCurrUserAssociations = coAssociation.selectActiveAssociations(
                    userProvider.getUser(),
                    SecurityRoleAssociation_CanRead_Token.class,
                    SecurityRoleAssociation_CanSave_Token.class);
            final var currUserAssociations = coAssociation.getAllEntities(from(qCurrUserAssociations).with(fetchNone(SecurityRoleAssociation.class).with(KEY)).model())
                    .stream()
                    .collect(groupingBy(SecurityRoleAssociation::getSecurityToken));
            // Is the user about to remove all associations between their roles and SecurityRoleAssociation_CanRead_Token?
            final var currUserReadAssociations = currUserAssociations.getOrDefault(SecurityRoleAssociation_CanRead_Token.class, List.of());
            if (!currUserReadAssociations.isEmpty() && assocationsToRemove.containsAll(currUserReadAssociations)) {
                msgBuilder.append(ERR_CAN_NOT_DELETE_ASSOCIATIONS_FOR_READING);
            }
            // Is the user about to remove all associations between their roles and SecurityRoleAssociation_CanSave_Token?
            final var currUserSaveAssociations = currUserAssociations.getOrDefault(SecurityRoleAssociation_CanSave_Token.class, List.of());
            if (!currUserSaveAssociations.isEmpty() && assocationsToRemove.containsAll(currUserSaveAssociations)) {
                if (!msgBuilder.isEmpty()) {
                    msgBuilder.append("<br>");
                }
                msgBuilder.append(ERR_CAN_NOT_DELETE_ASSOCIATIONS_FOR_SAVING);
            }

            return msgBuilder.isEmpty() ? successful() : failure(msgBuilder.toString());
        }
        else {
            return successful();
        }
    }

    private boolean hasSecurityMatrixRelatedAssociations(final Collection<SecurityRoleAssociation> associations) {
        return associations.stream()
                .anyMatch(association -> SecurityRoleAssociation_CanSave_Token.class.equals(association.getSecurityToken())
                                         || SecurityRoleAssociation_CanRead_Token.class.equals(association.getSecurityToken()));
    }

    private List<SecurityRoleAssociation> createSecurityRoleAssociations(final String securityToken, final List<Integer> roleIds, final Map<Long, UserRole> idRoleMap) {
        final Class<? extends ISecurityToken> token = loadToken(securityToken);
        final SecurityRoleAssociationCo associationCo = co$(SecurityRoleAssociation.class);
        return roleIds.stream().map(id -> associationCo.new_().setRole(idRoleMap.get(id.longValue())).setSecurityToken(token)).toList();
    }

    private Map<Long, UserRole> getUserRoles(final SecurityMatrixSaveAction entity) {
        final Set<Integer> userRoleIds = Stream.concat(entity.getAssociationsToSave().values().stream().flatMap(List::stream),
                                    entity.getAssociationsToRemove().values().stream().flatMap(List::stream)).collect(toSet());
        if (!userRoleIds.isEmpty()) {
            final EntityResultQueryModel<UserRole> userRolesQuery = select(UserRole.class).where().prop("id").in().values(userRoleIds.toArray()).model();
            try (Stream<UserRole> stream = co(UserRole.class).stream(from(userRolesQuery).with(fetchKeyAndDescOnly(UserRole.class)).model())) {
                return stream.collect(toMap(UserRole::getId, role -> role));
            }
        }
        return new HashMap<>();
    }

    private Class<? extends ISecurityToken> loadToken(final String tokenClassSimpleName) {
        return securityTokenProvider.getTokenByName(tokenClassSimpleName)
                .orElseThrow(() -> failure(new InvalidStateException(ERR_SECURITY_TOKEN_NOT_FOUND.formatted(tokenClassSimpleName))));
    }

}
