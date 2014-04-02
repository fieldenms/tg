package ua.com.fielden.platform.devdb_support;

import ua.com.fielden.platform.algorithm.search.ITreeNodePredicate;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.provider.SecurityTokenNode;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;

/**
 * A predicate, which saves associations between nodes' state as security token and the specified role while traversing the tree. Its primary purpose is to create associations
 * between the specified role and all security tokens.
 * 
 * @author TG Team
 * 
 */
public class SecurityTokenAssociator implements ITreeNodePredicate<Class<? extends ISecurityToken>, SecurityTokenNode> {

    private final UserRole role;
    private final IEntityDao<SecurityRoleAssociation> controller;

    public SecurityTokenAssociator(final UserRole role, final IEntityDao<SecurityRoleAssociation> controller) {
        this.role = role;
        this.controller = controller;
    }

    @Override
    public boolean eval(final SecurityTokenNode node) {
        final EntityFactory factory = role.getEntityFactory();
        final SecurityRoleAssociation assoc = factory.newByKey(SecurityRoleAssociation.class, node.state(), role);
        controller.save(assoc);

        return false;
    }

}
