package ua.com.fielden.platform.security.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.dao.annotations.Transactional;
import ua.com.fielden.platform.dao2.ISecurityRoleAssociationDao2;
import ua.com.fielden.platform.dao2.IUserRoleDao2;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;

import com.google.inject.Inject;

/**
 * Provides the simplest controller for retrieving data for the {@link SecurityTreeTableModel}. Implemented for testing purpose
 *
 * @author TG Team
 *
 */
public class SecurityTokenController2 implements ISecurityTokenController {

    private final ISecurityRoleAssociationDao2 securityAssociationDao;

    private final IUserRoleDao2 roleDao;

    /**
     * Creates new instance of SecurityTokenController with twelve user roles and security tokens
     */
    @Inject
    public SecurityTokenController2(final ISecurityRoleAssociationDao2 securityAssociationDao, final IUserRoleDao2 roleDao) {
	this.securityAssociationDao = securityAssociationDao;
	this.roleDao = roleDao;
    }

    @Override
    public List<UserRole> findUserRoles() {
	return roleDao.findAll();
    }

    @Override
    public List<UserRole> findUserRolesFor(final Class<? extends ISecurityToken> securityTokenClass) {
	final List<UserRole> roles = new ArrayList<UserRole>();
	for (final SecurityRoleAssociation association : securityAssociationDao.findAssociationsFor(securityTokenClass)) {
	    roles.add(association.getRole());
	}
	return roles;
    }

    @Transactional
    @Override
    public void saveSecurityToken(final Map<Class<? extends ISecurityToken>, List<UserRole>> tokenToRoleAssocations) {
	for (final Class<? extends ISecurityToken> token : tokenToRoleAssocations.keySet()) {
	    securityAssociationDao.removeAssociationsFor(token);
	    final List<UserRole> roles = tokenToRoleAssocations.get(token);
	    for (final UserRole role : roles) {
		securityAssociationDao.save(role.getEntityFactory().newByKey(SecurityRoleAssociation.class, token, role));
	    }
	}
    }

    public IUserRoleDao2 getRoleDao() {
	return roleDao;
    }

    public ISecurityRoleAssociationDao2 getSecurityAssociationDao() {
	return securityAssociationDao;
    }

    @Override
    public boolean canAccess(final String username, final Class<? extends ISecurityToken> securityTokenClass) {
	return securityAssociationDao.countAssociations(username, securityTokenClass) > 0;
    }
}
