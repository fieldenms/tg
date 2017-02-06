package ua.com.fielden.platform.security.dao;

import java.util.Set;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.IUserAndRoleAssociation;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;

import com.google.inject.Inject;

/**
 * DbDriven implementation of the {@link IUserAndRoleAssociation}
 * 
 * @author TG Team
 * 
 */
@EntityType(UserAndRoleAssociation.class)
public class UserAndRoleAssociationDao extends CommonEntityDao<UserAndRoleAssociation> implements IUserAndRoleAssociation {

    @Inject
    protected UserAndRoleAssociationDao(final IFilter filter) {
        super(filter);
    }

    @Override
    @SessionRequired
    public void removeAssociation(final Set<UserAndRoleAssociation> associations) {
        createQueryByKeyFor(associations).map(query -> batchDelete(query));
    }
    
    @Override
    @SessionRequired
    public int batchDelete(final EntityResultQueryModel<UserAndRoleAssociation> model) {
        return defaultBatchDelete(model);
    }
}