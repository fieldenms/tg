package ua.com.fielden.platform.entity;

import com.google.inject.Inject;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.exceptions.InvalidStateException;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.exceptions.SecurityException;
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
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;

@EntityType(SecurityMatrixSaveAction.class)
public class SecurityMatrixSaveActionDao extends CommonEntityDao<SecurityMatrixSaveAction> implements SecurityMatrixSaveActionCo{

    public static final String ERR_SECURITY_TOKEN_NOT_FOUND = "Security token [%s] could not be found.";
    public static final String ERR_CAN_NOT_DELETE_ASSOCIATIONS_FOR_READING = "Removing the [%s] security token from all your user roles will block access to the Security Matrix.".formatted(SecurityRoleAssociation_CanRead_Token.TITLE);
    public static final String ERR_CAN_NOT_DELETE_ASSOCIATIONS_FOR_SAVING = "Removing the [%s] security token from all your user roles will prevent you from editing the Security Matrix.".formatted(SecurityRoleAssociation_CanSave_Token.TITLE);


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
            final Set<SecurityRoleAssociation> addedAssociations = entity.getAssociationsToSave().entrySet().stream()
                .map(entry -> createSecurityRoleAssociations(entry.getKey(), entry.getValue(), idRoleMap))
                .flatMap(List::stream)
                .collect(toSet());
            final Set<SecurityRoleAssociation> removedAssociations = entity.getAssociationsToRemove().entrySet().stream()
                .map(entry -> createSecurityRoleAssociations(entry.getKey(), entry.getValue(), idRoleMap))
                .flatMap(List::stream)
                .collect(toSet());
            // Set empty associations for both save and remove operations to prevent client-side conversion errors if the save fails.
            entity.setAssociationsToRemove(Map.of()).setAssociationsToSave(Map.of());
            // Verify whether the security matrix read and edit tokens can be deleted for the current user.
            final String error = checkWhetherCanRemoveSecurityMatrixRelatedAssociations(removedAssociations);
            if (isNotEmpty(error)) {
                throw new SecurityException(error);
            }
            // Save associations.
            SecurityRoleAssociationCo associationCo$ = co$(SecurityRoleAssociation.class);
            associationCo$.addAssociations(addedAssociations);
            associationCo$.removeAssociations(removedAssociations);
        }
        return super.save(entity);
    }

    private String checkWhetherCanRemoveSecurityMatrixRelatedAssociations(final Set<SecurityRoleAssociation> removedAssociations) {
        final StringBuilder errorMsg = new StringBuilder();
        // Check whether the associations to be removed include any associations related to Security Matrix tokens.
        if (hasSecurityMatrixRelatedAssociations(removedAssociations)) {
            final SecurityRoleAssociationCo associationCo = co(SecurityRoleAssociation.class);
            final List<SecurityRoleAssociation> matrixRelatedAssociationsForCurrentUser = associationCo.findActiveAssociations(
                    userProvider.getUser(),
                    SecurityRoleAssociation_CanRead_Token.class,
                    SecurityRoleAssociation_CanSave_Token.class);
            // Append an error message if the user is about to remove all associations between his roles and SecurityRoleAssociation_CanRead_Token.
            if (removedAssociations.containsAll(extractAssociationsFor(SecurityRoleAssociation_CanRead_Token.class, matrixRelatedAssociationsForCurrentUser))) {
                errorMsg.append(ERR_CAN_NOT_DELETE_ASSOCIATIONS_FOR_READING);
            }
            // Append an error message if the user is about to remove all associations between his roles and SecurityRoleAssociation_CanSave_Token.
            if (removedAssociations.containsAll(extractAssociationsFor(SecurityRoleAssociation_CanSave_Token.class, matrixRelatedAssociationsForCurrentUser))) {
                if (!errorMsg.isEmpty()) {
                    errorMsg.append("<br>");
                }
                errorMsg.append(ERR_CAN_NOT_DELETE_ASSOCIATIONS_FOR_SAVING);
            }
        }
        return errorMsg.toString();
    }

    private boolean hasSecurityMatrixRelatedAssociations(final Collection<SecurityRoleAssociation> associations) {
        return associations.stream().anyMatch(association ->
                SecurityRoleAssociation_CanSave_Token.class.equals(association.getSecurityToken()) ||
                        SecurityRoleAssociation_CanRead_Token.class.equals(association.getSecurityToken()));
    }

    private List<SecurityRoleAssociation> extractAssociationsFor(Class<? extends ISecurityToken> token, List<SecurityRoleAssociation> associations) {
        return associations.stream().filter(association -> token.equals(association.getSecurityToken())).toList();
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
                .orElseThrow(() -> Result.failure(new InvalidStateException(ERR_SECURITY_TOKEN_NOT_FOUND.formatted(tokenClassSimpleName))));
    }

}
