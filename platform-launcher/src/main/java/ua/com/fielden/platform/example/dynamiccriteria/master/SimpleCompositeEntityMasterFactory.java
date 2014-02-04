package ua.com.fielden.platform.example.dynamiccriteria.master;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.domaintree.master.IMasterDomainTreeManager;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.example.dynamiccriteria.entities.SimpleCompositeEntity;
import ua.com.fielden.platform.example.dynamiccriteria.iao.ISimpleCompositeEntityDao;
import ua.com.fielden.platform.swing.model.IUmViewOwner;
import ua.com.fielden.platform.swing.model.callback.IPostInitCallback;
import ua.com.fielden.platform.swing.review.factory.IEntityMasterFactory;
import ua.com.fielden.platform.swing.view.BaseFrame;
import ua.com.fielden.platform.swing.view.IEntityMasterCache;

import com.google.inject.Inject;

public class SimpleCompositeEntityMasterFactory implements IEntityMasterFactory<SimpleCompositeEntity, ISimpleCompositeEntityDao> {

    private final ISimpleCompositeEntityDao dao;
    //private final IEntityMasterManager entityMasterManager;
    private final ICriteriaGenerator criteriaGenerator;

    @Inject
    public SimpleCompositeEntityMasterFactory(final ISimpleCompositeEntityDao dao, final ICriteriaGenerator criteriaGenerator){
	this.dao = dao;
	this.criteriaGenerator = criteriaGenerator;
    }

    @Override
    public BaseFrame createMasterFrame(final IEntityProducer<SimpleCompositeEntity> entityProducer, final IEntityMasterCache cache, final SimpleCompositeEntity entity, final IValueMatcherFactory vmf, final IMasterDomainTreeManager masterManager, final IUmViewOwner ownerView, final IPostInitCallback<SimpleCompositeEntity, ISimpleCompositeEntityDao> postInitCallback) {
	final SimpleCompositeEntityFrame frame = new SimpleCompositeEntityFrame(entityProducer, cache, entity, dao, vmf, //
		ownerView, masterManager, criteriaGenerator, postInitCallback);
	if (!entity.isPersisted()) {
	    frame.enforceNewState();
	}
	return frame;
    }

}
