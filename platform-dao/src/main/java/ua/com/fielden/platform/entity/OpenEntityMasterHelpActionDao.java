package ua.com.fielden.platform.entity;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link OpenEntityMasterHelpActionCo}.
 *
 * @author TG Team
 *
 */
@EntityType(OpenEntityMasterHelpAction.class)
public class OpenEntityMasterHelpActionDao extends CommonEntityDao<OpenEntityMasterHelpAction> implements OpenEntityMasterHelpActionCo {

    @Inject
    public OpenEntityMasterHelpActionDao(final IFilter filter) {
        super(filter);
    }

}