package ua.com.fielden.platform.security.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.ISecurityRoleAssociationDao;
import ua.com.fielden.platform.dao.ISessionEnabled;
import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.AlwaysAccessibleToken;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserRole;

/**
 * Provides the simplest controller for retrieving data for the {@link SecurityTreeTableModel}. Implemented for testing purpose
 *
 * @author TG Team
 *
 */
public class SecurityTokenController implements ISecurityTokenController, ISessionEnabled {

    private final ISecurityRoleAssociationDao securityAssociationDao;

    private final IUserRoleDao roleDao;

    private Session session;

    @Override
    public Map<Class<? extends ISecurityToken>, Set<UserRole>> findAllAssociations() {
        return securityAssociationDao.findAllAssociations();
    }

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

    @Override
    @SessionRequired
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
    public boolean canAccess(final User user, final Class<? extends ISecurityToken> securityTokenClass) {
        if (securityTokenClass == AlwaysAccessibleToken.class) {
            return true;
        }

        return securityAssociationDao.countAssociations(user, securityTokenClass) > 0;
    }

    @Override
    public Session getSession() {
        if (session == null) {
            throw new IllegalStateException("Someone forgot to annotate some method with SessionRequired!");
        }
        return session;
    }

    @Override
    public void setSession(final Session session) {
        this.session = session;
    }
}
