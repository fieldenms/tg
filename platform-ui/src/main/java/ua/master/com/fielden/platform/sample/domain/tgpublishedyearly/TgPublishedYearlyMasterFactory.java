package ua.master.com.fielden.platform.sample.domain.tgpublishedyearly;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.domaintree.master.IMasterDomainTreeManager;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.sample.domain.ITgPublishedYearly;
import ua.com.fielden.platform.sample.domain.TgPublishedYearly;
import ua.com.fielden.platform.swing.model.IUmViewOwner;
import ua.com.fielden.platform.swing.model.callback.IPostInitCallback;
import ua.com.fielden.platform.swing.review.factory.IEntityMasterFactory;
import ua.com.fielden.platform.swing.view.BaseFrame;
import ua.com.fielden.platform.swing.view.IEntityMasterCache;

import com.google.inject.Inject;

/**
 * Factoring for instantiating master of entity {@link TgPublishedYearly}.
 * 
 * @author Developers
 * 
 */
public class TgPublishedYearlyMasterFactory implements IEntityMasterFactory<TgPublishedYearly, ITgPublishedYearly> {

    private final ITgPublishedYearly companionObject;
    private final ICriteriaGenerator criteriaGenerator;

    @Inject
    public TgPublishedYearlyMasterFactory(final ITgPublishedYearly companionObject, final ICriteriaGenerator criteriaGenerator) {
        this.companionObject = companionObject;
        this.criteriaGenerator = criteriaGenerator;
    }

    @Override
    public BaseFrame createMasterFrame(//
    final IEntityProducer<TgPublishedYearly> entityProducer,//
            final IEntityMasterCache cache,//
            final TgPublishedYearly entityObject,//
            final IValueMatcherFactory valueMatcherFactory,//
            final IMasterDomainTreeManager masterManager,//
            final IUmViewOwner owner, final IPostInitCallback<TgPublishedYearly, ITgPublishedYearly> postInitCallback) {
        return new TgPublishedYearlyMasterFrame(entityProducer, cache, entityObject, companionObject, valueMatcherFactory, owner, masterManager, criteriaGenerator, postInitCallback);
    }

}