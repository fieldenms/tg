package ua.com.fielden.platform.entity;

import static java.util.Optional.of;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchKeyAndDescOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.ISecurityRoleAssociation;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.security.provider.ISecurityTokenNodeTransformation;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;
import ua.com.fielden.platform.security.provider.SecurityTokenNode;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;

@EntityType(SecurityMatrixInsertionPoint.class)
public class SecurityMatrixInsertionPointDao extends CommonEntityDao<SecurityMatrixInsertionPoint> implements ISecurityMatrixInsertionPoint {

    private final ISecurityTokenProvider tokenProvider;
    private final ISecurityTokenNodeTransformation tokenTransformation;

    @Inject
    public SecurityMatrixInsertionPointDao(final IFilter filter,
            final ISecurityTokenNodeTransformation tokenTransformation,
            final ISecurityTokenProvider securityTokenProvider) {
        super(filter);
        this.tokenProvider = securityTokenProvider;
        this.tokenTransformation = tokenTransformation;
    }

    @Override
    @SessionRequired
    public SecurityMatrixInsertionPoint save(final SecurityMatrixInsertionPoint entity) {
        final List<SecurityTokenTreeNodeEntity> tokenEntities = tokenTransformation.transform(tokenProvider.getTopLevelSecurityTokenNodes()).stream().map(token -> createTokenNodeEntity(Optional.empty(), token)).collect(toList());
        final EntityResultQueryModel<UserRole> userRoleQueryModel = select(UserRole.class).model();
        try (final Stream<UserRole> stream = co(UserRole.class).stream(from(userRoleQueryModel).with(fetchKeyAndDescOnly(UserRole.class)).model())) {
            entity.setUserRoles(stream.collect(toList()));
        }
        entity.setTokens(tokenEntities);
        final ISecurityRoleAssociation coTokenRoleAssociation = co(SecurityRoleAssociation.class);
        final Map<String, List<Long>> tokenRoleMap = coTokenRoleAssociation.findAllAssociations().entrySet().stream().collect(toMap(entry -> entry.getKey().getName(), entry -> entry.getValue().stream().map(UserRole::getId).collect(toList())));
        entity.setTokenRoleMap(tokenRoleMap)
              .setCalculated(true)
              .setRoleFilter("")
              .setTokenFilter("");
        return super.save(entity);
    }

    private SecurityTokenTreeNodeEntity createTokenNodeEntity(final Optional<SecurityTokenTreeNodeEntity> parentNode, final SecurityTokenNode tokenNode) {
        final SecurityTokenTreeNodeEntity tokenTreeNode = new SecurityTokenTreeNodeEntity();
        tokenTreeNode.setParent(parentNode.orElse(null))
                     .setChildren(tokenNode.daughters().stream().map(child -> createTokenNodeEntity(of(tokenTreeNode), child)).collect(toCollection(LinkedHashSet::new)))
                     .setTitle(tokenNode.getShortDesc())
                     .setKey(tokenNode.getToken().getName())
                     .setDesc(tokenNode.getLongDesc());
        return tokenTreeNode;
    }

}