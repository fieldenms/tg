package ua.com.fielden.platform.entity.validation.test_entities;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;

/// DAO implementation for companion object [EntityWithDynamicRequirednessCo].
///
@EntityType(EntityWithDynamicRequiredness.class)
public class EntityWithDynamicRequirednessDao extends CommonEntityDao<EntityWithDynamicRequiredness> implements EntityWithDynamicRequirednessCo {

    @Override
    public EntityWithDynamicRequiredness new_() {
        final EntityWithDynamicRequiredness entity = super.new_();
        entity.getProperty("prop6").setRequired(true, EntityWithDynamicRequirednessCo.ERR_REQUIRED);
        entity.getProperty("prop7").setRequired(true, EntityWithDynamicRequirednessCo.ERR_REQUIRED);
        entity.getProperty("prop8").setRequired(true, EntityWithDynamicRequirednessCo.ERR_REQUIRED);
        return entity;
    }
    
    @Override
    protected IFetchProvider<EntityWithDynamicRequiredness> createFetchProvider() {
        return EntityWithDynamicRequirednessCo.FETCH_PROVIDER;
    }

}