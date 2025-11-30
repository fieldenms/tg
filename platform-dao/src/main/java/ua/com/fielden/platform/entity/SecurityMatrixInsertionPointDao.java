package ua.com.fielden.platform.entity;

import com.google.inject.Inject;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.provider.ISecurityTokenNodeTransformation;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;
import ua.com.fielden.platform.security.provider.SecurityTokenNode;
import ua.com.fielden.platform.security.tokens.security_matrix.SecurityRoleAssociation_CanRead_Token;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.SecurityRoleAssociationCo;
import ua.com.fielden.platform.security.user.UserRole;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Comparator.comparing;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toMap;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;

@EntityType(SecurityMatrixInsertionPoint.class)
public class SecurityMatrixInsertionPointDao extends CommonEntityDao<SecurityMatrixInsertionPoint> implements SecurityMatrixInsertionPointCo {

    private final ISecurityTokenProvider tokenProvider;
    private final ISecurityTokenNodeTransformation tokenTransformation;

    @Inject
    protected SecurityMatrixInsertionPointDao(
            final ISecurityTokenNodeTransformation tokenTransformation,
            final ISecurityTokenProvider securityTokenProvider)
    {
        this.tokenProvider = securityTokenProvider;
        this.tokenTransformation = tokenTransformation;
    }

    @Override
    @SessionRequired
    @Authorise(SecurityRoleAssociation_CanRead_Token.class)
    public SecurityMatrixInsertionPoint save(final SecurityMatrixInsertionPoint entity) {
        final var tokenEntities = tokenTransformation.transform(tokenProvider.getTopLevelSecurityTokenNodes()).stream()
                                  .map(token -> createTokenNodeEntity(Optional.empty(), token)).toList();
        final var userRoleQueryModel = select(UserRole.class).model();
        entity.setUserRoles(co(UserRole.class).getAllEntities(from(userRoleQueryModel).with(fetchKeyAndDescOnly(UserRole.class)).model()));
        entity.setTokens(tokenEntities);
        final SecurityRoleAssociationCo coTokenRoleAssociation = co(SecurityRoleAssociation.class);
        final Map<String, List<Long>> tokenRoleMap = coTokenRoleAssociation.findAllAssociations().entrySet().stream()
                .collect(toMap(
                        entry -> entry.getKey().getName(),
                        entry -> entry.getValue().stream().map(UserRole::getId).toList()
                ));
        entity.setTokenRoleMap(tokenRoleMap)
              .setCalculated(true)
              .setRoleFilter("")
              .setTokenFilter("");
        return super.save(entity);
    }

    private SecurityTokenTreeNodeEntity createTokenNodeEntity(final Optional<SecurityTokenTreeNodeEntity> parentNode, final SecurityTokenNode tokenNode) {
        final SecurityTokenTreeNodeEntity tokenTreeNode = new SecurityTokenTreeNodeEntity();
        final var childrenNodes = tokenNode.daughters().stream().map(child -> createTokenNodeEntity(of(tokenTreeNode), child))
                                  // Ensure that child nodes are always sorted by the title.
                                  .sorted(comparing(SecurityTokenTreeNodeEntity::getTitle))
                                  .collect(toCollection(LinkedHashSet::new));
        tokenTreeNode.setParent(parentNode.orElse(null))
                     .setChildren(childrenNodes)
                     .setTitle(tokenNode.getShortDesc())
                     .setKey(tokenNode.getToken().getName())
                     .setDesc(tokenNode.getLongDesc());
        return tokenTreeNode;
    }

}
