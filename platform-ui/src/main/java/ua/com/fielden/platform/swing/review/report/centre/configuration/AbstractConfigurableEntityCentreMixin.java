package ua.com.fielden.platform.swing.review.report.centre.configuration;

import ua.com.fielden.platform.domaintree.IDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.report.interfaces.IReviewEventListener;

public abstract class AbstractConfigurableEntityCentreMixin<T extends AbstractEntity<?>, DTM extends IDomainTreeManager> {

    private final CentreConfigurationModel<T> centreConfigurationModel;

    public AbstractConfigurableEntityCentreMixin(final CentreConfigurationModel<T> centreConfigurationModel){
	this.centreConfigurationModel = centreConfigurationModel;
    }

    //    abstract protected AbstractEntityCentre<T, DTM> initWithReviewAction(final AbstractEntityCentre<T, DTM> entityCentre);
    //    {
    //	entityCentre.addReviewEventListener(createSaveReviewEventListener());
    //	entityCentre.addReviewEventListener(createSaveAsReviewEventListener());
    //	return entityCentre;
    //    }

    /**
     * Provides the {@link IReviewEventListener} that handles entity centre's save as action.
     *
     * @return
     */
    abstract protected IReviewEventListener createSaveAsReviewEventListener();

    /**
     * Provides the {@link IReviewEventListener} that handles entity centre's save action.
     *
     * @return
     */
    abstract protected IReviewEventListener createSaveReviewEventListener();
    //    {
    //	return new IReviewEventListener() {
    //
    //	    @Override
    //	    public boolean configureActionPerformed(final ReviewEvent e) {
    //		switch (e.getReviewAction()) {
    //		case SAVE:
    //		    try {
    //			centreConfigurationModel.gdtm().saveEntityCentreManager(centreConfigurationModel.entityType(), centreConfigurationModel.name());
    //		    } catch (final IllegalArgumentException ex) {
    //			new DialogWithDetails(null, "Exception while opening report view", ex).setVisible(true);
    //			return false;
    //		    }
    //		    break;
    //		}
    //		return true;
    //	    }
    //	};
    //    }

    /**
     * Provides the {@link IReviewEventListener} that handles the entity centre's remove action.
     *
     * @return
     */
    abstract protected IReviewEventListener createRemoveReviewEventListener();
}
