package ua.com.fielden.platform.devdb_support;

import ua.com.fielden.platform.algorithm.search.ITreeNodePredicate;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.provider.SecurityTokenNode;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;

/// A predicate, which saves associations between nodes' state as security token and the specified role while traversing the tree. Its primary purpose is to create associations
/// between the specified role and all security tokens.
///
public class SecurityTokenAssociator implements ITreeNodePredicate<Class<? extends ISecurityToken>, SecurityTokenNode> {

    private final UserRole role;
    private final IEntityDao<SecurityRoleAssociation> coSecurityRoleAssociation;

    public SecurityTokenAssociator(final UserRole role, final IEntityDao<SecurityRoleAssociation> coSecurityRoleAssociation) {
        this.role = role;
        this.coSecurityRoleAssociation = coSecurityRoleAssociation;
    }

    @Override
    public boolean eval(final SecurityTokenNode node) {
        final SecurityRoleAssociation assoc = coSecurityRoleAssociation.new_().setSecurityToken(node.state()).setRole(role);
        coSecurityRoleAssociation.save(assoc);
        return false;
    }

}
