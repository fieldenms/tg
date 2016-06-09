package ua.com.fielden.platform.security.dao;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.ISecurityRoleAssociation;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

/**
 * DbDriven implementation of the {@link ISecurityRoleAssociation}
 * 
 * @author TG Team
 * 
 */
@EntityType(SecurityRoleAssociation.class)
public class SecurityRoleAssociationDao extends CommonEntityDao<SecurityRoleAssociation> implements ISecurityRoleAssociation {

    /**
     * Instantiates the {@link SecurityRoleAssociationDao}
     */
    @Inject
    protected SecurityRoleAssociationDao(final IFilter filter) {
        super(filter);
    }

    @Override
    @SessionRequired
    public List<SecurityRoleAssociation> findAssociationsFor(final Class<? extends ISecurityToken> securityToken) {
        final EntityResultQueryModel<SecurityRoleAssociation> model = select(SecurityRoleAssociation.class).where().prop("securityToken").eq().val(securityToken.getName()).model();
        final OrderingModel orderBy = orderBy().prop("role").asc().model();
        return getAllEntities(from(model).with(fetchAll(SecurityRoleAssociation.class)).with(orderBy).model());
    }

    @Override
    @SessionRequired
    public Map<Class<? extends ISecurityToken>, Set<UserRole>> findAllAssociations() {

        final EntityResultQueryModel<SecurityRoleAssociation> model = select(SecurityRoleAssociation.class).model();

        final List<SecurityRoleAssociation> associations = getAllEntities(from(model).with(fetchAll(SecurityRoleAssociation.class)).model());

        final Map<Class<? extends ISecurityToken>, Set<UserRole>> associationMap = new HashMap<>();
        for (final SecurityRoleAssociation association : associations) {
            Set<UserRole> roles = associationMap.get(association.getSecurityToken());
            if (roles == null) {
                roles = new HashSet<>();
                associationMap.put(association.getSecurityToken(), roles);
            }
            roles.add(association.getRole());
        }

        return associationMap;
    }

    @Override
    @SessionRequired
    public void removeAssociationsFor(final Class<? extends ISecurityToken> securityToken) {
        getSession().createQuery("delete from " + SecurityRoleAssociation.class.getName() + " where securityToken = '" + securityToken.getName() + "'").executeUpdate();
    }

    @Override
    @SessionRequired
    public int countActiveAssociations(final User user, final Class<? extends ISecurityToken> token) {
        final EntityResultQueryModel<UserAndRoleAssociation> slaveModel = select(UserAndRoleAssociation.class)
                .where()
                .prop("user").eq().val(user)
                .and().prop("userRole.active").eq().val(true) // filter out association with inactive roles
                .and().prop("userRole.id").eq().prop("sra.role.id").model();
        final EntityResultQueryModel<SecurityRoleAssociation> model = select(SecurityRoleAssociation.class).as("sra")
                .where()
                .prop("sra.securityToken").eq().val(token.getName())
                .and().exists(slaveModel).model();
        return count(model);
    }
    
    @Override
    @SessionRequired
    public void removeAssociations(final Set<SecurityRoleAssociation> associations) {
        if (associations.size() == 0) {
            return;
        }
        String query = "delete from " + SecurityRoleAssociation.class.getName() + " where ";
        final List<String> querySubstr = new ArrayList<>();
        for (final SecurityRoleAssociation assoc : associations) {
            querySubstr.add("(securityToken='" + assoc.getSecurityToken().getName() + "' and role.id=" + //
                    assoc.getRole().getId() + ")");
        }
        query += StringUtils.join(querySubstr, " or ");
        getSession().createQuery(query).executeUpdate();
    }
}