package ua.com.fielden.platform.swing.review.report.centre.configuration;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.dialogs.DialogWithDetails;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;
import ua.com.fielden.platform.swing.review.report.events.ReviewEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.IReviewEventListener;

public abstract class ConfigurableEntityCentreMixin<T extends AbstractEntity> {

    private final CentreConfigurationModel<T> centreConfigurationModel;

    public ConfigurableEntityCentreMixin(final CentreConfigurationModel<T> centreConfigurationModel){
	this.centreConfigurationModel = centreConfigurationModel;
    }

    final AbstractEntityCentre<T> initWithReviewAction(final AbstractEntityCentre<T> entityCentre){
	entityCentre.addReviewEventListener(createSaveReviewEventListener());
	entityCentre.addReviewEventListener(createSaveAsReviewEventListener());
	return entityCentre;
    }

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
    protected IReviewEventListener createSaveReviewEventListener() {
	return new IReviewEventListener() {

	    @Override
	    public boolean configureActionPerformed(final ReviewEvent e) {
		switch (e.getReviewAction()) {
		case SAVE:
		    try {
			centreConfigurationModel.gdtm().saveEntityCentreManager(centreConfigurationModel.entityType(), centreConfigurationModel.name());
		    } catch (final IllegalArgumentException ex) {
			new DialogWithDetails(null, "Exception while opening report view", ex).setVisible(true);
			return false;
		    }
		    break;
		}
		return true;
	    }
	};
    }

    /**
     * Provides the {@link IReviewEventListener} that handles the entity centre's remove action.
     * 
     * @return
     */
    abstract protected IReviewEventListener createRemoveReviewEventListener();
}
