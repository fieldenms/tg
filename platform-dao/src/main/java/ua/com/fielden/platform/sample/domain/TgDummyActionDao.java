package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

/** 
 * DAO implementation for companion object {@link ITgDummyAction}.
 * 
 * @author Developers
 *
 */
@EntityType(TgDummyAction.class)
public class TgDummyActionDao extends CommonEntityDao<TgDummyAction> implements ITgDummyAction {
    
    @Inject
    public TgDummyActionDao(final IFilter filter) {
        super(filter);
    }
    
    @Override
    public TgDummyAction save(TgDummyAction entity) {
        // let's introduce some delay to demonstrate action in progress spinner
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
        }

        return super.save(entity);
    }

}