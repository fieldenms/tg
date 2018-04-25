package ua.com.fielden.platform.entity;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

@EntityType(SecurityMatrixSaveAction.class)
public class SecurityMatrixSaveActionDao extends CommonEntityDao<SecurityMatrixSaveAction> implements ISecurityMatrixSaveAction{

    @Inject
    protected SecurityMatrixSaveActionDao(final IFilter filter) {
        super(filter);
    }

}
