package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/** 
 * DAO implementation for companion object {@link ITgSystem}.
 * 
 * @author Developers
 *
 */
@EntityType(TgSystem.class)
public class TgSystemDao extends CommonEntityDao<TgSystem> implements ITgSystem {
    
    @Inject
    public TgSystemDao(final IFilter filter) {
        super(filter);
    }
    
    @Override
    public void delete(TgSystem entity) {
        defaultDelete(entity);
    }
    
}