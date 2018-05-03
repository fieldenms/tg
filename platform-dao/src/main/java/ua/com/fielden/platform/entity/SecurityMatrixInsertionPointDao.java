package ua.com.fielden.platform.entity;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchKeyAndDescOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.ISecurityRoleAssociation;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.security.SecurityTokenInfoUtils;
import ua.com.fielden.platform.security.provider.SecurityTokenNode;
import ua.com.fielden.platform.security.provider.SecurityTokenProvider;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;

@EntityType(SecurityMatrixInsertionPoint.class)
public class SecurityMatrixInsertionPointDao extends CommonEntityDao<SecurityMatrixInsertionPoint> implements ISecurityMatrixInsertionPoint {

    private final SecurityTokenProvider tokenProvider;

    @Inject
    public SecurityMatrixInsertionPointDao(final IFilter filter,
            @Named("tokens.path") final String tokenPath,
            @Named("tokens.package") final String tokenPackage) {
        super(filter);
        this.tokenProvider = new SecurityTokenProvider(tokenPath, tokenPackage);
    }

    @Override
    public SecurityMatrixInsertionPoint save(final SecurityMatrixInsertionPoint entity) {
        final List<SecurityTokenTreeNodeEntity> tokenEntities = tokenProvider.getTopLevelSecurityTokenNodes().stream().map(token -> createTokenNodeEntity(Optional.empty(), token)).collect(Collectors.toList());
        final EntityResultQueryModel<UserRole> userRoleQueryModel = select(UserRole.class).model();
        try (Stream<UserRole> stream = co(UserRole.class).stream(from(userRoleQueryModel).with(fetchKeyAndDescOnly(UserRole.class)).model())) {
            entity.setUserRoles(stream.collect(Collectors.toList()));
        }
        entity.setTokens(tokenEntities);
        final ISecurityRoleAssociation coTokenRoleAssociation = co(SecurityRoleAssociation.class);
        final Map<String, List<Long>> tokenRoleMap = coTokenRoleAssociation.findAllAssociations().entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().getName(), entry -> entry.getValue().stream().map(UserRole::getId).collect(Collectors.toList())));
        entity.setTokenRoleMap(tokenRoleMap);
        return super.save(entity);
    }

    private SecurityTokenTreeNodeEntity createTokenNodeEntity(final Optional<SecurityTokenTreeNodeEntity> parentNode, final SecurityTokenNode tokenNode) {
        final SecurityTokenTreeNodeEntity tokenTreeNode = new SecurityTokenTreeNodeEntity();
        tokenTreeNode.setParent(parentNode.orElse(null))
                     .setChildren(tokenNode.daughters().stream().map(child -> createTokenNodeEntity(Optional.of(tokenTreeNode), child)).collect(Collectors.toSet()))
                     .setTitle(SecurityTokenInfoUtils.shortDesc(tokenNode.getToken()))
                     .setKey(tokenNode.getToken().getName())
                     .setDesc(SecurityTokenInfoUtils.longDesc(tokenNode.getToken()));
        return tokenTreeNode;
    }
}
