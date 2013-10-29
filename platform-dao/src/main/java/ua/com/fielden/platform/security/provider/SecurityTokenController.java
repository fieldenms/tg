package ua.com.fielden.platform.security.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.dao.ISecurityRoleAssociationDao;
import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.dao.annotations.Transactional;
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
public class SecurityTokenController implements ISecurityTokenController {

    private final ISecurityRoleAssociationDao securityAssociationDao;

    private final IUserRoleDao roleDao;

    /**
     * Creates new instance of SecurityTokenController with twelve user roles and security tokens
     */
    @Inject
    public SecurityTokenController(final ISecurityRoleAssociationDao securityAssociationDao, final IUserRoleDao roleDao) {
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
    public void saveSecurityToken(final Map<Class<? extends ISecurityToken>, Set<UserRole>> tokenToRoleAssociations) {
	for (final Class<? extends ISecurityToken> token : tokenToRoleAssociations.keySet()) {
	    securityAssociationDao.removeAssociationsFor(token);
	    final Set<UserRole> roles = tokenToRoleAssociations.get(token);
	    for (final UserRole role : roles) {
		securityAssociationDao.save(role.getEntityFactory().newByKey(SecurityRoleAssociation.class, token, role));
	    }
	}
    }

    public IUserRoleDao getRoleDao() {
	return roleDao;
    }

    public ISecurityRoleAssociationDao getSecurityAssociationDao() {
	return securityAssociationDao;
    }

    @Override
    public boolean canAccess(final String username, final Class<? extends ISecurityToken> securityTokenClass) {
	return securityAssociationDao.countAssociations(username, securityTokenClass) > 0;
    }
}
