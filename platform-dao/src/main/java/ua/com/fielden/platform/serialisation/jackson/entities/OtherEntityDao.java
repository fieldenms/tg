package ua.com.fielden.platform.serialisation.jackson.entities;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/** 
 * DAO implementation for companion object {@link IOtherEntity}.
 * 
 * @author Developers
 *
 */
@EntityType(OtherEntity.class)
public class OtherEntityDao extends CommonEntityDao<OtherEntity> implements IOtherEntity {
    
    @Inject
    public OtherEntityDao(final IFilter filter) {
        super(filter);
    }
    
}