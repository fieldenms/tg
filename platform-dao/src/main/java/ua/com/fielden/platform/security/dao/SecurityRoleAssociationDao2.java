package ua.com.fielden.platform.security.dao;

import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao2.CommonEntityDao2;
import ua.com.fielden.platform.dao2.ISecurityRoleAssociationDao2;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.fetchAll;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

/**
 * DbDriven implementation of the {@link ISecurityRoleAssociationDao2}
 *
 * @author TG Team
 *
 */
@EntityType(SecurityRoleAssociation.class)
public class SecurityRoleAssociationDao2 extends CommonEntityDao2<SecurityRoleAssociation> implements ISecurityRoleAssociationDao2 {

    /**
     * Instantiates the {@link SecurityRoleAssociationDao2}
     */
    @Inject
    protected SecurityRoleAssociationDao2(final IFilter filter) {
	super(filter);
    }

    @Override
    @SessionRequired
    public List<SecurityRoleAssociation> findAssociationsFor(final  Class<? extends ISecurityToken> securityToken) {
	final EntityResultQueryModel<SecurityRoleAssociation> model = select(SecurityRoleAssociation.class).where().prop("securityToken").eq().val(securityToken.getName()).model();
	final OrderingModel orderBy = orderBy().prop("role").asc().model();
	return getAllEntities(from(model).with(new fetchAll<SecurityRoleAssociation>(SecurityRoleAssociation.class)).with(orderBy).build());
    }

    @Override
    @SessionRequired
    public void removeAssociationsFor(final  Class<? extends ISecurityToken> securityToken) {
	getSession().createQuery("delete from " + SecurityRoleAssociation.class.getName() + " where securityToken = '" + securityToken.getName() + "'").executeUpdate();
    }

    @Override
    @SessionRequired
    public int countAssociations(final String username, final Class<? extends ISecurityToken> token) {
	final EntityResultQueryModel<UserAndRoleAssociation> slaveModel = select(UserAndRoleAssociation.class).where().prop("user.key").eq().val(username).and()
	.prop("userRole.id").eq().prop("sra.role.id").model();
	final EntityResultQueryModel<SecurityRoleAssociation> model = select(SecurityRoleAssociation.class).as("sra").where().prop("sra.securityToken").eq().val(token.getName()).and().exists(slaveModel).model();
	return count(model, Collections.<String, Object> emptyMap());
    }
}