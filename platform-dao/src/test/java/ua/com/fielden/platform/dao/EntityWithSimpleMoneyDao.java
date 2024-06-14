package ua.com.fielden.platform.dao;

import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.persistence.types.EntityWithSimpleMoney;

import com.google.inject.Inject;

/**
 * A DAO for {@link EntityWithSimpleMoney} used for testing.
 *
 * @author 01es
 *
 */
@EntityType(EntityWithSimpleMoney.class)
public class EntityWithSimpleMoneyDao extends CommonEntityDao<EntityWithSimpleMoney> {

    @Inject
    protected EntityWithSimpleMoneyDao(final IFilter filter) {
        super(filter);
    }
    
    @Override
    @SessionRequired
    public EntityWithSimpleMoney save(EntityWithSimpleMoney entity) {
        // the method is overridden to test guarding of quickSave invocations 
        return super.save(entity);
    }
    

}
