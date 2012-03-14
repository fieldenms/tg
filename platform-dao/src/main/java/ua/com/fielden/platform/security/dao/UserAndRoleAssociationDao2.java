package ua.com.fielden.platform.security.dao;

import java.util.List;

import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao2.CommonEntityDao2;
import ua.com.fielden.platform.dao2.IUserAndRoleAssociationDao2;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * DbDriven implementation of the {@link IUserAndRoleAssociationDao2}
 *
 * @author TG Team
 *
 */
@EntityType(UserAndRoleAssociation.class)
public class UserAndRoleAssociationDao2 extends CommonEntityDao2<UserAndRoleAssociation> implements IUserAndRoleAssociationDao2 {

    @Inject
    protected UserAndRoleAssociationDao2(final IFilter filter) {
	super(filter);
    }

    @Override
    @SessionRequired
    public void removeAssociation(final List<UserAndRoleAssociation> associations) {
	if (associations.size() == 0) {
	    return;
	}
	String query = "delete from " + UserAndRoleAssociation.class.getName() + " where ";
	for (int associationIndex = 0; associationIndex < associations.size(); associationIndex++) {
	    if (associationIndex > 0) {
		query += " or ";
	    }
	    query += "(user.id=" + associations.get(associationIndex).getUser().getId() + " and userRole.id=" + //
		    associations.get(associationIndex).getUserRole().getId() + ")";
	}
	getSession().createQuery(query).executeUpdate();
    }
}