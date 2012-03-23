package ua.com.fielden.platform.example.dynamiccriteria.master;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.domaintree.master.IMasterDomainTreeManager;
import ua.com.fielden.platform.entity.matcher.development.IValueMatcherFactory;
import ua.com.fielden.platform.example.dynamiccriteria.entities.SimpleCompositeEntity;
import ua.com.fielden.platform.example.dynamiccriteria.entities.SimpleECEEntity;
import ua.com.fielden.platform.example.dynamiccriteria.iao.ISimpleCompositeEntityDao;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressPane;
import ua.com.fielden.platform.swing.ei.development.MasterPropertyBinder;
import ua.com.fielden.platform.swing.model.FrameTitleUpdater;
import ua.com.fielden.platform.swing.model.IUmViewOwner;
import ua.com.fielden.platform.swing.model.UmMasterWithCrudAndUpdater;
import ua.com.fielden.platform.swing.model.UmState;
import ua.com.fielden.platform.swing.view.IEntityMasterCache;

public class SimpleCompositeEntityModel extends UmMasterWithCrudAndUpdater<SimpleCompositeEntity, ISimpleCompositeEntityDao> {

    /** Principle constructor */
    public SimpleCompositeEntityModel( //
	    final IEntityProducer<SimpleCompositeEntity> entityProducer,//
	    final IEntityMasterCache cache,//
	    final SimpleCompositeEntity entity, final ISimpleCompositeEntityDao controller, //
	    final IValueMatcherFactory valueMatcherFactory, //
	    //final IEntityMasterManager entityMasterFactory,//
	    final FrameTitleUpdater titleUpdater, //
	    final IUmViewOwner owner, //
	    //final IDaoFactory daoFactory, //
	    final IMasterDomainTreeManager masterManager, final ICriteriaGenerator criteriaGenerator) {
	super(entityProducer, cache, entity, controller, //
		MasterPropertyBinder.<SimpleCompositeEntity>createPropertyBinderWithLocatorSupport(//
			valueMatcherFactory, //
			masterManager, //
			criteriaGenerator),
			null, titleUpdater, owner, false);
	setState(UmState.VIEW);
    }

    @Override
    protected void preInit(final BlockingIndefiniteProgressPane blockingPane) {
	blockingPane.setText("Loading data...");
    }

    @Override
    protected void doInit(final BlockingIndefiniteProgressPane blockingPane) {
	setEntity(findById(getManagedEntity().getId(), true));
    }

    @Override
    protected void postInit(final BlockingIndefiniteProgressPane blockingPane) {
	setEditors(buildEditors(getEntity(), getController(), getPropertyBinder()));
	getView().buildUi();
	setState(UmState.VIEW);
    }

    @Override
    public String toString() {
	return TitlesDescsGetter.getEntityTitleAndDesc(SimpleECEEntity.class).getKey();
    }

    @Override
    protected String defaultTitle() {
	return toString();
    }

    @Override
    protected void notifyActionStageChange(final ua.com.fielden.platform.swing.model.UModel.ActionStage actionState) {
	super.notifyActionStageChange(actionState);

	// add focusing different editors depending on the action stage
	if (actionState == ActionStage.NEW_POST_ACTION) {
	    getEditors().get("key").getEditor().requestFocusInWindow();
	} else if (actionState == ActionStage.EDIT_POST_ACTION) {
	    getEditors().get("desc").getEditor().requestFocusInWindow();
	}
    }
}
