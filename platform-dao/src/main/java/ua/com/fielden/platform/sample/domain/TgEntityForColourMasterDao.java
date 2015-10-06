package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.mixin.TgEntityForColourMasterMixin;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

/**
 * DAO implementation for companion object {@link ITgEntityForColourMaster}.
 *
 * @author Developers
 *
 */
@EntityType(TgEntityForColourMaster.class)
public class TgEntityForColourMasterDao extends CommonEntityDao<TgEntityForColourMaster>implements ITgEntityForColourMaster {

    private final TgEntityForColourMasterMixin mixin;
    //    private final TgEntityForColourMasterChangeSubject changeSubject;
    //
    //    @Inject
    //    public TgEntityForColourMasterDao(final TgEntityForColourMasterChangeSubject changeSubject, final IFilter filter) {
    //        super(filter);
    //
    //        this.changeSubject = changeSubject;
    //        mixin = new TgEntityForColourMasterMixin(this);
    //    }
    //
    //    /**
    //     * Overridden to publish entity change events to an application wide observable.
    //     */
    //    @Override
    //    @SessionRequired
    //    public TgEntityForColourMaster save(final TgEntityForColourMaster entity) {
    //        final TgEntityForColourMaster saved = super.save(entity);
    //        changeSubject.publish(saved);
    //        return saved;
    //    }
    //
    //    @Override
    //    @SessionRequired
    //    public void delete(final TgEntityForColourMaster entity) {
    //        defaultDelete(entity);
    //    }
    //
    //    @Override
    //    @SessionRequired
    //    public void delete(final EntityResultQueryModel<TgEntityForColourMaster> model, final Map<String, Object> paramValues) {
    //        defaultDelete(model, paramValues);
    //    }

    @Inject
    public TgEntityForColourMasterDao(final IFilter filter) {
        super(filter);

        mixin = new TgEntityForColourMasterMixin(this);
    }

    @Override
    public IFetchProvider<TgEntityForColourMaster> createFetchProvider() {
        return super.createFetchProvider().with("key").with("stringProp").with("booleanProp").with("colourProp");
    }

}