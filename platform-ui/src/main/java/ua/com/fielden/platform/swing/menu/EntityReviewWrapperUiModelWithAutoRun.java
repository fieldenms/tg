package ua.com.fielden.platform.swing.menu;

import ua.com.fielden.platform.swing.model.UmCustomEntityCentre;

/**
 * A convenient UI model for an entity review wrapper view, which automatically runs the review upon its initialisation.
 *
 * @author TG Team
 *
 */
public class EntityReviewWrapperUiModelWithAutoRun extends StubUiModel {

    private final UmCustomEntityCentre<?,?,?,?> entityReviewModel;

    public EntityReviewWrapperUiModelWithAutoRun(final UmCustomEntityCentre<?,?,?,?> entityReviewModel) {
	super(true);
	this.entityReviewModel = entityReviewModel;
    }

    @Override
    public boolean canOpen() {
	return entityReviewModel.canOpen();
    }

    @Override
    public String whyCannotOpen() {
	return entityReviewModel.whyCannotOpen();
    }

    @Override
    protected void notifyActionStageChange(final ActionStage actionState) {
	super.notifyActionStageChange(actionState);
	if (ActionStage.INIT_POST_ACTION == actionState) {
	    entityReviewModel.getRun().actionPerformed(null);
	}
    }

}
