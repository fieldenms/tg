package ua.com.fielden.platform.entity;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchKeyAndDescOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.security.SecurityTokenInfo;
import ua.com.fielden.platform.security.provider.SecurityTokenNode;
import ua.com.fielden.platform.security.provider.SecurityTokenProvider;
import ua.com.fielden.platform.security.user.UserRole;

@EntityType(SecurityMatrixInsertionPoint.class)
public class SecurityMatrixInsertionPointDao extends CommonEntityDao<SecurityMatrixInsertionPoint> implements ISecurityMatrixInsertionPoint {

    private final SecurityTokenProvider tokenProvider;

    @Inject
    public SecurityMatrixInsertionPointDao(final IFilter filter, final SecurityTokenProvider tokenProvider) {
        super(filter);
        this.tokenProvider = tokenProvider;
    }

    @Override
    public SecurityMatrixInsertionPoint save(final SecurityMatrixInsertionPoint entity) {
        final List<SecurityTokenTreeNodeEntity> tokenEntities = tokenProvider.getTopLevelSecurityTokenNodes().stream().map(token -> createTokenNodeEntity(Optional.empty(), token)).collect(Collectors.toList());
        final EntityResultQueryModel<UserRole> userRoleQueryModel = select(UserRole.class).model();
        try (Stream<UserRole> stream = co(UserRole.class).stream(from(userRoleQueryModel).with(fetchKeyAndDescOnly(UserRole.class)).model())) {
            entity.setUserRoles(stream.collect(Collectors.toList()));
        }
        entity.setTokens(tokenEntities);
        return super.save(entity);
    }

    private SecurityTokenTreeNodeEntity createTokenNodeEntity(final Optional<SecurityTokenTreeNodeEntity> parentNode, final SecurityTokenNode tokenNode) {
        final SecurityTokenTreeNodeEntity tokenTreeNode = new SecurityTokenTreeNodeEntity();
        tokenTreeNode.setParent(parentNode.orElse(null))
                     .setChildren(tokenNode.daughters().stream().map(child -> createTokenNodeEntity(Optional.of(tokenTreeNode), child)).collect(Collectors.toSet()))
                     .setKey(SecurityTokenInfo.shortDesc(tokenNode.getToken()))
                     .setDesc(SecurityTokenInfo.longDesc(tokenNode.getToken()));
        return tokenTreeNode;
    }
}
