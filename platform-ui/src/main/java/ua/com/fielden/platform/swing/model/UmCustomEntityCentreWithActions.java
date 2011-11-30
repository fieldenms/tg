package ua.com.fielden.platform.swing.model;

import ua.com.fielden.actionpanelmodel.ActionPanelBuilder;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.egi.models.builders.PropertyTableModelBuilder;
import ua.com.fielden.platform.swing.ei.editors.IPropertyBinder;
import ua.com.fielden.platform.swing.review.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.swing.view.BaseFrame;

/**
 * UI model based on a common model for custom entity centres ({@link UmCustomEntityCentre}), which provides a capability to have an action panel.
 *
 * @author TG Team
 *
 * @param <T>
 * @param <DAO>
 * @param <CRIT>
 * @param <F>
 */
public abstract class UmCustomEntityCentreWithActions<T extends AbstractEntity, DAO extends IEntityDao<T>, CRIT extends EntityQueryCriteria<T, DAO>, F extends BaseFrame> extends UmCustomEntityCentre<T, DAO, CRIT, F> {

    private final ActionPanelBuilder actionPanelBuilder;

    public UmCustomEntityCentreWithActions(//
	    final IEntityProducer<T> entityProducer, //
	    final CRIT criteria, //
	    final DAO controller, //
	    final PropertyTableModelBuilder<T> builder, //
	    final IPropertyBinder<CRIT> propertyBinder, //
	    final IEntityMasterManager entityMasterFactory,//
	    final ActionPanelBuilder panelBuilder) {
	super(entityProducer, criteria, controller, builder, propertyBinder, entityMasterFactory);

	actionPanelBuilder = new ActionPanelBuilder()//
	.addButton(createOpenMasterWithNewCommand())//
	.addButton(createOpenMasterCommand())//
	.addSeparator()//
	.addActionItems(panelBuilder);
    }

    /**
     * Returns a default action panel builder with actions open master and open master with new command.
     */
    @Override
    public ActionPanelBuilder getActionPanelBuilder() {
	return actionPanelBuilder;
    }

}
