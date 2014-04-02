package ua.master.com.fielden.platform.sample.domain.tgpublishedyearly;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.domaintree.master.IMasterDomainTreeManager;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.sample.domain.ITgPublishedYearly;
import ua.com.fielden.platform.sample.domain.TgPublishedYearly;
import ua.com.fielden.platform.swing.ei.development.MasterPropertyBinder;
import ua.com.fielden.platform.swing.model.FrameTitleUpdater;
import ua.com.fielden.platform.swing.model.IUmViewOwner;
import ua.com.fielden.platform.swing.model.UmMasterWithCrudAndUpdater;
import ua.com.fielden.platform.swing.model.UmState;
import ua.com.fielden.platform.swing.model.callback.IPostInitCallback;
import ua.com.fielden.platform.swing.view.IEntityMasterCache;

/**
 * Master model for entity {@link TgPublishedYearly}.
 * 
 * @author Developers
 * 
 */
public class TgPublishedYearlyMasterModel extends UmMasterWithCrudAndUpdater<TgPublishedYearly, ITgPublishedYearly> {

    private final static fetch<TgPublishedYearly> qm = fetchAll(TgPublishedYearly.class);

    public TgPublishedYearlyMasterModel(//
    final IEntityProducer<TgPublishedYearly> entityProducer,//
            final IEntityMasterCache cache,//
            final TgPublishedYearly entity,//
            final ITgPublishedYearly controller,//
            final IValueMatcherFactory valueMatcherFactory,//
            final IUmViewOwner owner,//
            final FrameTitleUpdater titleUpdater,//
            final IMasterDomainTreeManager masterManager,//
            final ICriteriaGenerator criteriaGenerator,//
            final IPostInitCallback<TgPublishedYearly, ITgPublishedYearly> postInitCallback) {
        super(entityProducer, cache, entity, controller, //
        MasterPropertyBinder.<TgPublishedYearly> createPropertyBinderWithLocatorSupport(//
        valueMatcherFactory, //
                masterManager, //
                criteriaGenerator),//
        qm, titleUpdater, owner, false);
        setState(UmState.VIEW);
        if (postInitCallback != null) {
            postInitCallback.run(this);
        }
    }

    @Override
    protected void notifyActionStageChange(final ActionStage actionState) {
        super.notifyActionStageChange(actionState);
        if (actionState == ActionStage.NEW_POST_ACTION) {
            // a designated property can be focused upon creaton of a new entity instance
            //getEditors().get("key").getEditor().requestFocusInWindow();
        } else if (actionState == ActionStage.EDIT_POST_ACTION) {
            // a designated property can be focused upon editing of the entity
            //getEditors().get("desc").getEditor().requestFocusInWindow();
        }
    }

    @Override
    public String toString() {
        return TitlesDescsGetter.getEntityTitleAndDesc(TgPublishedYearly.class).getKey() + " Master";
    }

    @Override
    protected String defaultTitle() {
        return toString();
    }
}