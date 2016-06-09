package ua.com.fielden.platform.entity.functional.centre;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.entity.functional.centre.mixin.SavingInfoHolderMixin;
import com.google.inject.Inject;

/** 
 * RAO implementation for master object {@link ISavingInfoHolder} based on a common with DAO mixin.
 * 
 * @author Developers
 *
 */
@EntityType(SavingInfoHolder.class)
public class SavingInfoHolderRao extends CommonEntityRao<SavingInfoHolder> implements ISavingInfoHolder {

    
    private final SavingInfoHolderMixin mixin;
    
    @Inject
    public SavingInfoHolderRao(final RestClientUtil restUtil) {
        super(restUtil);
        
        mixin = new SavingInfoHolderMixin(this);
    }
    
}