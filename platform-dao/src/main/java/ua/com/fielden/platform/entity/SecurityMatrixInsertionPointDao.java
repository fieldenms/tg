package ua.com.fielden.platform.entity;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

@EntityType(SecurityMatrixInsertionPoint.class)
public class SecurityMatrixInsertionPointDao extends CommonEntityDao<SecurityMatrixInsertionPoint> implements ISecurityMatrixInsertionPoint {

    @Inject
    public SecurityMatrixInsertionPointDao(final IFilter filter) {
        super(filter);
    }


}
