package ua.com.fielden.platform.entity.functional.centre;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/** 
 * DAO implementation for companion object {@link ISavingInfoHolder}.
 * 
 * @author Developers
 *
 */
@EntityType(SavingInfoHolder.class)
public class SavingInfoHolderDao extends CommonEntityDao<SavingInfoHolder> implements ISavingInfoHolder {
    
    @Inject
    public SavingInfoHolderDao(final IFilter filter) {
        super(filter);
    }
    
}