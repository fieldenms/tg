package ua.com.fielden.platform.entity;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

@EntityType(SecurityTokenTreeNodeEntity.class)
public class SecurityTokenTreeNodeEntityDao extends CommonEntityDao<SecurityTokenTreeNodeEntity> implements ISecurityTokenTreeNodeEntity {

    @Inject
    protected SecurityTokenTreeNodeEntityDao(final IFilter filter) {
        super(filter);
    }

}
