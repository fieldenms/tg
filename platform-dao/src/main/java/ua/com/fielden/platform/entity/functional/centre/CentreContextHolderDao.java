package ua.com.fielden.platform.entity.functional.centre;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

/** 
 * DAO implementation for companion object {@link ICentreContextHolder}.
 * 
 * @author Developers
 *
 */
@EntityType(CentreContextHolder.class)
public class CentreContextHolderDao extends CommonEntityDao<CentreContextHolder> implements ICentreContextHolder {
    
    @Inject
    public CentreContextHolderDao(final IFilter filter) {
        super(filter);
    }
    
}