package ua.com.fielden.platform.sample.domain;

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
import ua.com.fielden.platform.sample.domain.mixin.TgEntityForColourMasterMixin;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import com.google.inject.Inject;

/** 
 * DAO implementation for companion object {@link ITgEntityForColourMaster}.
 * 
 * @author Developers
 *
 */
@EntityType(TgEntityForColourMaster.class)
public class TgEntityForColourMasterDao extends CommonEntityDao<TgEntityForColourMaster> implements ITgEntityForColourMaster {
    
    private final TgEntityForColourMasterMixin mixin;
    
    @Inject
    public TgEntityForColourMasterDao(final IFilter filter) {
        super(filter);
        
        mixin = new TgEntityForColourMasterMixin(this);
    }
    
}