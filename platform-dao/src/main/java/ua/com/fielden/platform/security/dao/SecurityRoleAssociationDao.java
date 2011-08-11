package ua.com.fielden.platform.security.dao;

import java.util.List;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.ISecurityRoleAssociationDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.equery.fetchAll;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.equery.interfaces.IQueryModel;
import ua.com.fielden.platform.equery.interfaces.IQueryOrderedModel;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

import static ua.com.fielden.platform.equery.equery.select;

/**
 * DbDriven implementation of the {@link ISecurityRoleAssociationDao}
 *
 * @author TG Team
 *
 */
@EntityType(SecurityRoleAssociation.class)
public class SecurityRoleAssociationDao extends CommonEntityDao<SecurityRoleAssociation> implements ISecurityRoleAssociationDao {

    /**
     * Instantiates the {@link SecurityRoleAssociationDao}
     */
    @Inject
    protected SecurityRoleAssociationDao(final IFilter filter) {
	super(filter);
    }

    @Override
    @SessionRequired
    public List<SecurityRoleAssociation> findAssociationsFor(final  Class<? extends ISecurityToken> securityToken) {
	final IQueryOrderedModel<SecurityRoleAssociation> model = select(SecurityRoleAssociation.class)//
	.where().prop("securityToken").eq().val(securityToken.getName()).orderBy("role").model();
	return getEntities(model, new fetchAll(SecurityRoleAssociation.class));
    }

    @Override
    @SessionRequired
    public void removeAssociationsFor(final  Class<? extends ISecurityToken> securityToken) {
	getSession().createQuery("delete from " + SecurityRoleAssociation.class.getName() + " where securityToken = '" + securityToken.getName() + "'").executeUpdate();
    }

    @Override
    @SessionRequired
    public int countAssociations(final String username, final Class<? extends ISecurityToken> token) {
	final IQueryModel<UserAndRoleAssociation> slaveModel = select(UserAndRoleAssociation.class).where().prop("user.key").eq().val(username).and()
	.prop("userRole.id").eq().prop("sra.role.id").model();
	final IQueryOrderedModel<SecurityRoleAssociation> model = select(SecurityRoleAssociation.class, "sra").where().prop("sra.securityToken").eq().val(token.getName()).and().exists(slaveModel).model();
	return count(model);
    }
}
