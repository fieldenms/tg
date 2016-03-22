package ua.com.fielden.platform.entity.functional.centre;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import java.util.Map;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.functional.centre.mixin.SavingInfoHolderMixin;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import com.google.inject.Inject;

/** 
 * DAO implementation for companion object {@link ISavingInfoHolder}.
 * 
 * @author Developers
 *
 */
@EntityType(SavingInfoHolder.class)
public class SavingInfoHolderDao extends CommonEntityDao<SavingInfoHolder> implements ISavingInfoHolder {
    
    private final SavingInfoHolderMixin mixin;
    
    @Inject
    public SavingInfoHolderDao(final IFilter filter) {
        super(filter);
        
        mixin = new SavingInfoHolderMixin(this);
    }
    
}