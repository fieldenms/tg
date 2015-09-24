package ua.com.fielden.platform.sample.domain.mixin;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.sample.domain.TgEntityForColourMaster;
import ua.com.fielden.platform.sample.domain.ITgEntityForColourMaster;

/** 
 * Mixin implementation for companion object {@link ITgEntityForColourMaster}.
 * 
 * @author Developers
 *
 */
public class TgEntityForColourMasterMixin {
    
    private final ITgEntityForColourMaster companion;
    
    public TgEntityForColourMasterMixin(final ITgEntityForColourMaster companion) {
        this.companion = companion;
    }
    
}