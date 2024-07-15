package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

@EntityType(EntityWithUnionEntityWithSkipExistsValidation.class)
public class EntityWithUnionEntityWithSkipExistsValidationDao extends CommonEntityDao<EntityWithUnionEntityWithSkipExistsValidation> implements EntityWithUnionEntityWithSkipExistsValidationCo {

    @Inject
    protected EntityWithUnionEntityWithSkipExistsValidationDao(final IFilter filter) {
        super(filter);
    }

}