package ua.com.fielden.platform.swing.review.report.centre;

import javax.swing.JPanel;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.report.centre.configuration.CentreConfigurationWithoutCriteriaView;

/**
 * Represents the entity centre that has only review panel.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class EmptyEntityCentre<T extends AbstractEntity<?>> extends EntityCentreWithoutSelectionCriteria<T> {

    private static final long serialVersionUID = -2828215048397768235L;

    public EmptyEntityCentre(final EntityCentreModel<T> model, final CentreConfigurationWithoutCriteriaView<T> owner) {
	super(model, owner);
    }

    @Override
    protected JPanel createControlPanel() {
	return null;
    }
}
