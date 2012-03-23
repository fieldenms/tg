package ua.com.fielden.platform.example.dynamiccriteria.master;

import org.jfree.ui.RefineryUtilities;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.domaintree.master.IMasterDomainTreeManager;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory2;
import ua.com.fielden.platform.example.dynamiccriteria.entities.SimpleCompositeEntity;
import ua.com.fielden.platform.example.dynamiccriteria.iao.ISimpleCompositeEntityDao;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.swing.components.NotificationLayer.MessageType;
import ua.com.fielden.platform.swing.model.FrameTitleUpdater;
import ua.com.fielden.platform.swing.model.ICloseGuard;
import ua.com.fielden.platform.swing.model.IUmViewOwner;
import ua.com.fielden.platform.swing.view.BaseFrame;
import ua.com.fielden.platform.swing.view.IEntityMasterCache;
import ua.com.fielden.platform.utils.ResourceLoader;

public class SimpleCompositeEntityFrame extends BaseFrame {

    private static final long serialVersionUID = 3531115876272941059L;

    private final SimpleCompositeEntityView view;
    private final SimpleCompositeEntityModel model;

    public SimpleCompositeEntityFrame(final IEntityProducer<SimpleCompositeEntity> entityProducer,//
	    final IEntityMasterCache cache,//
	    final SimpleCompositeEntity entity, //
	    final ISimpleCompositeEntityDao controller,//
	    final IValueMatcherFactory2 valueMatcherFactory,//
	    //final IEntityMasterManager entityMasterFactory,//
	    //final IDaoFactory daoFactory, //
	    final IUmViewOwner owner,//
	    final IMasterDomainTreeManager masterManager, final ICriteriaGenerator criteriaGenerator) {
	super(TitlesDescsGetter.getEntityTitleAndDesc(SimpleCompositeEntity.class).getKey() + " Master: " + entity.getKey() + " -- " + entity.getDesc());
	setIconImage(ResourceLoader.getImage("images/tg-icon.png"));

	final FrameTitleUpdater titleUpdater = new FrameTitleUpdater(this);

	model = new SimpleCompositeEntityModel(entityProducer, cache, entity, controller, valueMatcherFactory, titleUpdater, owner, masterManager, criteriaGenerator);

	add(view = new SimpleCompositeEntityView(model));

	pack();
	RefineryUtilities.centerFrameOnScreen(this);
    }

    @Override
    protected void notify(final ICloseGuard guard) {
	view.notify(guard.whyCannotClose(), MessageType.WARNING);
    }

    public void enforceNewState() {
	model.getNewAction().actionPerformed(null);
    }
}
